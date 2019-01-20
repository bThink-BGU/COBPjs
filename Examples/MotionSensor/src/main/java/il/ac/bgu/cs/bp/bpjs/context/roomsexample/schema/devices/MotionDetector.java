package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.BasicEntity;

import javax.persistence.Entity;

@Entity
public class MotionDetector extends BasicEntity {
	protected MotionDetector() { super(); }
	public MotionDetector(String id) {
		super(id);
	}
}
