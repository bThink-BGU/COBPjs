package il.ac.bgu.cs.bp.bpjs.context;

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

public class ContextService {
	private static ContextService uniqInstance = new ContextService();
	@SuppressWarnings("unused")
	public static NativeFunction subscribe;
	private EntityManagerFactory emf;
	private EntityManager em;
	private ExecutorService pool;
	private BProgram bprog;
	private List<CtxType> contextTypes = new LinkedList<>();
	private BEvent[] contextEvents;

	private ContextService() {}

	@SuppressWarnings("unused")
	public static ContextService getInstance() {
		return uniqInstance;
	}

	private static class CtxType {
		String name;
		TypedQuery query;
		Class cls;
		List<?> activeContexts = new LinkedList<>();
	}

	private void persistObjects(Object ... objects) {
		if(objects == null)
			return;
		em.getTransaction().begin();
		for (Object o: objects) {
			em.merge(o);
		}
		em.getTransaction().commit();
		updateContexts();
	}

	public BProgram run(String... programs) {
		List<String> a = new ArrayList<>(Arrays.asList(programs));
		a.add(0,"context.js");
		bprog = new ResourceBProgram(a);

		//TODO: remove?
		MyPrioritizedBThreadsEventSelectionStrategy eventSelectionStrategy = new MyPrioritizedBThreadsEventSelectionStrategy();
		eventSelectionStrategy.setPriority("ContextReporterBT", 1000);
		bprog.setEventSelectionStrategy(eventSelectionStrategy);

		bprog.setWaitForExternalEvents(true);
		BProgramRunner rnr = new BProgramRunner(bprog);
		rnr.addListener(new PrintBProgramRunnerListener());
		rnr.addListener(new DBActuator());

		pool = Executors.newCachedThreadPool();
		pool.execute(rnr);
		pool.execute(new Runnable() {
			private int tick = 0;
			@Override
			public void run() {
				try {
					//noinspection InfiniteLoopStatement
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
	private void updateContexts() {
		Set<BEvent> events = new HashSet<>();
		contextTypes.forEach(ctxType -> {
			// Remember the list of contexts that we already reported of
			List<?> knownContexts = new LinkedList<>(ctxType.activeContexts);
			// Update the list of contexts
			ctxType.activeContexts = ctxType.query.getResultList();
			// Filter the contexts that we didn't yet report of
			List<?> newContexts = new LinkedList<>(ctxType.activeContexts);
            //noinspection SuspiciousMethodCalls
            newContexts.removeAll(knownContexts);
			// Compute the contexts that where just removed
			newContexts.stream().map(obj -> new NewContextEvent(ctxType.name, obj)).forEach(events::add);
			//noinspection SuspiciousMethodCalls
			knownContexts.removeAll(ctxType.activeContexts);
			knownContexts.stream().map(obj -> new ContextEndedEvent(ctxType.name, obj)).forEach(events::add);
		});
		contextEvents = events.toArray(new BEvent[0]);
	}

	// Produce context update events to be triggered after each update event
	@SuppressWarnings("unused")
	public BEvent[] getContextEvents() {
		return contextEvents;
	}

	/*public Object[] getContextsOfType(String type) {
		for (CtxType ct : contextTypes) {
			if (ct.name.equals(type)) {
				return ct.activeContexts.toArray();
			}
		}
		return null;
	}*/

	public <T> List<T> getContextsOfType(String type) {
		for (CtxType ct : contextTypes) {
			if (ct.name.equals(type)) {
				@SuppressWarnings("unchecked")
				List<T> l =  (List<T>) ct.activeContexts;
				return l;
			}
		}
		return null;
	}

	private HashMap<Class<?>, NamedQuery[]> findAllNamedQueries(EntityManagerFactory emf) {
		HashMap<Class<?>, NamedQuery[]> allNamedQueries = new HashMap<>();
		Set<ManagedType<?>> managedTypes = emf.getMetamodel().getManagedTypes();
		for (ManagedType<?> managedType: managedTypes) {
			if (managedType instanceof IdentifiableType) {
//				Class<? extends ManagedType> identifiableTypeClass = managedType.getClass();
//				@SuppressWarnings("rawtypes")
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

	private void registerContextQuery(String name, TypedQuery<?> query, Class<?> cls) {
		CtxType newType = new CtxType();
		newType.name = name;
		newType.cls = cls;
		newType.query = query;
		contextTypes.add(newType);
	}

	public void init(String persistenceUnit) {
		emf = Persistence.createEntityManagerFactory(persistenceUnit);
		em = emf.createEntityManager();
		HashMap<Class<?>, NamedQuery[]> queries = findAllNamedQueries(emf);
		for(Map.Entry<Class<?>, NamedQuery[]> entry : queries.entrySet()) {
			Class<?> key = entry.getKey();
			for (NamedQuery nq : entry.getValue()) {
				try {
					TypedQuery q = em.createNamedQuery(nq.name(), key);
					registerContextQuery(nq.name(), q, key);
				} catch (Exception ignored) { }
			}
		}
		updateContexts();
	}

	@SuppressWarnings("unused")
	public void close() {
		pool.shutdown();
		em.close();
		emf.close();
	}

	//region Internal Events
	@SuppressWarnings("WeakerAccess")
	public static class NewContextEvent extends BEvent {
		public final String contextName;
		public final Object ctx;

		public NewContextEvent(String contextName, Object ctx) {
			super("NewContextEvent(" + contextName + "," + ctx + ")");
			this.contextName = contextName;
			this.ctx = ctx;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class ContextEndedEvent extends BEvent {
		public String contextName;
		public Object ctx;

		public ContextEndedEvent(String contextName, Object ctx) {
			super("ContextEndedEvent(" + contextName + "," + ctx + ")");
			this.contextName = contextName;
			this.ctx = ctx;
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class InsertEvent extends BEvent {
		public final Object[] persistObjects;

		public InsertEvent(Object ... persistObjects) {
			super("InsertEvent(" + Arrays.toString(persistObjects) + ")");
			this.persistObjects = persistObjects;
		}

		void execute() {
		 	getInstance().persistObjects(persistObjects);
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class UpdateEvent extends BEvent {
		public final String query;
		public final Map<String, Object> parameters;

		public UpdateEvent(String query, Map<String, Object> parameters) {
			super("UpdateEvent(" + query + "," + parameters.entrySet() + ")");
			this.query = query;
			this.parameters = Collections.unmodifiableMap(parameters);
		}

		@SuppressWarnings("unused")
		public UpdateEvent(String query) {
			this(query, new HashMap<>());
		}

		void execute() {
			EntityManager em = getInstance().em;
			Query namedQuery = em.createNamedQuery(query);

			for (Map.Entry<String, Object> e : parameters.entrySet()) {
				namedQuery.setParameter(e.getKey(), e.getValue());
			}

			em.getTransaction().begin();
			namedQuery.executeUpdate();
			em.getTransaction().commit();

			getInstance().updateContexts();
		}
	}

	@SuppressWarnings("unused")
	public static class UnsubscribeEvent extends BEvent {
		public final String id;

		public UnsubscribeEvent(String id) {
			super("UnsubscribeEvent(" + id + ")");
			this.id = id;
		}
	}
	@SuppressWarnings("WeakerAccess")
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
	@SuppressWarnings({"WeakerAccess","unused"})
	public static class AnyNewContextEvent implements EventSet {
		public final String contextName;

		public AnyNewContextEvent(String contextName) {
			super();
			this.contextName = contextName;
		}

		@Override
		public boolean contains(BEvent event) {
			return (event instanceof NewContextEvent) && ((NewContextEvent) event).contextName.equals(contextName);
		}
	}

	@SuppressWarnings("unused")
	public static class AnyUpdateContextDBEvent implements EventSet {
		@Override
		public boolean contains(BEvent event) {
			return event instanceof UpdateEvent || event instanceof InsertEvent;
		}
	}

	@SuppressWarnings("unused")
	public static class AnyTickEvent implements EventSet {
		@Override
		public boolean contains(BEvent event) {
			return event instanceof TickEvent;
		}
	}
	//endregion
}
