package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class MotionStoppedEvent extends BEvent {
	public MotionStoppedEvent(String sensor) {
		super("MotionStoppedEvent(" + sensor + ")");
	}
}