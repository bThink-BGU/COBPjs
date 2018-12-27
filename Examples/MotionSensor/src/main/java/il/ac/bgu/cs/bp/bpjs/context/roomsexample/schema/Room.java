package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Room extends BasicEntity {
	@Column
	private boolean hasPerson = false;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Room other = (Room) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
