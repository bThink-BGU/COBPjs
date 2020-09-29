package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices;

import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.BasicEntity;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

import javax.persistence.Entity;

@Entity
public class SmartLight extends BasicEntity {
	protected SmartLight() {
		super();
	}
	public SmartLight(String id) {
		super(id);
	}

	@SuppressWarnings("unused")
	public TurnLightOnEvent onEvent() {
		return new TurnLightOnEvent();
	}

	@SuppressWarnings("unused")
	public TurnLightOnEvent offEvent() {
		return new TurnLightOnEvent();
	}

	private class TurnLightOnEvent extends BEvent {
		private TurnLightOnEvent() {
			super("TurnLightOnEvent(" + getId() + ")");
		}
	}

	private class TurnLightOffEvent extends BEvent {
		private TurnLightOffEvent() {
			super("TurnLightOffEvent(" + getId() + ")");
		}
	}

	@SuppressWarnings("unused")
	public static class AnyTurnLightOffEvent implements EventSet {
		@Override
		public boolean contains(BEvent event) {
			return event instanceof TurnLightOffEvent;
		}
	}
}
