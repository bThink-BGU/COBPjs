package il.ac.bgu.cs.bp.bpjs.context;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;

import com.google.common.collect.*;
import il.ac.bgu.cs.bp.bpjs.context.eventselection.ContextualEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.context.eventselection.PrioritizedBSyncEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;

public class ContextService implements Serializable {
    public static AtomicInteger counter = new AtomicInteger(0);
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
    private List<NamedQuery> namedQueries;
    private boolean verificationMode = false;
    private Collection<CtxType> contextTypes;
    private Collection<EffectFunction> contextUpdateListeners;
    private ContextInternalEvent contextEvents;

    private static class ContextServiceProxy implements Serializable {
        private Collection<CtxType> contextTypes;
        private ContextInternalEvent contextEvents;
        private List<String> dbDump;

        ContextServiceProxy(@NotNull ContextService cs) {
            this.contextTypes = cs.contextTypes;
            this.contextEvents = cs.contextEvents;
            this.dbDump = new LinkedList<>();
            EntityManager em = cs.createEntityManager();
            Session session = em.unwrap(Session.class);
            session.doWork(connection -> em.getMetamodel().getEntities()
                    .forEach(e -> dbDump.addAll(Db2Sql.dumpTable(connection, e.getName()))));
            em.close();
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
            em.close();
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
    public static List<?> getContextInstances(String contextName) {
        return uniqInstance.innerGetContextInstances(contextName);
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

    private static class CtxType implements Serializable {
        private static final long serialVersionUID = -1633878308592722931L;

        // transient TypedQuery query;
        final String queryName;
        final String uniqueId;
        final Map<String, ?> parameters;
        List<?> activeContexts = new LinkedList<>();

        CtxType(String queryName, String uniqueId,
                @Nullable Map<String, ?> parameters) {
            this.queryName = queryName;
            this.uniqueId = uniqueId;
            this.parameters = parameters;
        }

        private void readObject(ObjectInputStream aInputStream)
                throws ClassNotFoundException, IOException {
            // perform the default de-serialization first
            aInputStream.defaultReadObject();
        }

        private Query createQuery() {
            Query q = uniqInstance.createEntityManager().createNamedQuery(queryName);
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

    public static EventSet AnyContextCommandEvent() {
        return ComposableEventSet.anyOf(getInstance().contextUpdateListeners.stream().map(l->l.eventSet).collect(Collectors.toSet()));
    }

    public static void updateContextForVerification(BEvent selectedEvent) {
        getInstance().contextUpdateListeners.forEach(c -> {
            if(c.eventSet.contains(selectedEvent))
                c.execute(selectedEvent);
        });
    }

    public BProgram getBProgram() {
        return bprog;
    }

    public void addListener(BProgramRunnerListener listener) {
        if (rnr != null)
            rnr.addListener(listener);
    }

    public void addContextUpdateListener(EffectFunction listener) {
        addListener(listener);
        contextUpdateListeners.add(listener);
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
        /*if (verificationMode) {
            a.add("internal_context_verification.js");
        }*/
        bprog = new ResourceBProgram(a);

        init(persistenceUnit);
    }

    private void init(String persistenceUnit) {
        close();
        contextTypes = new ConcurrentLinkedDeque<>();
        pool = Executors.newFixedThreadPool(2);
        emf = Persistence.createEntityManagerFactory(persistenceUnit, ImmutableMap
                .of("javax.persistence.sharedCache.mode", "NONE"));
        namedQueries = findAllNamedQueries(emf);
        namedQueries.forEach(namedQuery -> {
            try {
                Query q = createEntityManager().createQuery(namedQuery.query());
                Set<Parameter<?>> parameters = q.getParameters();
                if ((parameters == null || parameters.isEmpty()) &&
                        namedQuery.query().trim().toLowerCase().startsWith("select")) {
                    registerContextQuery(namedQuery.name(), namedQuery.name(), null);
                }
            } catch (Exception ignored) {
            }
        });

        if(!verificationMode) {
            bprog.prependSource("bp.registerBThread(\"ContextReporterBT\", function() {\n" +
                    "    while (true) {\n" +
                    "        // Wait for next update\n" +
                    "        bp.sync({ waitFor:CTX.AnyContextCommandEvent() });\n" +
                    "\n" +
                    "        // Trigger new context events\n" +
                    "        var events = CTX.getContextEvents();\n" +
                    "        if(events.events.size() > 0)\n" +
                    "            bp.sync({ request: events });\n" +
                    "    }\n" +
                    "});");
        } else {
            bprog.prependSource("bp.registerBThread(\"ContextReporterBT\", function() {\n" +
                    "    while (true) {\n" +
                    "        var e = bp.sync({ waitFor: bp.EventSet(\"\", function(e) { return true;})});\n" +
                    "        CTX.updateContextForVerification(e);\n" +
                    "        // Trigger new context events\n" +
                    "        var events = CTX.getContextEvents();\n" +
                    "        if(events.events.size() > 0)\n" +
                    "            bp.sync({ request: events });\n" +
                    "    }\n" +
                    "});");
        }

        ContextualEventSelectionStrategy eventSelectionStrategy =
                new PrioritizedBSyncEventSelectionStrategy();
        eventSelectionStrategy.setPriority("ContextReporterBT", 1000);
        eventSelectionStrategy.setPriority("PopulateDB", 999);

        bprog.setEventSelectionStrategy(eventSelectionStrategy);
        bprog.setWaitForExternalEvents(true);

        contextUpdateListeners = new ArrayList<>();
        rnr = new BProgramRunner(bprog);
        addListener(new PrintBProgramRunnerListener());
        addContextUpdateListener(new InsertEffect());
    }

    public void run() {
        if (rnr == null)
            throw new IllegalArgumentException("initFromResources or initFromString must be called before running the program");
        pool.execute(rnr);
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

    private List<?> innerGetContextInstances(String contextName) {
        for (CtxType ct : contextTypes) {
            if (ct.uniqueId.equals(contextName)) {
                return ct.activeContexts;
            }
        }
        return null;
    }

    private static List<NamedQuery> findAllNamedQueries(EntityManagerFactory emf) {
        List<NamedQuery> namedQueries = Lists.newArrayList();
        Set<ManagedType<?>> managedTypes = emf.getMetamodel().getManagedTypes();
        for (ManagedType<?> managedType : managedTypes) {
            if (managedType instanceof IdentifiableType) {
                // Class<? extends ManagedType> identifiableTypeClass = managedType.getClass();
                // @SuppressWarnings("rawtypes")
                Class<?> javaClass = managedType.getJavaType();
                NamedQueries namedQueriesList = javaClass.getAnnotation(NamedQueries.class);
                if (namedQueriesList != null) {
                    namedQueries.addAll(Arrays.asList(namedQueriesList.value()));
                }

                NamedQuery namedQuery = javaClass.getAnnotation(NamedQuery.class);
                if (namedQuery != null) {
                    namedQueries.add(namedQuery);
                }
            }
        }
        return ImmutableList.copyOf(namedQueries);
    }

    private  void registerContextQuery(String queryName, String uniqueId, @Nullable Map<String, ?> parameters) {
        contextTypes.add(new CtxType(queryName, uniqueId, parameters));
        updateContexts();
    }

    public static void registerParameterizedContextQuery(String queryName, String uniqueId,
                                                         NativeObject params) {
        getInstance().innerRegisterParameterizedContextQuery(queryName, uniqueId, params);
    }

    private void innerRegisterParameterizedContextQuery(String queryName, String uniqueId,
                                                        NativeObject params) {
        namedQueries.forEach(namedQuery -> {
            if (namedQuery.name().equals(queryName)) {
                registerContextQuery(queryName, uniqueId, params);
            }
        });
    }

    @SuppressWarnings("unused")
    public void close() {
        try {
            pool.shutdownNow();
            emf.close();
        } catch (Exception ignored) {    }
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
            return events.stream()
                    .filter(e -> (e.type.equals(ContextEventType.NEW) && e.contextName.equals(contextName)))
                    .toArray(ContextInternalEventData[]::new);
        }

        public ContextInternalEventData[] endedContexts(String contextName) {
            return events.stream()
                    .filter(e -> (e.type.equals(ContextEventType.ENDED) && e.contextName.equals(contextName)))
                    .toArray(ContextInternalEventData[]::new);
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
            String ctx = this.ctx.toString();
            if(this.ctx instanceof Object[]) {
                ctx = Arrays.toString((Object[])this.ctx);
            }
            return String.format("{\"type\":%s , \"name\":%s , \"ctx\":%s}", type.toString(), contextName, ctx);
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

    public static abstract class EffectFunction extends BProgramRunnerListenerAdapter {
        public final EventSet eventSet;

        public EffectFunction(EventSet eventSet) {
            super();
            this.eventSet = eventSet;
        }

        @Override
        public final void eventSelected(BProgram bp, BEvent theEvent) {
            if(eventSet.contains(theEvent)) {
                execute(theEvent);
            }
        }

        public final void execute(BEvent event) {
            EntityManager em = getInstance().createEntityManager();
            em.getTransaction().begin();
            innerExecution(em, event);
            em.getTransaction().commit();
            getInstance().updateContexts();
        }

        protected abstract void innerExecution(EntityManager em, BEvent event);
    }

    public static class InsertEffect extends EffectFunction {

        public InsertEffect() {
            super((EventSet) bEvent -> bEvent.name.equals("CTX.Insert"));
        }

        @Override
        protected void innerExecution(EntityManager em, BEvent e) {
            if (e.maybeData == null)
                return;
            List<Object> objects = e.maybeData instanceof NativeArray ? (NativeArray)e.maybeData : List.of(e.maybeData);

            for (Object o : objects) {
                em.merge(o);
            }
        }
    }

    public static class UpdateEffect extends EffectFunction {
        public final String[] namedQueries;
        private final Function<BEvent,Map<String,Object>> parametersHandler;

        public UpdateEffect(String eventName, String[] namedQueries) {
            this(eventName, namedQueries, UpdateEffect::defaultParametersHandler);
        }

        public UpdateEffect(String eventName, String[] namedQueries, Function<BEvent,Map<String,Object>> parametersHandler) {
            this((EventSet) bEvent -> bEvent.name.equals(eventName),namedQueries, parametersHandler);
        }

        public UpdateEffect(EventSet eventSet, String[] namedQueries) {
            this(eventSet, namedQueries, UpdateEffect::defaultParametersHandler);
        }

        public UpdateEffect(EventSet eventSet, String[] namedQueries, Function<BEvent,Map<String,Object>> parametersHandler) {
            super(eventSet);
            this.namedQueries = namedQueries;
            this.parametersHandler = parametersHandler;
        }

        private static Map<String,Object> defaultParametersHandler(BEvent theEvent) {
            return (Map<String, Object>) theEvent.getData();
        }

        @Override
        protected void innerExecution(EntityManager em, BEvent theEvent) {
            for (String queryName : namedQueries) {
                Query q = em.createNamedQuery(queryName);
                if(theEvent.maybeData != null) ContextService.setParameters(q, parametersHandler.apply(theEvent));
                q.executeUpdate();
            }
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

    public static class AnyContextEndedEvent implements EventSet {
        private static final long serialVersionUID = 5402960437353425951L;
        public final String contextName;
        public final Object ctx;

        public AnyContextEndedEvent(String contextName) {
            this(contextName, null);
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
