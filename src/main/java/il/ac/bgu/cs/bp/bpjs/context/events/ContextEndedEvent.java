package il.ac.bgu.cs.bp.bpjs.context.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class ContextEndedEvent extends BEvent {

	public String contextName;
	public Object ctx;

	public int col;

	public ContextEndedEvent(String contextName, Object ctx) {
		super("ContextEndedEvent(" + contextName + "," + ctx + ")");
		this.contextName = contextName;
		this.ctx = ctx;
	}
}