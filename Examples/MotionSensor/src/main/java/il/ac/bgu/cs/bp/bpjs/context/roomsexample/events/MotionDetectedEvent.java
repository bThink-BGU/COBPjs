package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class MotionDetectedEvent extends BEvent {

	public MotionDetectedEvent(String sensor) {
		super("MotionDetectedEvent(" + sensor + ")");
	}
}