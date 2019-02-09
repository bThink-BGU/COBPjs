package il.ac.bgu.cs.bp.bpjs.context.eventselection;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import il.ac.bgu.cs.bp.bpjs.internal.Pair;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import org.mozilla.javascript.Context;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.AbstractEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;

/**
 * An event selection strategy that prefers events from b-threads with higher
 * priorities. The higher the number assigned to a b-thread, the higher its
 * priority is.
 * 
 * @author geraw
 * @author michael
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CPrioritizedBThreadsEventSelectionStrategy extends AbstractEventSelectionStrategy implements CEventSelectionStrategy {

    public static final int DEFAULT_PRIORITY = -1;
    
    /** A mapping of b-thread names to their priorities. */
    final private Map<String, Integer> priorities = new HashMap<>();
    
    public CPrioritizedBThreadsEventSelectionStrategy(long seed ) {
        super(seed);
    }

    public CPrioritizedBThreadsEventSelectionStrategy() { }

    @Override
    public Set<BEvent> selectableEvents(BProgramSyncSnapshot bpss) {
        Set<SyncStatement> statements = bpss.getStatements();
        List<BEvent> externalEvents = bpss.getExternalEvents();

        if ( statements.isEmpty() ) {
            // Corner case, not sure this is even possible.
            return externalEvents.isEmpty() ? Collections.emptySet() : singleton(externalEvents.get(0));
        } else {
            final EventSet blocked = ComposableEventSet.anyOf(statements.stream()
                    .filter(Objects::nonNull)
                    .map(SyncStatement::getBlock)
                    .filter(r -> r != EventSets.none)
                    .collect(toSet()));

            Set<Pair<BEvent, Integer>> requested = statements.stream()
                    .filter(Objects::nonNull)
                    .flatMap(this::eventsToPrioritizedPairs)
                    .collect(Collectors.toSet());

            // Let's see what internal events are requested and not blocked (if any).
            try {
                Context.enter();

                Set<Pair<BEvent, Integer>> requestedAndNotBlockedWithPriorities = requested.stream()
                        .filter(req -> !blocked.contains(req.getLeft()))
                        .collect(toSet());


                //TODO: understand this one
                Integer highestPriority = requestedAndNotBlockedWithPriorities.isEmpty() ? -1 : requestedAndNotBlockedWithPriorities.stream()
                        .map(Pair::getRight)
                        .max(Comparator.comparing(Integer::valueOf)).get();

                Set<BEvent> requestedAndNotBlocked = requestedAndNotBlockedWithPriorities.stream()
                        .filter(p -> p.getRight().intValue() == highestPriority.intValue())
                        .map(Pair::getLeft)
                        .collect(toSet());

                return requestedAndNotBlocked.isEmpty() ?
                        externalEvents.stream().filter(e -> !blocked.contains(e)) // No internal events requested, defer to externals.
                                .findFirst().map(Collections::singleton).orElse(emptySet())
                        : requestedAndNotBlocked;
            } finally {
                Context.exit();
            }
        }
    }

    private Stream<Pair<BEvent, Integer>> eventsToPrioritizedPairs(SyncStatement stmt) {
        final Collection<? extends BEvent> request = stmt.getRequest();
        if ( request.isEmpty() ) return Stream.empty();
        Integer priority = getPriority(stmt.getBthread().getName());
        return request.stream().map( e -> Pair.of(e, priority));
    }

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
