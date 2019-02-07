package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms;


import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.BasicEntity;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.Building;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices.MotionDetector;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices.SmartLight;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices.SmartSpeaker;

import javax.persistence.*;

@Entity
@NamedQueries(value = {
		@NamedQuery(name = "Room", query = "SELECT r FROM Room r"),
		@NamedQuery(name = "NonEmptyRoom", query = "SELECT r FROM Room r where r.hasPerson=true"),
		@NamedQuery(name = "MarkRoomAsEmpty", query = "Update Room R set R.hasPerson=false where R=:room"),
		@NamedQuery(name = "MarkRoomAsNonEmpty", query = "Update Room R set R.hasPerson=true where R=:room")
})
public class Room extends BasicEntity {
	@Column
	private boolean hasPerson = false;

	@OneToOne(cascade = CascadeType.MERGE)
	private MotionDetector motionDetector;

	@OneToOne(cascade = CascadeType.MERGE)
	private SmartLight smartLight;

	@OneToOne(cascade = CascadeType.MERGE)
	private SmartSpeaker smartSpeaker;

	@ManyToOne
	@JoinColumn(name = "building_fk")
	private Building building;

	protected Room() {
		super();
	}

	public Room(String id, Building building) {
		super(building.getId() + "/" + id);
		motionDetector = new MotionDetector(getId() + ".MotionDetector");
		smartLight = new SmartLight(getId() + ".SmartLight");
		smartSpeaker = new SmartSpeaker(getId() + ".SmartSpeaker");
		this.building = building;
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
