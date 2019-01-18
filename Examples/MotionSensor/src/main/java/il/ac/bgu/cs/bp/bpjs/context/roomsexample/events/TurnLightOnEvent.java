package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.SmartLight;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class TurnLightOnEvent extends BEvent {
	public TurnLightOnEvent(SmartLight light) {
		super("TurnLightOnEvent(" + light.getId() + ")");
	}
}