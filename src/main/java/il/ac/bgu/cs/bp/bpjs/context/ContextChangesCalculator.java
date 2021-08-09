package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.DirectMapProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult;
import il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy;
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
  public HashSet<ContextChange> calculateChanges(Map<String,Object> mapProxy) {
    Map<String, MapProxy.Modification<Object>> updates = mapProxy.getModifications();
    if (updates.isEmpty() || updates.keySet().stream().noneMatch(k->k.startsWith("CTX.Entity: "))) return new HashSet<>();
    DirectMapProxy<String, Object> currentMap = new DirectMapProxy<>(mapProxy);
    MapProxy<String, Object> currentStore = new MapProxy<>(currentMap);
    MapProxy<String, Object> nextStore = new MapProxy<>(currentMap, updates);
    HashSet<ContextChange> changes = new HashSet<>();

    //region Entities
    Map<String, Set<NativeObject>> currentQueriesEntities = getQueriesEntities(currentStore, bProgramSyncSnapshot);
    Map<String, Set<NativeObject>> nextQueriesEntities = getQueriesEntities(nextStore, bProgramSyncSnapshot);

    Map<String, Set<NativeObject>> newQueriesEntities = subtractCurrentEntitiesMaps(nextQueriesEntities, currentQueriesEntities);
    Map<String, Set<NativeObject>> removedQueriesEntities = subtractCurrentEntitiesMaps(currentQueriesEntities, nextQueriesEntities);

    changes.addAll(newQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "new", (String) entity.get("id")))).collect(Collectors.toList()));
    changes.addAll(removedQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "end", (String) entity.get("id")))).collect(Collectors.toList()));

    updates.put("Context changes", new MapProxy.PutValue<>(changes));
    return success;
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

  private static Map<String, Set<NativeObject>> getQueriesEntities(MapProxy<String, Object> store, BProgramSyncSnapshot bProgramSyncSnapshot) {
    Map<String, Set<NativeObject>> queriesEntities = ContextProxy.proxy.queries.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> store.filter(
                (key, value) -> key.startsWith("CTX.Entity: ") &&
                    runQuery(value, entry.getValue(), bProgramSyncSnapshot, store)
            ).values().stream().map(v -> (NativeObject) v).collect(Collectors.toSet())));
    return queriesEntities;
  }

  @Override
  public StorageConsolidationResult resolve(StorageConsolidationResult.Conflict conflict, BProgramSyncSnapshot bProgramSyncSnapshot, Set<BThreadSyncSnapshot> set) {
    return conflict;
  }

  private static boolean runQuery(Object value, BaseFunction func, BProgramSyncSnapshot bpss, MapProxy<String, Object> store) {
    Scriptable bpjsScope = bpss.getBProgram().getGlobalScope();
    Object bp  = bpjsScope.get("bp", bpjsScope);
    BProgramProxyForEffects proxy = new BProgramProxyForEffects(store);
    boolean bpChanged = false;
    Context cx = BPjs.enterRhinoContext();
    try {
      bpjsScope.put("bp", bpjsScope, Context.javaToJS(proxy, bpjsScope));
      bpChanged = true;
      return (boolean) func.call(cx, bpjsScope, bpjsScope, new Object[]{value});
    } finally {
      if(bpChanged)
        bpjsScope.put("bp", bpjsScope, bp);
      Context.exit();
    }
  }

  public static class ContextChange implements Serializable {
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
