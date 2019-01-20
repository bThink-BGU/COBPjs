package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.MotionDetector;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class MotionDetectedEvent extends BEvent {
	public final MotionDetector sensor;
	public MotionDetectedEvent(MotionDetector sensor) {
		super("MotionDetectedEvent");
		this.sensor = sensor;
	}

	@Override
	public String toString() {
		return  String.format("{0} ({1})",name,sensor.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MotionDetectedEvent other = (MotionDetectedEvent) obj;
		if (sensor == null) {
			if (other.sensor != null)
				return false;
		} else if (!sensor.equals(other.sensor))
			return false;
		return true;
	}
}