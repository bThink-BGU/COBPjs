package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.BasicEntity;

import javax.persistence.Entity;

@Entity
public class SmartSpeaker extends BasicEntity {
	protected SmartSpeaker() {
		super();
	}
	public SmartSpeaker(String id) {
		super(id);
	}

	public void speak(String s) {
		System.out.println(s);
	}
}
