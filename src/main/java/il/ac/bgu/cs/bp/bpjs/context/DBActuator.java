package il.ac.bgu.cs.bp.bpjs.context;

import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

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
		if (theEvent instanceof CTX.UpdateEvent) {
			CTX.UpdateEvent updateEvent = (CTX.UpdateEvent) theEvent;
			Query query = em.createNamedQuery(updateEvent.query);

			for (Entry<String, Object> e : updateEvent.parameters.entrySet()) {
				query.setParameter(e.getKey(), e.getValue());
			}

			em.getTransaction().begin();
			query.executeUpdate();
			em.getTransaction().commit();

			CTX.updateContexts();
		}
	}
}
