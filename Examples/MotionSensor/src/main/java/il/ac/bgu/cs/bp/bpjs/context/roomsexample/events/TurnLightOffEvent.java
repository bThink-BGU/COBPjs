package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.SmartLight;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class TurnLightOffEvent extends BEvent {
	public TurnLightOffEvent(SmartLight light) {
		super("TurnLightOffEvent(" + light.getId() + ")");
	}
}