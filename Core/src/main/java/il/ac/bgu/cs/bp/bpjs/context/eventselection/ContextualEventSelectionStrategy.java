package il.ac.bgu.cs.bp.bpjs.context.eventselection;


import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;

public interface ContextualEventSelectionStrategy extends EventSelectionStrategy {

    void setPriority(String bThreadName, Integer priority);

    int getPriority(String bThreadName);

    int getHighestPriority();

    int getLowestPriority();
}
