package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms;


import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.BasicEntity;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.System;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices.MotionDetector;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices.SmartLight;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices.SmartSpeaker;

import javax.persistence.*;

@Entity
@NamedQueries(value = {
		@NamedQuery(name = "Room", query = "SELECT r FROM Room r"),
		@NamedQuery(name = "EmptyRoom", query = "SELECT r FROM Room r where r.hasPerson=false "),
		@NamedQuery(name = "NonEmptyRoom", query = "SELECT r FROM Room r where r.hasPerson=true"),
		@NamedQuery(name = "NoMovement_3", query = "SELECT r FROM Room r where lastMovement + 3 <= (select s.time from System s)"),
		@NamedQuery(name = "MarkRoomAsEmpty", query = "Update Room R set R.hasPerson=false where R=:room"),
		@NamedQuery(name = "MarkRoomAsNonEmpty", query = "Update Room R set R.hasPerson=true where R=:room"),
		@NamedQuery(name = "UpdateMovement", query = "Update Room R set R.lastMovement=(select s.time from System s) where R.id=:roomId"),
})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Room extends BasicEntity {
	@Column
	private boolean hasPerson = false;

	@Column
	private int lastMovement = 0;

	@OneToOne(cascade = CascadeType.MERGE)
	private MotionDetector motionDetector;

	@OneToOne(cascade = CascadeType.MERGE)
	private SmartLight smartLight;

	@OneToOne(cascade = CascadeType.MERGE)
	private SmartSpeaker smartSpeaker;

	protected Room() {
		super();
	}

	public Room(String id) {
		super(id);
		motionDetector = new MotionDetector(getId());
		smartLight = new SmartLight(getId() + ".SmartLight");
		smartSpeaker = new SmartSpeaker(getId() + ".SmartSpeaker");
	}

	public boolean hasPerson() {
		return hasPerson;
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
