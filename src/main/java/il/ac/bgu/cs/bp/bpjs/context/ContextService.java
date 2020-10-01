package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ContextService implements Serializable {
    private static ContextService singleton;
    private final transient AtomicInteger idCounter = new AtomicInteger(0);

    private final transient BProgram bp;
    private final transient BProgramRunner rnr;
    private final transient Map<String, Function> queries = new HashMap<>();
    private final Map<String, ContextEntity> CTX = new HashMap<>();
    private final Map<String, List<ContextEntity>> active = new HashMap<>();
    private final Set<ActiveChange> changes = new HashSet<>();
    private final ArrayList<EffectFunction> effectFunctions = new ArrayList<>();

    public static ContextService GetInstance() {
        return singleton;
    }

    public static ContextService CreateInstance(BProgram bp, BProgramRunner rnr) {
        singleton = new ContextService(bp, rnr);
//        bp.putInGlobalScope("CTX", singleton); //TODO return
        return singleton;
    }

    public static int generateUniqueId() {
        return singleton.idCounter.incrementAndGet();
    }

    public void addEffectFunction(EffectFunction function) {
        this.effectFunctions.add(function);
        rnr.addListener(function);
    }

    public List<ContextEntity> getActive(String query) {
        return active.get(query);
    }

    public List<ContextEntity> getQueryResults(String query) {
        return CTX.values().stream().filter(entity -> runQuery(query, entity)).collect(Collectors.toList());
    }

    private boolean runQuery(String queryName, ContextEntity entity) {
        Function fct = queries.get(queryName);
        Object result = fct.call(Context.getCurrentContext(), bp.getGlobalScope(), bp.getGlobalScope(), new Object[]{entity});
        return (boolean) Context.jsToJava(result,boolean.class);
    }

    public ActiveChange[] recentChanges() {
        return changes.toArray(ActiveChange[]::new);
    }

    private ContextService(BProgram bp, BProgramRunner rnr) {
        this.bp = bp;
        this.rnr = rnr;
    }

    public void insertEntity(ContextEntity entity) {
        if (CTX.containsKey(entity.id)) {
            throw new IllegalArgumentException("Key " + entity.id + " already exists");
        }
        ContextEntity attached = entity.attachedCopy(bp);
        CTX.put(attached.id, attached);
        updateQueries();
    }

    public void updateEntity(ContextEntity detachedEntity) {
        if (!CTX.containsKey(detachedEntity.id)) {
            throw new IllegalArgumentException("Key " + detachedEntity.id + " does not exists");
        }
        CTX.get(detachedEntity.id).mergeChanges(bp, detachedEntity);
        updateQueries();
    }

    public ActiveChange[] getNewForQuery(String query) {
        return changes.stream().filter(c -> c.type.equals("new") && c.query.equals(query)).toArray(ActiveChange[]::new);
    }

    public ActiveChange[] getRecentCtxEnd() {
        return changes.stream().filter(c -> c.type.equals("end")).toArray(ActiveChange[]::new);
    }

    public void deleteEntity(ContextEntity detachedEntity) {
        if (!CTX.containsKey(detachedEntity.id)) {
            throw new IllegalArgumentException("Key " + detachedEntity.id + " does not exists");
        }
        CTX.remove(detachedEntity.id);
        updateQueries();
    }

    /*public boolean hasQuery(String q) {
        return active.containsKey(q);
    }*/

    public void registerQuery(String q, Function query) {
        if (active.containsKey(q)) {
            throw new IllegalArgumentException("Query " + q + " already exists");
        }
        active.put(q, new ArrayList<>());
        queries.put(q, query);
//        updateQueries();
    }

    private void updateQueries() {
        changes.clear();
        active.entrySet().forEach(entry -> {
            List<ContextEntity> entryEntities = entry.getValue();
            // Remember the list of contexts that we already reported of
            List<ContextEntity> knownContexts = new LinkedList<>(entryEntities);
            // Update the list of contexts
            entryEntities.clear();
            entryEntities.addAll(getQueryResults(entry.getKey()));
            // Filter the contexts that we didn't yet report of
            List<ContextEntity> newContexts = new LinkedList<>(entryEntities);
            // noinspection SuspiciousMethodCalls
            newContexts.removeAll(knownContexts);
            // Compute the contexts that where just removed
            newContexts.stream().map(obj -> new ActiveChange(entry.getKey(), obj, "new")).forEach(changes::add);
            // noinspection SuspiciousMethodCalls
            knownContexts.removeAll(entryEntities);
            knownContexts.stream().map(obj -> new ActiveChange(entry.getKey(), obj, "end")).forEach(changes::add);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextService contextService = (ContextService) o;
        return Objects.equals(CTX, contextService.CTX);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CTX);
    }

    public static class ActiveChange implements Serializable {
        public final String query;
        public final ContextEntity entity;
        public final String type;

        public ActiveChange(String query, ContextEntity entity, String type) {
            this.query = query;
            this.entity = entity;
            this.type = type;
        }

        @Override
        public String toString() {
            return "ActiveChange{" +
                    "query=" + query +
                    ", entity=" + entity +
                    ", type='" + type + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ActiveChange that = (ActiveChange) o;
            return Objects.equals(query, that.query) &&
                    Objects.equals(entity, that.entity) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(query, entity, type);
        }
    }
}
