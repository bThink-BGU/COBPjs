package il.ac.bgu.cs.bp.bpjs.context.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class UnsubscribeEvent extends BEvent {
	public String id;

	public UnsubscribeEvent(String id) {
		super("UnsubscribeEvent(" + id + ")");
		this.id = id;
	}
}