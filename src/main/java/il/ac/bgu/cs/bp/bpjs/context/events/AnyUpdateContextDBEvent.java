package il.ac.bgu.cs.bp.bpjs.context.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

@SuppressWarnings("serial")
public class AnyUpdateContextDBEvent implements EventSet {

	@Override
	public boolean contains(BEvent event) {
		return event instanceof UpdateContexDBEvent;
	}
}
