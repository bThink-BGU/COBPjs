package il.ac.bgu.cs.bp.bpjs.context;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import il.ac.bgu.cs.bp.bpjs.context.events.ContextEndedEvent;
import il.ac.bgu.cs.bp.bpjs.context.events.NewContextEvent;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;

public class CTX {

	private static EntityManagerFactory emf;
	private static EntityManager em;

	private static class CtxType {
		String name;
		TypedQuery<?> query;
		List<?> activeContexts = new LinkedList<Object>();
	}

	private static List<CtxType> contextTypes = new LinkedList<CtxType>();
	private static BEvent[] contextEvents;

	public static void registerContextQuery(String name, Class<?> cls) {
		CtxType newType = new CtxType();
		newType.name = name;
		newType.query = em.createNamedQuery(name, cls);
		contextTypes.add(newType);
		
		// An easy, inefficient, way to initialize the new context 
		updateContexts();
	}

	public static BProgram run(String program) {
		BProgram bprog = new ResourceBProgram("context.js", program);

		MyPrioritizedBThreadsEventSelectionStrategy eventSelectionStrategy = new MyPrioritizedBThreadsEventSelectionStrategy();
		eventSelectionStrategy.setPriority("ContextReporterBT", 1000);

		bprog.setEventSelectionStrategy(eventSelectionStrategy);

		bprog.setWaitForExternalEvents(true);

		BProgramRunner rnr = new BProgramRunner(bprog);
		rnr.addListener(new PrintBProgramRunnerListener());

		rnr.addListener(new DBActuator(em));
		
		Thread thread = new Thread(rnr);
		thread.start();

		return bprog;
	}

	// Produce context update events to be triggered after each update event
	public static void updateContexts() {
		Set<BEvent> events = new HashSet<BEvent>();

		for (CtxType ctxType : contextTypes) {
			// Remember the list of contexts that we already reported of
			List<?> knownContexts = new LinkedList<Object>(ctxType.activeContexts);

			// Update the list of contexts
			ctxType.activeContexts = ctxType.query.getResultList();

			// Filter the contexts that we didn't yet report of
			List<Object> newContexts = new LinkedList<Object>(ctxType.activeContexts);
			newContexts.removeAll(knownContexts);

			newContexts.stream().map(obj -> new NewContextEvent(ctxType.name, obj)).forEach(e -> events.add(e));

			// Compute the contexts that where just removed
			knownContexts.removeAll(ctxType.activeContexts);

			knownContexts.stream().map(obj -> new ContextEndedEvent(ctxType.name, obj)).forEach(e -> events.add(e));
		}

		contextEvents = events.stream().toArray(BEvent[]::new);
	}

	// Produce context update events to be triggered after each update event
	public static BEvent[] getContextEvents() {
		return contextEvents;
	}

	public static Object[] getContextsOfType(String type) {
		for (CtxType ct : contextTypes) {
			if (ct.name.equals(type)) {
				return ct.activeContexts.stream().toArray(Object[]::new);
			}
		}
		return null;
	}

	public static void init(Consumer<EntityManager> f) {
		emf = Persistence.createEntityManagerFactory("ContextDB");
		em = emf.createEntityManager();

		f.accept(em);
	}

	public static void close() {
		em.close();
		emf.close();
	}

}
