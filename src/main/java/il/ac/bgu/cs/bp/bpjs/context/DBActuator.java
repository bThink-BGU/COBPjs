package il.ac.bgu.cs.bp.bpjs.context;

import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import il.ac.bgu.cs.bp.bpjs.context.events.UpdateContexDBEvent;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class DBActuator extends BProgramRunnerListenerAdapter {
	private EntityManager em;

	public DBActuator(EntityManager em) {
		super();
		this.em = em;
	}

	@Override
	public void eventSelected(BProgram bp, BEvent theEvent) {

		// Actuator for context update
		if (theEvent instanceof UpdateContexDBEvent) {
			UpdateContexDBEvent updCtxDbEvent = (UpdateContexDBEvent) theEvent;

			Query query = em.createNamedQuery(updCtxDbEvent.query);

			for (Entry<String, Object> e : updCtxDbEvent.parameters.entrySet()) {
				query.setParameter(e.getKey(), e.getValue());
			}

			em.getTransaction().begin();
			query.executeUpdate();
			em.getTransaction().commit();

			CTX.updateContexts();
		}
	}
}
