package il.ac.bgu.cs.bp.bpjs.context.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

@SuppressWarnings("serial")
public class AnyNewContextEvent implements EventSet {

	String contextName;

	public AnyNewContextEvent(String contextName) {
		super();
		this.contextName = contextName;
	}

	@Override
	public boolean contains(BEvent event) {
		return (event instanceof NewContextEvent) && ((NewContextEvent) event).contextName.equals(contextName);
	}
}
