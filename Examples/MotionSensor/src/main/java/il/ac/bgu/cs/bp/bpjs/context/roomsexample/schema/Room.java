package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Room extends BasicEntity {
	@Column
	private boolean hasPerson=false;
	
	@Column
	private String motionSensor;

	public Room() {
		super();
	}

	public Room(String id) {
		super(id);
		motionSensor = id + ".MotionSensor";
	}

	public boolean hasPerson() {
		return hasPerson;
	}

	public void setHasPerson(boolean hasPerson) {
		this.hasPerson = hasPerson;
	}

	
	public String getMotionSensor() {
		return motionSensor;
	}

	@Override
	public String toString() {
		return "Room [" + getId() + "]";
	}

}
