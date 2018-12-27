package il.ac.bgu.cs.bp.bpjs.context.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class NewContextEvent extends BEvent {

	public String contextName;
	public Object ctx;

	public int col;

	public NewContextEvent(String contextName, Object ctx) {
		super("NewContextEvent(" + contextName + "," + ctx + ")");
		this.contextName = contextName;
		this.ctx = ctx;
	}
}