package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.ContextDirectMapProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.mozilla.javascript.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ContextChangesCalculator {
  public void executeEffect(MapProxy<String, Object> nextStore, BaseFunction func, Object data, Context cx) {
    func.call(cx, func, func, new Object[]{data});
  }

  public HashSet<ContextChange> calculateChanges(MapProxy<String, Object> nextStore, ContextProxy proxy, BEvent event, Scriptable globalScope) {
    var currentStore = new ContextDirectMapProxy<>(nextStore);
    var bpJsProxy = globalScope.get("bp", globalScope);
    var ctx = (Map<String, Object>) Context.jsToJava(globalScope.get("ctx", globalScope), Map.class);
    var jsRunQuery = (Function) ctx.get("runQuery");
    var bpJProxy = (BProgramJsProxy) Context.jsToJava(bpJsProxy, BProgramJsProxy.class);
    ContextBProgramProxyForEffects bpProxy = new ContextBProgramProxyForEffects(nextStore, bpJProxy);
    Context cx = BPjs.enterRhinoContext();
    try {
      globalScope.put("bp", globalScope, Context.javaToJS(bpProxy, globalScope));
      return calculateChanges(nextStore, currentStore, event, proxy, cx, bpProxy, jsRunQuery);
    } finally {
      globalScope.put("bp", globalScope, bpJsProxy);
      Context.exit();
    }
  }

  public HashSet<ContextChange> calculateChanges(MapProxy<String, Object> nextStore, ContextDirectMapProxy<String, Object> currentStore,
                                                 BEvent event, ContextProxy proxy, Context cx, ContextBProgramProxyForEffects cbpProxy, Function jsRunQuery) {
    executeEffect(nextStore, proxy.effectFunctions.get("CTX.Effect: " + event.name), event.maybeData, cx);
    Map<String, MapProxy.Modification<Object>> updates = nextStore.getModifications();
    HashSet<ContextChange> changes = new HashSet<>();
    if (updates.isEmpty() || updates.keySet().stream().noneMatch(k -> k.startsWith("CTX.Entity: "))) {
      return changes;
    }
    //region Entities
    cbpProxy.setStore(currentStore);
    Map<String, NativeObject[]> currentQueriesEntities = getQueriesEntities(currentStore, proxy, cx, jsRunQuery);
    cbpProxy.setStore(nextStore);
    Map<String, NativeObject[]> nextQueriesEntities = getQueriesEntities(nextStore, proxy, cx, jsRunQuery);

    Map<String, Set<NativeObject>> newQueriesEntities = subtractCurrentEntitiesMaps(nextQueriesEntities, currentQueriesEntities);
    Map<String, Set<NativeObject>> removedQueriesEntities = subtractCurrentEntitiesMaps(currentQueriesEntities, nextQueriesEntities);

    changes.addAll(newQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "new", (String) entity.get("id")))).collect(Collectors.toList()));
    changes.addAll(removedQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "end", (String) entity.get("id")))).collect(Collectors.toList()));

    consolidate(updates, currentStore);
    return changes;
  }

  private void consolidate(Map<String, MapProxy.Modification<Object>> updates, ContextDirectMapProxy<String, Object> store) {
    updates.entrySet().stream()
        .filter(e -> e.getValue() instanceof MapProxy.PutValue)
        .forEach(e -> store.put(e.getKey(), ((MapProxy.PutValue<Object>) e.getValue()).getValue()));
    updates.entrySet().stream()
        .filter(e -> e.getValue() == MapProxy.Modification.DELETE)
        .forEach(e -> store.remove(e.getKey()));
  }

  private static Map<String, Set<NativeObject>> subtractCurrentEntitiesMaps(Map<String, NativeObject[]> queriesEntities1, Map<String, NativeObject[]> queriesEntities2) {
    Map<String, Set<NativeObject>> newQueriesEntities = queriesEntities1.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> Arrays.stream(entry.getValue()).filter(
                    v1 -> !queriesEntities2.containsKey(entry.getKey()) || Arrays.stream(queriesEntities2.get(entry.getKey()))
                        .noneMatch(v2 -> v2.get("id").equals(v1.get("id"))))
                .collect(Collectors.toSet())))
        .entrySet().stream().filter(entry -> entry.getValue().size() > 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return newQueriesEntities;
  }

  private static Map<String, NativeObject[]> getQueriesEntities(MapProxy<String, Object> store, ContextProxy proxy, Context cx, Function jsRunQuery) {
    Map<String, NativeObject[]> queriesEntities = proxy.queries.keySet().stream()
            .collect(Collectors.toMap(
                    java.util.function.Function.identity(),
                    key -> runQuery( key, cx, jsRunQuery)
            ));
    return queriesEntities;
  }

  private static NativeObject[] runQuery(String func, Context cx, Function jsRunQuery) {
    var funcScope = jsRunQuery.getParentScope();
    NativeArray result = (NativeArray) jsRunQuery.call(cx, funcScope, funcScope, new Object[]{func});
    return  (NativeObject[])Context.jsToJava(result, NativeObject[].class);
  }


  private static boolean runQuery(Object value, BaseFunction func, Context cx) {
    var funcScope = func.getParentScope();
    return (boolean) func.call(cx, funcScope, funcScope, new Object[]{value});
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
