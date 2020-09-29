package il.ac.bgu.cs.bp.bpjs.context.examples.gol;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

public class AnyDieEvent implements EventSet {

    @Override
    public boolean contains(BEvent e) {
        return e.name.equals("Die");
    }
}
