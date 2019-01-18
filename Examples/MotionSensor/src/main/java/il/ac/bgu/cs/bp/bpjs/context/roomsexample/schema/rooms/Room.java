package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.BasicEntity;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.Worker;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.MotionDetector;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.SmartLight;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.SmartSpeaker;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class Room extends BasicEntity {
	@Column
	private boolean hasPerson = false;
	@OneToOne(cascade = CascadeType.MERGE)
	private MotionDetector motionDetector;
	@OneToOne(cascade = CascadeType.MERGE)
	private SmartLight smartLight;
	@OneToOne(cascade = CascadeType.MERGE)
	private SmartSpeaker smartSpeaker;

	public Room() {
		super();
	}

	public Room(String id) {
		super(id);
		motionDetector = new MotionDetector(id + ".MotionDetector");
		smartLight = new SmartLight(id + ".SmartLight");
		smartSpeaker = new SmartSpeaker(id + ".SmartSpeaker");
	}

	public boolean hasPerson() {
		return hasPerson;
	}
	public void setHasPerson(boolean hasPerson) {
		this.hasPerson = hasPerson;
	}

	public MotionDetector getMotionDetector() {
		return motionDetector;
	}
	public SmartLight getSmartLight() {
		return smartLight;
	}

	@Override
	public String toString() {
		return "Room [" + getId() + "]";
	}
}
