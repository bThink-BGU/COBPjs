package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.AirConditioner;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.SmartLight;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class TurnACOnEvent extends BEvent {
	public TurnACOnEvent(AirConditioner ac) {
		super("TurnACOnEvent(" + ac.getId() + ")");
	}
}