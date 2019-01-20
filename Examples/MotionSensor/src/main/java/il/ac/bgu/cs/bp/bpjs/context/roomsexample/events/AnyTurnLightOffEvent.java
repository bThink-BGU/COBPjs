package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

public class AnyTurnLightOffEvent implements EventSet {
    @Override
    public boolean contains(BEvent event) {
        return event instanceof TurnACOffEvent;
    }
}
