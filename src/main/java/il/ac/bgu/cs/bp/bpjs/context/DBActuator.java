package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class DBActuator extends BProgramRunnerListenerAdapter {
	@Override
	public void eventSelected(BProgram bp, BEvent theEvent) {
		// Actuator for context update
		if (theEvent instanceof ContextService.UpdateEvent) {
			ContextService.UpdateEvent updateEvent = (ContextService.UpdateEvent) theEvent;
			updateEvent.execute();
		} else if (theEvent instanceof ContextService.InsertEvent) {
			((ContextService.InsertEvent) theEvent).execute();
		}
	}
}
