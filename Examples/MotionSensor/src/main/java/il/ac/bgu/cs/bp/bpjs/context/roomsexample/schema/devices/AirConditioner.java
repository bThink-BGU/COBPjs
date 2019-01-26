package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.BasicEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class AirConditioner extends BasicEntity {
	@Column
	private int temprature = 23;

	@Column
	private boolean turnedOn = false;

	protected AirConditioner() {
		super();
	}
	public AirConditioner(String id) {
		super(id);
	}

	public int getTemprature(){
		return temprature;
	}

	/*public void setTemprature(int temprature) {
		this.temprature = temprature;
	}*/

	public boolean isTurnedOn() {
		return turnedOn;
	}

	/*public void turnOn() {
		turnedOn = true;
	}*/

	/*public void turnOff() {
		turnedOn = false;
	}*/
}
