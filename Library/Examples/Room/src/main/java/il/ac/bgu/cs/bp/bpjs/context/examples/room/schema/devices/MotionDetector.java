package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices;

import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.BasicEntity;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import javax.persistence.Entity;

@Entity
public class MotionDetector extends BasicEntity {
	protected MotionDetector() { super(); }
	public MotionDetector(String id) {
		super(id);
	}

	@SuppressWarnings("unused")
	public MotionStartedEvent startedEvent() {
		return new MotionStartedEvent();
	}

	@SuppressWarnings("unused")
	public MotionStoppedEvent stoppedEvent() {
		return new MotionStoppedEvent();
	}

	private class MotionStartedEvent extends BEvent {
		private MotionStartedEvent() {
			super("MotionStartedEvent(" + getId() + ")");
		}
	}

	private class MotionStoppedEvent extends BEvent {
		private MotionStoppedEvent() {
			super("MotionStoppedEvent(" + getId() + ")");
		}
	}
}
