package il.ac.bgu.cs.bp.bpjs.context;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.*;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;

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
	private static ExecutorService pool;
	private static BProgramRunner rnr;
	private static BProgram bprog;

	private static class CtxType<T> {
		String name;
		TypedQuery<T> query;
		Class<T> cls;
		List<T> activeContexts = new LinkedList<>();
	}
	private static List<CtxType<?>> contextTypes = new LinkedList<>();
	private static BEvent[] contextEvents;

	public static void persistObjects(Object[] objects) {
		em.getTransaction().begin();
		for (Object o: objects) {
			em.merge(o);
		}
		em.getTransaction().commit();
		CTX.updateContexts();
	}

	public static BProgram run(String... programs) {
		List<String> a = new ArrayList<>(List.of(programs));
		a.add(0,"context.js");
		bprog = new ResourceBProgram(a);

		//TODO: remove?
		MyPrioritizedBThreadsEventSelectionStrategy eventSelectionStrategy = new MyPrioritizedBThreadsEventSelectionStrategy();
		eventSelectionStrategy.setPriority("ContextReporterBT", 1000);
		bprog.setEventSelectionStrategy(eventSelectionStrategy);

		bprog.setWaitForExternalEvents(true);
		rnr = new BProgramRunner(bprog);
		rnr.addListener(new PrintBProgramRunnerListener());
		rnr.addListener(new DBActuator(em));

		pool = Executors.newFixedThreadPool(2);
		pool.execute(rnr);
		pool.execute(new Runnable() {
			private int tick = 0;
			@Override
			public void run() {
				try {
					while(true) {
						Thread.sleep(1000);
						bprog.enqueueExternalEvent(new TickEvent(++tick));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
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

	public static <T> List<T> getContextsOfType(String type, Class<T> clz) {
		for (CtxType ct : contextTypes) {
			if (ct.name.equals(type)) {
				return ct.activeContexts;
			}
		}
		return null;
	}

	private static HashMap<Class<?>, NamedQuery[]> findAllNamedQueries(EntityManagerFactory emf) {
		HashMap<Class<?>, NamedQuery[]> allNamedQueries = new HashMap<>();
		Set<ManagedType<?>> managedTypes = emf.getMetamodel().getManagedTypes();
		for (ManagedType<?> managedType: managedTypes) {
			if (managedType instanceof IdentifiableType) {
				@SuppressWarnings("rawtypes")
//				Class<? extends ManagedType> identifiableTypeClass = managedType.getClass();
				Class<?> javaClass = managedType.getJavaType();
				NamedQueries namedQueries = javaClass.getAnnotation(NamedQueries.class);
				if (namedQueries != null) {
					allNamedQueries.put(managedType.getJavaType(),namedQueries.value());
				}

				NamedQuery namedQuery = javaClass.getAnnotation(NamedQuery.class);
				if (namedQuery != null) {
					allNamedQueries.put(managedType.getJavaType(), new NamedQuery[]{namedQuery});
				}
			}
		}
		return allNamedQueries;
	}

	private static <T> void registerContextQuery(String name, TypedQuery<T> query, Class<T> cls) {
		CtxType<T> newType = new CtxType<>();
		newType.name = name;
		newType.cls = cls;
		newType.query = query;
		contextTypes.add(newType);
	}

	public static void init(String persistenceUnit) {
		emf = Persistence.createEntityManagerFactory(persistenceUnit);
		em = emf.createEntityManager();
		HashMap<Class<?>, NamedQuery[]> queries = findAllNamedQueries(emf);
		for(Map.Entry<Class<?>, NamedQuery[]> entry : queries.entrySet()) {
			Class<?> key = entry.getKey();
			Type p = key.getGenericSuperclass();
			for (NamedQuery nq : entry.getValue()) {
				try {
					TypedQuery q = em.createNamedQuery(nq.name(), key);
					registerContextQuery(nq.name(), q, key);
				} catch (Exception e) {

				}
			}
		}
		updateContexts();
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
		public final Object[] persistObjects;

		public InsertEvent(Object ... persistObjects) {
			super("InsertEvent(" + persistObjects + ")");
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
	public static class TickEvent extends BEvent {
		public final int tick;
		public TickEvent(int tick) {
			super("Tick");
			this.tick = tick;
		}
		@Override
		public String toString() {
			return "Tick: " + tick;
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
			return event instanceof UpdateEvent || event instanceof InsertEvent;
		}
	}

	public static class AnyTickEvent implements EventSet {
		@Override
		public boolean contains(BEvent event) {
			return event instanceof TickEvent;
		}
	}
	//endregion
}
