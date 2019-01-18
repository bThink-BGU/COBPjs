package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.BasicEntity;

import javax.persistence.Entity;

@Entity
public class SmartLight extends BasicEntity {
	public SmartLight() {
		super();
	}
	public SmartLight(String id) {
		super(id);
	}
}
