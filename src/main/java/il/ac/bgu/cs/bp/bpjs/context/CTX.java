package il.ac.bgu.cs.bp.bpjs.context;

import java.util.*;
import java.util.function.Consumer;

import javax.persistence.*;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import org.mozilla.javascript.NativeFunction;

public class CTX {
	public static NativeFunction subscribe;
	private static EntityManagerFactory emf;
	private static EntityManager em;

	private static class CtxType {
		String name;
		TypedQuery<?> query;
		Class<?> cls;
		List<?> activeContexts = new LinkedList<Object>();
	}
	private static List<CtxType> contextTypes = new LinkedList<CtxType>();
	private static BEvent[] contextEvents;

	public static void registerContextQuery(String name, Class<?> cls) {
		CtxType newType = new CtxType();
		newType.name = name;
		newType.cls = cls;
		newType.query = em.createNamedQuery(name, cls);
		contextTypes.add(newType);
		
		// An easy, inefficient, way to initialize the new context 
		updateContexts();
	}

	public static void populateDB(Collection<?> data) {
		em.getTransaction().begin();
		for (Object o: data) {
			em.merge(o);
		}
		em.getTransaction().commit();
		CTX.updateContexts();
	}

	public static void persistObject(Object o) {
		em.getTransaction().begin();
		em.merge(o);
		em.getTransaction().commit();
		CTX.updateContexts();
	}

	public static BProgram run(String... programs) {
		List<String> a = new ArrayList<>(List.of(programs));
		a.add(0,"context.js");
		BProgram bprog = new ResourceBProgram(a);

		//TODO: remove?
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

	public static void init() {
		emf = Persistence.createEntityManagerFactory("ContextDB");
		em = emf.createEntityManager();
	}

	public static void close() {
		em.close();
		emf.close();
	}

	//region Internal Events
	public static class NewContextEvent extends BEvent {
		public String contextName;
		public Object ctx;
		public int col;

		public NewContextEvent(String contextName, Object ctx) {
			super("NewContextEvent(" + contextName + "," + ctx + ")");
			this.contextName = contextName;
			this.ctx = ctx;
		}
	}

	public static class ContextEndedEvent extends BEvent {
		public String contextName;
		public Object ctx;

		public ContextEndedEvent(String contextName, Object ctx) {
			super("ContextEndedEvent(" + contextName + "," + ctx + ")");
			this.contextName = contextName;
			this.ctx = ctx;
		}
	}

	public static class InsertEvent extends BEvent {
		public final Object persistObject;

		public InsertEvent(Object persistObject) {
			super("InsertEvent(" + persistObject + ")");
			this.persistObject = persistObject;
		}
	}

	public static class PopulateEvent extends BEvent {
		public final List<?> persistObjects;

		public PopulateEvent(List<?> persistObjects) {
			super("PopulateEvent(" + persistObjects + ")");
			this.persistObjects = persistObjects;
		}
	}

	public static class UpdateEvent extends BEvent {
		public final String query;
		public Map<String, Object> parameters;

		public UpdateEvent(String query, Map<String, Object> parameters) {
			super("UpdateEvent(" + query + "," + parameters.entrySet() + ")");
			this.query = query;
			this.parameters = parameters;
		}

		public UpdateEvent(String query) {
			this(query, new HashMap<String, Object>());
		}
	}

	public static class UnsubscribeEvent extends BEvent {
		public final String id;

		public UnsubscribeEvent(String id) {
			super("UnsubscribeEvent(" + id + ")");
			this.id = id;
		}
	}
	//endregion

	//region Internal EventSets
	public static class AnyNewContextEvent implements EventSet {
		public String contextName;

		public AnyNewContextEvent(String contextName) {
			super();
			this.contextName = contextName;
		}

		@Override
		public boolean contains(BEvent event) {
			return (event instanceof NewContextEvent) && ((NewContextEvent) event).contextName.equals(contextName);
		}
	}

	public static class AnyUpdateContextDBEvent implements EventSet {
		@Override
		public boolean contains(BEvent event) {
			return
					event instanceof UpdateEvent ||
							event instanceof InsertEvent ||
							event instanceof PopulateEvent;
		}
	}
	//endregion
}
