/*
 * The MIT License
 *
 * Copyright 2017 michael.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package il.ac.bgu.cs.bp.bpjs.context.eventselection;

import il.ac.bgu.cs.bp.bpjs.internal.Pair;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.AbstractEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;

/**
 * An event selection strategy that prefers events requested by BSync statements with higher priority.
 * BSync statement priority is determined by an integer added to the BSync metadata field, like so:
 *
 * <code>
 * bsync({ request:..., waitFor:... }, 2);
 * </code>
 *
 * @author michael
 */
public class PrioritizedBSyncEventSelectionStrategy extends AbstractEventSelectionStrategy implements ContextualEventSelectionStrategy {
    public static final int DEFAULT_PRIORITY = -1;

    /** A mapping of b-thread names to their priorities. */
    final private Map<String, Integer> priorities = new HashMap<>();


    public PrioritizedBSyncEventSelectionStrategy(long seed) {
        super(seed);
    }

    public PrioritizedBSyncEventSelectionStrategy() {
    }

    @Override
    public Set<BEvent> selectableEvents(BProgramSyncSnapshot bpss) {
        Set<SyncStatement> statements = bpss.getStatements();
        List<BEvent> externalEvents = bpss.getExternalEvents();

        if ( statements.isEmpty() ) {
            // Corner case, not sure this is even possible.
            return externalEvents.isEmpty() ? emptySet() : singleton(externalEvents.get(0));
        }

        final EventSet blocked = ComposableEventSet.anyOf(statements.stream()
                .filter(Objects::nonNull)
                .map(SyncStatement::getBlock )
                .filter( r -> r != EventSets.none )
                .collect( toSet() ) );

        Set<Pair<BEvent,Integer>> requestedPrioritizedByThread = statements.stream()
                .filter(Objects::nonNull)
                .flatMap(this::eventsToPrioritizedByThreadPairs)
                .collect( Collectors.toSet() );

        // Let's see what internal events are requested and not blocked (if any).
        try {
            Context.enter();

            Set<Pair<BEvent,Integer>> requestedAndNotBlockedWithPriorities = requestedPrioritizedByThread.stream()
                    .filter( req -> !blocked.contains(req.getLeft()) )
                    .collect( toSet() );

            if ( requestedAndNotBlockedWithPriorities.isEmpty() ) {
                return externalEvents.stream().filter(e -> !blocked.contains(e)) // No internal events requested, defer to externals.
                        .findFirst().map(Collections::singleton).orElse(emptySet());
            }

            Integer threadMaxValueOpt = requestedAndNotBlockedWithPriorities.stream()
                    .map(Pair::getRight)
                    .max(Comparator.comparing(Integer::valueOf)).get();

            Set<BEvent> requestedAndNotBlocked = requestedAndNotBlockedWithPriorities.stream()
                    .filter(p -> p.getRight().intValue() == threadMaxValueOpt.intValue())
                    .map(Pair::getLeft)
                    .collect(toSet());

            return getPrioritizedBSyncEvents(statements, requestedAndNotBlocked);
        } finally {
            Context.exit();
        }
    }

    @NotNull
    private Set<BEvent> getPrioritizedBSyncEvents(Set<SyncStatement> statements, Set<BEvent> filteredEvents) {
        try {
            Context.enter();

            OptionalInt maxValueOpt = statements.stream()
                    .filter(Objects::nonNull)
                    .filter( s -> !getRequestedAndInFilteredEvents(s, filteredEvents).isEmpty() )
                    .mapToInt(this::getSyncValue)
                    .max();

            int maxValue = maxValueOpt.getAsInt();
            return statements.stream()
                    .filter( Objects::nonNull )
                    .filter(s -> getSyncValue(s) == maxValue)
                    .flatMap(s -> getRequestedAndInFilteredEvents(s, filteredEvents).stream())
                    .collect(toSet());
        } finally {
            Context.exit();
        }
    }

    private Set<BEvent> getRequestedAndInFilteredEvents(SyncStatement stmt, Set<BEvent> filteredEvents) {
        try {
            Context.enter();
            return stmt.getRequest().stream().filter((BEvent req) -> filteredEvents.contains(req)).collect(toSet());
        } finally {
            Context.exit();
        }
    }

    private int getSyncValue(SyncStatement stmt ) {
        return (stmt.hasData() && (stmt.getData() instanceof Number))?
                ((Number)stmt.getData()).intValue() : Integer.MIN_VALUE;
    }

    private Stream<Pair<BEvent, Integer>> eventsToPrioritizedByThreadPairs(SyncStatement stmt) {
        final Collection<? extends BEvent> request = stmt.getRequest();
        if ( request.isEmpty() ) return Stream.empty();
        Integer priority = getPriority(stmt.getBthread().getName());
        return request.stream().map( e -> Pair.of(e, priority));
    }

    /*private Stream<Pair<SyncStatement, Integer>> statementsToPrioritizedByThreadPairs(SyncStatement stmt) {
        final Collection<? extends BEvent> request = stmt.getRequest();
        if ( request.isEmpty() ) return Stream.empty();
        Integer priority = getPriority(stmt.getBthread().getName());
        return Arrays.asList(Pair.of(stmt, priority)).stream();
    }*/

    public void setPriority(String bThreadName, Integer priority) {
        priorities.put(bThreadName, priority);
    }

    public int getPriority(String bThreadName) {
        return priorities.getOrDefault(bThreadName, DEFAULT_PRIORITY);
    }

    public int getHighestPriority() {
        return priorities.values().stream().mapToInt( Integer::intValue ).max().orElse(DEFAULT_PRIORITY);
    }

    public int getLowestPriority() {
        return priorities.values().stream().mapToInt( Integer::intValue ).min().orElse(DEFAULT_PRIORITY);
    }
}