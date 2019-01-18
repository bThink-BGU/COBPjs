package il.ac.bgu.cs.bp.bpjs.context.roomsexample.events;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.AirConditioner;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class TurnACOffEvent extends BEvent {
	public TurnACOffEvent(AirConditioner ac) {
		super("TurnACOffEvent(" + ac.getId() + ")");
	}
}