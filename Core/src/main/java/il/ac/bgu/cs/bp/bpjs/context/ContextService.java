package il.ac.bgu.cs.bp.bpjs.context;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import com.google.common.collect.*;
import il.ac.bgu.cs.bp.bpjs.context.eventselection.ContextualEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.context.eventselection.PrioritizedBSyncEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import org.hibernate.Session;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;

public class ContextService implements Serializable {
	private static AtomicInteger counter = new AtomicInteger(0);
	private static final ContextService uniqInstance = new ContextService();
	@SuppressWarnings("unused")
	public static NativeFunction subscribe;
	@SuppressWarnings("unused")
	public static NativeFunction subscribeWithParameters;
	private EntityManagerFactory emf;
	private final Collection<EntityManagerCreateHook> entityManagerCreateHooks = new ConcurrentLinkedDeque<>();
	private ExecutorService pool;
	private BProgram bprog;
	private BProgramRunner rnr;
	private Multimap<Class<?>, NamedQuery> namedQueries;
	private boolean verificationMode = false;
	private Collection<CtxType<?>> contextTypes;
	private ContextInternalEvent contextEvents;

	private static class ContextServiceProxy implements Serializable {
		private Collection<CtxType<?>> contextTypes;
		private ContextInternalEvent contextEvents;
		private List<String> dbDump = new LinkedList<>();

		private ContextServiceProxy() { }

		public ContextServiceProxy(ContextService cs) {
			this.contextTypes = cs.contextTypes;
			this.contextEvents = cs.contextEvents;
			this.dbDump = new LinkedList<>();
			EntityManager em = cs.createEntityManager();
			Session session = em.unwrap(Session.class);
			session.doWork(connection -> {
				em.getMetamodel().getEntities()
						.forEach(e -> dbDump.addAll(Db2Sql.dumpTable(connection, e.getName())));
			});
		}

		private Object readResolve() throws ObjectStreamException {
			uniqInstance.contextEvents = contextEvents;
			uniqInstance.contextTypes = contextTypes;

			EntityManager em = uniqInstance.createEntityManager();
			em.getTransaction().begin();
			em.getMetamodel().getEntities()
					.forEach(e -> em.createQuery("Delete from " + e.getName() + " e").executeUpdate());
			dbDump.forEach(s -> em.createNativeQuery(s).executeUpdate());
			em.getTransaction().commit();
			return uniqInstance;
		}
	}

	private ContextService() {
	}

	public void enableVerificationMode() {
		if (bprog != null) {
			throw new IllegalStateException(
					"Setting verification mode must be done before calling initFromResources or initFromString");
		}
		this.verificationMode = true;
	}

	@SuppressWarnings("unused")
	public static ContextService getInstance() {
		return uniqInstance;
	}

	@SuppressWarnings("unused")
	public static <T> List<T> getContextInstances(String contextName, Class<T> contextClass) {
		return uniqInstance.innerGetContextInstances(contextName, contextClass);
	}

	@SuppressWarnings("unused")
	public static <T> List<T> getContextInstances(String contextName) {
		return uniqInstance.innerGetContextInstances(contextName, null);
	}

