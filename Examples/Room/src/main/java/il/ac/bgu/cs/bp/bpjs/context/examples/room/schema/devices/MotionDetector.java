package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices;

import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.BasicEntity;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import javax.persistence.Entity;

@Entity
public class MotionDetector extends BasicEntity {
	private final String roomId;
	protected MotionDetector() { super(); 
	this.roomId = "";}
	public MotionDetector(String roomId) {
		super(roomId+ ".MotionDetector");
		this.roomId = roomId;
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
			super("MotionStartedEvent", roomId);
		}
	}

	private class MotionStoppedEvent extends BEvent {
		private MotionStoppedEvent() {
			super("MotionStoppedEvent", roomId);
		}
	}
}
