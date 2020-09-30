package il.ac.bgu.cs.bp.bpjs.context;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Context implements Serializable {
    private static final Context singleton = new Context();
    private static AtomicInteger idCounter = new AtomicInteger(0);

    public final Map<String, ContextEntity> CTX;
    public final Map<String, List<ContextEntity>> active = new HashMap<>();
    private Set<ActiveChange> changes = new HashSet<>();

    public static Context GetInstance() {
        return singleton;
    }

    public static int generateUniqueId() {
        return singleton.idCounter.incrementAndGet();
    }

    public List<ContextEntity> getActive(String query) {
        return active.get(query);
    }

    public List<ContextEntity> getQueryResults(String query) {
        return CTX.values().stream().filter(entity -> Query.getQuery(query).func(entity)).collect(Collectors.toList());
    }

    public ActiveChange[] recentChanges() {
        return changes.toArray(ActiveChange[]::new);
    }

    private Context() {
        CTX = new HashMap<>();
    }

    public void insertEntity(ContextEntity entity) {
        if (CTX.containsKey(entity.id)) {
            throw new IllegalArgumentException("Key " + entity.id + " already exists");
        }
        ContextEntity attached = entity.attachedCopy();
        CTX.put(attached.id, attached);
        updateQueries();
    }

    public void updateEntity(ContextEntity detachedEntity) {
        if (!CTX.containsKey(detachedEntity.id)) {
            throw new IllegalArgumentException("Key " + detachedEntity.id + " does not exists");
        }
        CTX.get(detachedEntity.id).mergeChanges(detachedEntity);
        updateQueries();
    }

    public void deleteEntity(ContextEntity detachedEntity) {
        if (!CTX.containsKey(detachedEntity.id)) {
            throw new IllegalArgumentException("Key " + detachedEntity.id + " does not exists");
        }
        CTX.remove(detachedEntity.id);
        updateQueries();
    }

    public boolean hasQuery(String q) {
        return active.containsKey(q);
    }

    public void registerQuery(String q) {
        if (active.containsKey(q)) {
            throw new IllegalArgumentException("Query " + q + " already exists");
        }
        active.put(q, new ArrayList<>());
        updateQueries();
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
        Context context = (Context) o;
        return Objects.equals(CTX, context.CTX);
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
