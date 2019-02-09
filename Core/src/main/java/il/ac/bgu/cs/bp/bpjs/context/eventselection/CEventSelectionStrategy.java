package il.ac.bgu.cs.bp.bpjs.context.eventselection;

import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Strategy for selecting events from a set of {@link SyncStatement}s and an
 * external event queue.
 *
 * This class has two methods, one for detecting the set of selectable events,
 * and the other for selecting the actual event. The former is useful in both
 * execution and model checking. The latter - in execution only.
 *
 * @author michael
 */
public interface CEventSelectionStrategy extends EventSelectionStrategy {
    public void setPriority(String bThreadName, Integer priority);
}