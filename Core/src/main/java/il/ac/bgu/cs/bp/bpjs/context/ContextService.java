package il.ac.bgu.cs.bp.bpjs.context;

import java.io.*;
import java.util.*;
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
    private transient EntityManagerFactory emf;
    private transient EntityManager em;
    private transient ExecutorService pool;
    private transient BProgram bprog;
    private transient BProgramRunner rnr;
    private transient Multimap<Class<?>, NamedQuery> namedQueries;
	private transient boolean verificationMode = false;
	private List<CtxType> contextTypes;
	private BEvent[] contextEvents;
	private List<String> dbDump = new LinkedList<>();

	private ContextService() { }

	public void enableVerificationMode() {
		if(bprog != null) {
			throw new IllegalStateException("Setting verification mode must be done before calling initFromResources or initFromString");
		}
		this.verificationMode = true;
	}

	@SuppressWarnings("unused")
	public static ContextService getInstance() {
		return uniqInstance;
	}

	@SuppressWarnings("unused")
	public static <T> List<T> getContextsOfType(String type) {
		return uniqInstance.getContextInstances(type);
	}

	private static class CtxType implements Serializable {
        transient TypedQuery query;
        final String queryName;
        final String uniqueId;
        final Class cls;
        final Map<String,?> parameters;
        List<?> activeContexts = new LinkedList<>();

		public CtxType(String queryName, String uniqueId, Class cls, @Nullable Map<String,?> parameters){
			this.queryName = queryName;
			this.uniqueId = uniqueId;
			this.cls = cls;
            this.parameters = parameters;
            this.query = createQuery(queryName, cls, parameters);
        }

        private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
            // perform the default de-serialization first
            aInputStream.defaultReadObject();

            query = createQuery(queryName, cls, parameters);
        }

        /*private Object readResolve() throws ObjectStreamException {
		    query = createQuery(queryName, cls, parameters);
		    return this;
        }*/

        private static TypedQuery createQuery(String name, Class cls, @Nullable Map<String,?> params) {
            TypedQuery q = uniqInstance.em.createNamedQuery(name, cls);
            if(params != null) {
                params.forEach((key, val) -> {
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
            return q;
        }
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private Object readResolve() throws ObjectStreamException {
        int c = counter.incrementAndGet();

		uniqInstance.contextEvents = contextEvents;
		uniqInstance.contextTypes = contextTypes;

		uniqInstance.em.getTransaction().begin();
		uniqInstance.em.getMetamodel().getEntities()
				.forEach(e -> uniqInstance.em.createQuery("Delete from "+e.getName()+" e").executeUpdate());
		dbDump.forEach(s -> uniqInstance.em.createNativeQuery(s).executeUpdate());
		uniqInstance.em.getTransaction().commit();
		return uniqInstance;
	}

//    private void writeObject(ObjectOutputStream out) throws IOException {
    private Object writeReplace() throws ObjectStreamException {
	    dbDump = new LinkedList<>();
        Session session = uniqInstance.em.unwrap(Session.class);
        session.doWork(connection -> {
            uniqInstance.em.getMetamodel().getEntities().forEach(e -> dbDump.addAll(Db2Sql.dumpTable(connection, e.getName())));
		});
        if(this!=uniqInstance){
            System.out.println("counter is "+counter.get());
            uniqInstance.dbDump = dbDump;
            uniqInstance.contextEvents = contextEvents;
            uniqInstance.contextTypes = contextTypes;
        }
		return uniqInstance;
//        out.defaultWriteObject();
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

	public void enableTicker() {
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
	}

	public BProgram getBProgram() {
		return bprog;
	}

	@SuppressWarnings("WeakerAccess")
	public void addListener(BProgramRunnerListener listener) {
		if(rnr!=null)
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
		if(verificationMode) {
			a.add("internal_context_verification.js");
		}
		bprog = new ResourceBProgram(a);

		init(persistenceUnit);
	}

	private void init(String persistenceUnit) {
	    close();
		contextTypes = new LinkedList<>();
        pool = Executors.newCachedThreadPool();
        emf = Persistence.createEntityManagerFactory(persistenceUnit);
        em = emf.createEntityManager();
        namedQueries = findAllNamedQueries(emf);
        namedQueries.forEach((aClass, namedQuery) -> {
			try {
				TypedQuery<?> q = em.createNamedQuery(namedQuery.name(), aClass);
				Set<Parameter<?>> parameters = q.getParameters();
				if (parameters == null || parameters.isEmpty()) {
					registerContextQuery(namedQuery.name(), namedQuery.name(), aClass, null);
				}
			} catch (Exception ignored) { }
		});

		ContextualEventSelectionStrategy eventSelectionStrategy = new PrioritizedBSyncEventSelectionStrategy();
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
            newContexts.stream().map(obj -> new NewContextEvent(ctxType.uniqueId, obj)).forEach(events::add);
            //noinspection SuspiciousMethodCalls
            knownContexts.removeAll(ctxType.activeContexts);
            knownContexts.stream().map(obj -> new ContextEndedEvent(ctxType.uniqueId, obj)).forEach(events::add);
        });
		contextEvents = events.toArray(new BEvent[0]);
	}


    public static BEvent[] getContextEvents() {
        return getInstance().innerGetContextEvents();
    }

	// Produce contextName update events to be triggered after each update event
	private BEvent[] innerGetContextEvents() {
		return contextEvents;
	}

    @SuppressWarnings("WeakerAccess")
	public <T> List<T> getContextInstances(String type) {
        for (CtxType ct : contextTypes) {
            if (ct.uniqueId.equals(type)) {
                @SuppressWarnings("unchecked")
                List<T> l =  (List<T>) ct.activeContexts;
                return l;
            }
        }
        return null;
	}

	private static Multimap<Class<?>, NamedQuery> findAllNamedQueries(EntityManagerFactory emf) {
        Multimap<Class<?>, NamedQuery> namedQueries = ArrayListMultimap.create();
		Set<ManagedType<?>> managedTypes = emf.getMetamodel().getManagedTypes();
		for (ManagedType<?> managedType: managedTypes) {
			if (managedType instanceof IdentifiableType) {
//				Class<? extends ManagedType> identifiableTypeClass = managedType.getClass();
//				@SuppressWarnings("rawtypes")
				Class<?> javaClass = managedType.getJavaType();
				NamedQueries namedQueriesList = javaClass.getAnnotation(NamedQueries.class);
				if (namedQueriesList != null) {
					namedQueries.putAll(javaClass,Arrays.asList(namedQueriesList.value()));
				}

				NamedQuery namedQuery = javaClass.getAnnotation(NamedQuery.class);
				if (namedQuery != null) {
					namedQueries.put(javaClass, namedQuery);
				}
			}
		}
		return ImmutableMultimap.copyOf(namedQueries);
	}

	private void registerContextQuery(String queryName, String uniqueId, Class<?> cls, @Nullable Map<String, ?> parameters) {
		contextTypes.add(new CtxType(queryName, uniqueId, cls, parameters));
		updateContexts();
	}

	public static void registerParameterizedContextQuery(String queryName, String uniqueId, NativeObject params) {
	    getInstance().innerRegisterParameterizedContextQuery(queryName, uniqueId, params);
    }

	@SuppressWarnings("unused")
	private void innerRegisterParameterizedContextQuery(String queryName, String uniqueId, NativeObject params) {
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
            em.close();
            emf.close();
        } catch (Exception e) { }
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

		public void execute() {
			getInstance().persistObjects(persistObjects);
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class UpdateEvent extends BEvent {
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

        public void execute() {
			EntityManager em = getInstance().em;
			Query namedQuery = em.createNamedQuery(contextName);

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

    @SuppressWarnings({"WeakerAccess","unused"})
	public static class AnyUpdateContextDBEvent implements EventSet {
		public final String contextName;

		public AnyUpdateContextDBEvent() {
			super();
			contextName = null;
		}

		public AnyUpdateContextDBEvent(String contextName) {
			super();
			this.contextName = contextName;
		}

		@Override
		public boolean contains(BEvent event) {
			if(contextName != null) {
				return (event instanceof UpdateEvent && ((UpdateEvent) event).contextName.equals(contextName)) || (event instanceof InsertEvent);
			} else {
				return (event instanceof UpdateEvent) || (event instanceof InsertEvent);
			}
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