	private EntityManager createEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.setFlushMode(FlushModeType.COMMIT);
		entityManagerCreateHooks.forEach(h -> h.hook(em));
		return em;
	}

	public void addEntityManagerCreateHook(EntityManagerCreateHook hook) {
		entityManagerCreateHooks.add(hook);
	}

	private static class CtxType<T> implements Serializable {
		private static final long serialVersionUID = -1633878308592722931L;

		// transient TypedQuery query;
		final String queryName;
		final String uniqueId;
		final Class<T> cls;
		final Map<String, ?> parameters;
		List<T> activeContexts = new LinkedList<>();

		public CtxType(String queryName, String uniqueId, Class<T> cls,
				@Nullable Map<String, ?> parameters) {
			this.queryName = queryName;
			this.uniqueId = uniqueId;
			this.cls = cls;
			this.parameters = parameters;
			// this.query = createQuery(queryName, cls, parameters);
		}

		private void readObject(ObjectInputStream aInputStream)
				throws ClassNotFoundException, IOException {
			// perform the default de-serialization first
			aInputStream.defaultReadObject();

			// query = createQuery(queryName, cls, parameters);
		}

		private TypedQuery<T> createQuery() {
			TypedQuery<T> q = uniqInstance.createEntityManager().createNamedQuery(queryName, cls);
			q.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
			ContextService.setParameters(q, parameters);
			return q;
		}

		private void updateActive() {
			this.activeContexts = this.createQuery().getResultList();
		}
	}

	private static void setParameters(Query q, Map<String, ?> parameters) {
		if (parameters != null) {
			parameters.forEach((key, val) -> {
				Object v = val;
				if (v instanceof Number) {
					Number number = (Number) v;
					String typeName = q.getParameter(key).getParameterType().getName();
					switch (typeName) {
						case "java.lang.Integer":
							v = number.intValue();
							break;
						case "java.lang.Long":
							v = number.longValue();
							break;
						case "java.lang.Byte":
							v = number.byteValue();
							break;
						case "java.lang.Short":
							v = number.shortValue();
							break;
						case "java.lang.Float":
							v = number.floatValue();
							break;
					}
				}
				q.setParameter(key, v);
			});
		}
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private Object writeReplace() throws ObjectStreamException {
		return new ContextServiceProxy(this);
	}

	public void enableTicker() {
		pool.execute(new Runnable() {
			private int tick = 0;

			@Override
			public void run() {
				try {
					// noinspection InfiniteLoopStatement
					while (true) {
						Thread.sleep(1000);
						bprog.enqueueExternalEvent(new TickEvent(++tick));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public BProgram getBProgram() {
		return bprog;
	}

	@SuppressWarnings("WeakerAccess")
	public void addListener(BProgramRunnerListener listener) {
		if (rnr != null)
			rnr.addListener(listener);
	}

	@SuppressWarnings("unused")
	public void initFromString(String persistenceUnit, String program) {
		bprog = new ResourceBProgram("context.js");
		bprog.appendSource(program);

		init(persistenceUnit);
	}

	public void initFromResources(String persistenceUnit, String... programs) {
		List<String> a = new ArrayList<>(Arrays.asList(programs));
		a.add(0, "context.js");
		if (verificationMode) {
			a.add("internal_context_verification.js");
		}
		bprog = new ResourceBProgram(a);

		init(persistenceUnit);
	}

	private void init(String persistenceUnit) {
		close();
		contextTypes = new ConcurrentLinkedDeque<>();
		pool = Executors.newFixedThreadPool(2);
		emf = Persistence.createEntityManagerFactory(persistenceUnit, ImmutableMap
				.builderWithExpectedSize(1).put("javax.persistence.sharedCache.mode", "NONE").build());
		namedQueries = findAllNamedQueries(emf);
		namedQueries.forEach((aClass, namedQuery) -> {
			try {
				TypedQuery<?> q = createEntityManager().createNamedQuery(namedQuery.name(), aClass);
				Set<Parameter<?>> parameters = q.getParameters();
				if (parameters == null || parameters.isEmpty()) {
					registerContextQuery(namedQuery.name(), namedQuery.name(), aClass, null);
				}
			} catch (Exception ignored) {
			}
		});

		ContextualEventSelectionStrategy eventSelectionStrategy =
				new PrioritizedBSyncEventSelectionStrategy();
		eventSelectionStrategy.setPriority("ContextReporterBT", 1000);
		eventSelectionStrategy.setPriority("PopulateDB", 999);

		bprog.setEventSelectionStrategy(eventSelectionStrategy);
		bprog.setWaitForExternalEvents(true);

		rnr = new BProgramRunner(bprog);
		addListener(new PrintBProgramRunnerListener());
		addListener(new DBActuator());
	}

	public void run() {

		try {
			pool.execute(rnr);
		} catch (Exception e) {
			// ignored in case the bprogram has been stopped.
		}
	}

	// Produce contextName update events to be triggered after each update event
	private synchronized void updateContexts() {
		Set<ContextInternalEventData> events = new HashSet<>();
		contextTypes.forEach(ctxType -> {
			// Remember the list of contexts that we already reported of
			List<?> knownContexts = new LinkedList<>(ctxType.activeContexts);
			// Update the list of contexts
			ctxType.updateActive();

			// Filter the contexts that we didn't yet report of
			List<?> newContexts = new LinkedList<>(ctxType.activeContexts);
			// noinspection SuspiciousMethodCalls
			newContexts.removeAll(knownContexts);
			// Compute the contexts that where just removed
			newContexts.stream().map(obj -> new ContextInternalEventData(ContextEventType.NEW, ctxType.uniqueId, obj))
					.forEach(events::add);
			// noinspection SuspiciousMethodCalls
			knownContexts.removeAll(ctxType.activeContexts);
			knownContexts.stream().map(obj -> new ContextInternalEventData(ContextEventType.ENDED, ctxType.uniqueId, obj))
					.forEach(events::add);
		});
		contextEvents = new ContextInternalEvent(events);
	}

	public static ContextInternalEvent getContextEvents() {
		return getInstance().innerGetContextEvents();
	}

	// Produce contextName update events to be triggered after each update event
	private ContextInternalEvent innerGetContextEvents() {
		return contextEvents;
	}

	@SuppressWarnings("WeakerAccess")
	private <T> List<T> innerGetContextInstances(String contextName, Class<T> contextClass) {
		for (CtxType ct : contextTypes) {
			if (ct.uniqueId.equals(contextName)) {
				return ct.activeContexts;
			}
		}
		return null;
	}

	private static Multimap<Class<?>, NamedQuery> findAllNamedQueries(EntityManagerFactory emf) {
		Multimap<Class<?>, NamedQuery> namedQueries = ArrayListMultimap.create();
		Set<ManagedType<?>> managedTypes = emf.getMetamodel().getManagedTypes();
		for (ManagedType<?> managedType : managedTypes) {
			if (managedType instanceof IdentifiableType) {
				// Class<? extends ManagedType> identifiableTypeClass = managedType.getClass();
				// @SuppressWarnings("rawtypes")
				Class<?> javaClass = managedType.getJavaType();
				NamedQueries namedQueriesList = javaClass.getAnnotation(NamedQueries.class);
				if (namedQueriesList != null) {
					namedQueries.putAll(javaClass, Arrays.asList(namedQueriesList.value()));
				}

				NamedQuery namedQuery = javaClass.getAnnotation(NamedQuery.class);
				if (namedQuery != null) {
					namedQueries.put(javaClass, namedQuery);
				}
			}
		}
		return ImmutableMultimap.copyOf(namedQueries);
	}

	private <T> void registerContextQuery(String queryName, String uniqueId, Class<T> cls,
			@Nullable Map<String, ?> parameters) {
		contextTypes.add(new CtxType<>(queryName, uniqueId, cls, parameters));
		updateContexts();
	}

	public static void registerParameterizedContextQuery(String queryName, String uniqueId,
			NativeObject params) {
		getInstance().innerRegisterParameterizedContextQuery(queryName, uniqueId, params);
	}

	@SuppressWarnings("unused")
	private void innerRegisterParameterizedContextQuery(String queryName, String uniqueId,
			NativeObject params) {
		namedQueries.forEach((aClass, namedQuery) -> {
			if (namedQuery.name().equals(queryName)) {
				registerContextQuery(queryName, uniqueId, aClass, params);
			}
		});
	}

	@SuppressWarnings("unused")
	public void close() {
		try {
			pool.shutdownNow();
			emf.close();
		} catch (Exception e) {
		}
	}

	// region Internal Events
  public static class ContextInternalEvent extends BEvent {
		private static final long serialVersionUID = 3165975196124148981L;
		private static final AtomicInteger counter = new AtomicInteger(0);
		public final Set<ContextInternalEventData> events;
		
		private ContextInternalEvent(Set<ContextInternalEventData> events) {
			super("ContextInternalEvent_" + counter.incrementAndGet());
			this.events = events;
		}

		public ContextInternalEventData[] newContexts(String contextName) {
			ContextInternalEventData[] array = events.stream()
					.filter(e -> (e.type.equals(ContextEventType.NEW) && e.contextName.equals(contextName)))
					.toArray(ContextInternalEventData[]::new);
			return array;
		}

		public ContextInternalEventData[] endedContexts(String contextName) {
			ContextInternalEventData[] array = events.stream()
					.filter(e -> (e.type.equals(ContextEventType.ENDED) && e.contextName.equals(contextName)))
					.toArray(ContextInternalEventData[]::new);
			return array;
		}

		@Override
		public int hashCode() {
			return events.hashCode();
		}

		@Override
		public String toString() {
			return "ContextInternalEvent_" + events.toString();
		}

		@Override
    public boolean equals(Object obj) {
			if (this == obj) {
				return true;
		}
		if (obj == null) {
				return false;
		}
		if (!getClass().isInstance(obj)) {
				return false;
		}
		ContextInternalEvent other = (ContextInternalEvent) obj;
			return events.equals(other.events);
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class ContextInternalEventData implements Serializable {
		private static final long serialVersionUID = 8036020413270769137L;
		public final ContextEventType type;
		public final String contextName;
		public final Object ctx;

		private ContextInternalEventData(ContextEventType type, String contextName, Object ctx) {
			this.type = type;
			this.contextName = contextName;
			this.ctx = ctx;
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, contextName, ctx);
		}

		@Override
		public String toString() {
			return String.format("{\"type\":%s , \"name\":%s , \"ctx\":%s}", type.toString(), contextName,
					ctx.toString());
		}

		/*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isInstance(obj)) {
            return false;
        }
        ContextInternalEventData other = (ContextInternalEventData) obj;
        return type.equals(other.type) && contextName.equals(other.contextName) && ctx.equals(other.ctx);
    }
	}

	@SuppressWarnings("WeakerAccess")
	public static abstract class CommandEvent extends BEvent {
		public CommandEvent(String name) {
			super(name);
		}

		public final void execute() {
			EntityManager em = getInstance().createEntityManager();
			em.getTransaction().begin();
			innerExecution(em);
			em.getTransaction().commit();
			getInstance().updateContexts();
		}

		protected abstract void innerExecution(EntityManager em);
	}

	@SuppressWarnings("unused")
	public static final class TransactionEvent extends CommandEvent {
		public final CommandEvent[] commands;

		public TransactionEvent(CommandEvent... commands) {
			super("TransactionEvent [ " + Arrays.toString(commands) + " ]");
			this.commands = commands;
		}

		@Override
		protected void innerExecution(EntityManager em) {
			for (CommandEvent e : commands) {
				e.innerExecution(em);
			}
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class InsertEvent extends CommandEvent {
		public final Object[] persistObjects;

		public InsertEvent(Object... persistObjects) {
			super("InsertEvent(" + Arrays.toString(persistObjects) + ")");
			this.persistObjects = persistObjects;
		}

		@Override
		protected void innerExecution(EntityManager em) {
			persistObjects(em, persistObjects);
		}

		private void persistObjects(EntityManager em, Object... objects) {
			if (objects == null)
				return;
			for (Object o : objects) {
				em.merge(o);
			}
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class UpdateEvent extends CommandEvent {
		public final String contextName;
		public final Map<String, Object> parameters;

		public UpdateEvent(String contextName, Map<String, Object> parameters) {
			super("UpdateEvent(" + contextName + "," + parameters.entrySet() + ")");
			this.contextName = contextName;
			this.parameters = Collections.unmodifiableMap(parameters);
		}

		@SuppressWarnings("unused")
		public UpdateEvent(String contextName) {
			this(contextName, new HashMap<>());
		}

		@Override
		protected void innerExecution(EntityManager em) {
			Query q = em.createNamedQuery(contextName);
			ContextService.setParameters(q, parameters);
			q.executeUpdate();
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
	// endregion

	// region Internal EventSets
	@SuppressWarnings({"WeakerAccess", "unused"})
	public static class AnyNewContextEvent implements EventSet {
		private static final long serialVersionUID = -8858955086405859047L;
    public final String contextName;
		public final Object ctx;

		public AnyNewContextEvent(String contextName) {
			this(contextName, null);
		}

		public AnyNewContextEvent(String contextName, Object ctx) {
			super();
			this.contextName = contextName;
			this.ctx = ctx;
		}

		@Override
		public boolean contains(BEvent event) {
			if (!(event instanceof ContextInternalEvent))
				return false;
			ContextInternalEvent internal = (ContextInternalEvent) event;
			return internal.events.stream().anyMatch(e -> (
				e.type.equals(ContextEventType.NEW)
					&& e.contextName.equals(contextName) && (ctx == null || ctx.equals(e.ctx))
			));
		}
	}

	@SuppressWarnings({"WeakerAccess", "unused"})
	public static class AnyContextEndedEvent implements EventSet {
		private static final long serialVersionUID = 5402960437353425951L;
    public final String contextName;
		public final Object ctx;

		public AnyContextEndedEvent(String contextName) {
			this(contextName,null);
		}

		public AnyContextEndedEvent(String contextName, Object context) {
			super();
			this.contextName = contextName;
			this.ctx = context;
		}

		@Override
		public boolean contains(BEvent event) {
			if (!(event instanceof ContextInternalEvent))
				return false;
			ContextInternalEvent internal = (ContextInternalEvent) event;
			return internal.events.stream().anyMatch(e -> (e.type == ContextEventType.ENDED
					&& e.contextName.equals(contextName) && (ctx == null || ctx.equals(e.ctx))));
		}
	}


	@SuppressWarnings({"WeakerAccess", "unused"})
	public static class AnyContextCommandEvent implements EventSet {
		@Override
		public boolean contains(BEvent event) {
			boolean b = event instanceof CommandEvent;
			return b;
		}
	}

	@SuppressWarnings("unused")
	public static class AnyTickEvent implements EventSet {
		@Override
		public boolean contains(BEvent event) {
			return event instanceof TickEvent;
		}
	}

	public enum ContextEventType {
		NEW, ENDED
	}
	// endregion
}
