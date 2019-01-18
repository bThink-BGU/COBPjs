package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.MotionDetector;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class MotionStoppedEvent extends BEvent {
	public MotionStoppedEvent(MotionDetector sensor) {
		super("MotionStoppedEvent(" + sensor.getId() + ")");
	}
}