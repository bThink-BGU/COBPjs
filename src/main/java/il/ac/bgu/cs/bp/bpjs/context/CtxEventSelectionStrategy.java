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
package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.AbstractEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

import org.mozilla.javascript.Context;

public final class CtxEventSelectionStrategy extends AbstractEventSelectionStrategy {

    private EventSelectionStrategy strategy;

    public CtxEventSelectionStrategy(long seed) {
        super(seed);
        strategy = new SimpleEventSelectionStrategy(seed);
    }

    public CtxEventSelectionStrategy() {
        this(new SimpleEventSelectionStrategy());
    }

    public CtxEventSelectionStrategy(EventSelectionStrategy eventSelectionStrategy) {
        strategy = eventSelectionStrategy;
    }

    public void setEventSelectionStrategy(EventSelectionStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Set<BEvent> selectableEvents(BProgramSyncSnapshot bpss) {

        Set<SyncStatement> statements = bpss.getStatements();
        List<BEvent> externalEvents = bpss.getExternalEvents();

        EventSet blocked = EventSets.anyOf(statements.stream()
            .filter(Objects::nonNull)
            .map(SyncStatement::getBlock)
            .filter(r -> r != EventSets.none)
            .collect(Collectors.toSet()));

        Set<BEvent> ctxEvents = statements.parallelStream()
            .filter(Objects::nonNull)
            .filter(s -> !getRequestedAndNotBlocked(s, blocked).isEmpty())
            .flatMap(s -> getRequestedAndNotBlocked(s, blocked).stream())
            .filter(e -> ContextProxy.CtxEvents.contains(e.name))
            .collect(toSet());
        if (ctxEvents.size() > 0) {
            for (BEvent e : ctxEvents) {
                if (e.name.equals("CTX.Changed"))
                    return new HashSet<>() {{
                        add(e);
                    }};
            }
            return ctxEvents;
        }

        return strategy.selectableEvents(bpss);
    }

    @Override
    public Optional<EventSelectionResult> select(BProgramSyncSnapshot bpss, Set<BEvent> selectableEvents) {
        return strategy.select(bpss, selectableEvents);
    }
}