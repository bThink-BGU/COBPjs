package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.CtxDirectMapProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ContextChangesCalculator {
  public HashSet<ContextChange> calculateChanges(MapProxy<String,Object> mapProxy) {
    Map<String, MapProxy.Modification<Object>> updates = mapProxy.getModifications();
    if (updates.isEmpty() || updates.keySet().stream().noneMatch(k->k.startsWith("CTX.Entity: "))) return new HashSet<>();
    CtxDirectMapProxy<String, Object> currentStore = new CtxDirectMapProxy<>(mapProxy);
    MapProxy<String, Object> nextStore = mapProxy;
    HashSet<ContextChange> changes = new HashSet<>();

    //region Entities
    Map<String, Set<NativeObject>> currentQueriesEntities = getQueriesEntities(currentStore);
    Map<String, Set<NativeObject>> nextQueriesEntities = getQueriesEntities(nextStore);

    Map<String, Set<NativeObject>> newQueriesEntities = subtractCurrentEntitiesMaps(nextQueriesEntities, currentQueriesEntities);
    Map<String, Set<NativeObject>> removedQueriesEntities = subtractCurrentEntitiesMaps(currentQueriesEntities, nextQueriesEntities);

    changes.addAll(newQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "new", (String) entity.get("id")))).collect(Collectors.toList()));
    changes.addAll(removedQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "end", (String) entity.get("id")))).collect(Collectors.toList()));

    return changes;
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

  private static Map<String, Set<NativeObject>> getQueriesEntities(MapProxy<String, Object> store) {
    Map<String, Set<NativeObject>> queriesEntities = ContextProxy.proxy.queries.entrySet().stream()
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
