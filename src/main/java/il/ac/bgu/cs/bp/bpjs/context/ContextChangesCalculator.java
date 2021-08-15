package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ContextChangesCalculator {
  public MapProxy<String, Object> executeEffect(Map<String, Object> store, BaseFunction func, Object data) {
    Scriptable bpjsScope = BPjs.makeBPjsSubScope();
    MapProxy<String, Object> mapProxy = new MapProxy<>(store);
    BProgramProxyForEffects proxy = new BProgramProxyForEffects(mapProxy);
    Context cx = BPjs.enterRhinoContext();
    try {
      bpjsScope.put("bp", bpjsScope, Context.javaToJS(proxy, bpjsScope));
      func.call(cx, bpjsScope, bpjsScope, new Object[]{data});
      return mapProxy;
    } finally {
      Context.exit();
    }
  }

  public void calculateChanges(Map<String,Object> store, ContextProxy proxy, BEvent event) {
    MapProxy<String, Object> nextStore = executeEffect(store, proxy.effectFunctions.get(event.name), event.maybeData);
    Map<String, MapProxy.Modification<Object>> updates = nextStore.getModifications();
    MapProxy<String, Object> currentStore = new MapProxy<>(store);
    HashSet<ContextChange> changes = new HashSet<>();
    store.put("CTX.Changes", changes);
    if (updates.isEmpty() || updates.keySet().stream().noneMatch(k->k.startsWith("CTX.Entity: "))) {
      return;
    }

    //region Entities
    Map<String, Set<NativeObject>> currentQueriesEntities = getQueriesEntities(currentStore, proxy);
    Map<String, Set<NativeObject>> nextQueriesEntities = getQueriesEntities(nextStore, proxy);

    Map<String, Set<NativeObject>> newQueriesEntities = subtractCurrentEntitiesMaps(nextQueriesEntities, currentQueriesEntities);
    Map<String, Set<NativeObject>> removedQueriesEntities = subtractCurrentEntitiesMaps(currentQueriesEntities, nextQueriesEntities);

    changes.addAll(newQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "new", (String) entity.get("id")))).collect(Collectors.toList()));
    changes.addAll(removedQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "end", (String) entity.get("id")))).collect(Collectors.toList()));
  }

  private static Map<String, Set<NativeObject>> subtractCurrentEntitiesMaps(Map<String, Set<NativeObject>> queriesEntities1, Map<String, Set<NativeObject>> queriesEntities2) {
    Map<String, Set<NativeObject>> newQueriesEntities = queriesEntities1.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().stream().filter(
                v1 -> !queriesEntities2.containsKey(entry.getKey()) || queriesEntities2.get(entry.getKey()).stream()
                    .noneMatch(v2 -> v2.get("id").equals(v1.get("id"))))
                .collect(Collectors.toSet())))
        .entrySet().stream().filter(entry -> entry.getValue().size() > 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return newQueriesEntities;
  }

  private static Map<String, Set<NativeObject>> getQueriesEntities(MapProxy<String, Object> store, ContextProxy proxy) {
    Map<String, Set<NativeObject>> queriesEntities = proxy.queries.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> store.filter(
                (key, value) -> key.startsWith("CTX.Entity: ") &&
                    runQuery(value, entry.getValue(), store)
            ).values().stream().map(v -> (NativeObject) v).collect(Collectors.toSet())));
    return queriesEntities;
  }

  private static boolean runQuery(Object value, BaseFunction func, MapProxy<String, Object> store) {
    Scriptable bpjsScope = BPjs.makeBPjsSubScope();
    BProgramProxyForEffects proxy = new BProgramProxyForEffects(store);
    Context cx = BPjs.enterRhinoContext();
    try {
      bpjsScope.put("bp", bpjsScope, Context.javaToJS(proxy, bpjsScope));
      return (boolean) func.call(cx, bpjsScope, bpjsScope, new Object[]{value});
    } finally {
      Context.exit();
    }
  }

  public static class ContextChange implements Serializable {
    private static final long serialVersionUID = 4949558465562651801L;
    public final String query;
    public final String type;
    public final String entityId;

    private ContextChange(String query, String type) {
      this(query, type, null);
    }

    private ContextChange(String query, String type, String entityId) {
      this.query = query;
      this.type = type;
      this.entityId = entityId;
    }

    @Override
    public int hashCode() {
      return Objects.hash(query, type, entityId);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ContextChange))
        return false;
      ContextChange o = (ContextChange) obj;
      return Objects.equals(query, o.query) && Objects.equals(type, o.type) && Objects.equals(entityId, o.entityId);
    }

    @Override
    public String toString() {
      String entityStr = entityId == null ? "" : ", entity: " + ScriptableUtils.stringify(entityId);
      return "{query: " + query + ", type: " + type + entityStr + "}";
    }
  }
}
