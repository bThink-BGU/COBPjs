package il.ac.bgu.cs.bp.bpjs.context;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
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
import org.hibernate.jdbc.Work;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;

public class ContextService implements Serializable {
    private static ContextService uniqInstance = new ContextService();
	@SuppressWarnings("unused")
	public static NativeFunction subscribe;
	@SuppressWarnings("unused")
	public static NativeFunction subscribeWithParameters;
    private transient EntityManagerFactory emf;
    private transient EntityManager em;
    private transient ExecutorService pool;
    private transient BProgram bprog;
    private transient BProgramRunner rnr;
	private transient List<CtxType> contextTypes;
    private transient Multimap<Class<?>, NamedQuery> namedQueries;
    private Multimap<String, ?> activeContexts;
    private BEvent[] contextEvents;
	private String dbDump = null;
	private boolean verificationMode = false;

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

	private static class CtxType {
		String name;
		TypedQuery query;
		Class cls;
	}

	private Object readResolve() throws ObjectStreamException {
		uniqInstance.contextEvents = contextEvents;
		uniqInstance.contextTypes = contextTypes;
		uniqInstance.namedQueries = namedQueries;
		uniqInstance.activeContexts = activeContexts;
		
		if(dbDump!=null) {
            System.out.println(dbDump);
		    uniqInstance.em.getTransaction().begin();
            for(EntityType<?> e : uniqInstance.em.getMetamodel().getEntities()){
                uniqInstance.em.createQuery("Delete from "+e.getName()+" e").executeUpdate();
            }
            uniqInstance.em.createNativeQuery(dbDump).executeUpdate();
            uniqInstance.em.getTransaction().commit();
		}
		return uniqInstance;
	}

	private Object writeReplace() throws ObjectStreamException {
        Session session = em.unwrap(Session.class);
        session.doWork(connection -> {
            StringBuffer sb = new StringBuffer();
            for(EntityType<?> e : em.getMetamodel().getEntities()){
                Db2Sql.dumpTable(connection,sb,e.getName());
            }
            if(sb.length() > 0) {
                dbDump = sb.toString();
            } else {
                dbDump = null;
            }
        });
		return this;
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
        activeContexts = ArrayListMultimap.create();
        namedQueries.forEach((aClass, namedQuery) -> {
			try {
				TypedQuery<?> q = em.createNamedQuery(namedQuery.name(), aClass);
				Set<Parameter<?>> parameters = q.getParameters();
				if (parameters == null || parameters.isEmpty()) {
					registerContextQuery(namedQuery.name(), em.createNamedQuery(namedQuery.name(), aClass), aClass);
				}
			} catch (Exception ignored) {
			}
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
			List<?> knownContexts = new LinkedList<>(activeContexts.get(ctxType.name));
			// Update the list of contexts
            activeContexts.replaceValues(ctxType.name, ctxType.query.getResultList());
			// Filter the contexts that we didn't yet report of
			List<?> newContexts = new LinkedList<>(activeContexts.get(ctxType.name));
			//noinspection SuspiciousMethodCalls
			newContexts.removeAll(knownContexts);
			// Compute the contexts that where just removed
			newContexts.stream().map(obj -> new NewContextEvent(ctxType.name, obj)).forEach(events::add);
			//noinspection SuspiciousMethodCalls
			knownContexts.removeAll(activeContexts.get(ctxType.name));
			knownContexts.stream().map(obj -> new ContextEndedEvent(ctxType.name, obj)).forEach(events::add);
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
			if (ct.name.equals(type)) {
				@SuppressWarnings("unchecked")
				List<T> l =  (List<T>) activeContexts.get(ct.name);
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

	private void registerContextQuery(String name, TypedQuery<?> query, Class<?> cls) {
		CtxType newType = new CtxType();
		newType.name = name;
		newType.cls = cls;
		newType.query = query;
		contextTypes.add(newType);
		updateContexts();
	}

	public static void registerParameterizedContextQuery(String queryName, String uniqueId, NativeObject params) {
	    getInstance().innerRegisterParameterizedContextQuery(queryName, uniqueId, params);
    }

	@SuppressWarnings("unused")
	private void innerRegisterParameterizedContextQuery(String queryName, String uniqueId, NativeObject params) {
		namedQueries.forEach((aClass, namedQuery) -> {
			if(namedQuery.name().equals(queryName)) {
				TypedQuery<?> q = em.createNamedQuery(queryName, aClass);
				//noinspection unchecked
				((Map<String,?>) params).forEach((key, val) -> {
					Object v = val;
					if(v instanceof Number) {
						Number number= (Number) v;
						String typeName = q.getParameter(key).getParameterType().getName();
						switch (typeName){
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
				registerContextQuery(uniqueId, q, aClass);
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
