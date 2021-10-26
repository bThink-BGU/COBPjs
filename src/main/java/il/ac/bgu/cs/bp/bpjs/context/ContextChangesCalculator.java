package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.DirectMapProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.mozilla.javascript.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ContextChangesCalculator {
  public void executeEffect(BaseFunction func, Object data, Context cx) {
    func.call(cx, func, func, new Object[]{data});
  }

  public HashSet<ContextChange> calculateChanges(MapProxy<String, Object> nextStore, ContextProxy proxy, BEvent event, Scriptable globalScope) {
    var currentStore = new DirectMapProxy<>(nextStore);
    var bpJsProxy = globalScope.get("bp", globalScope);
    var ctx = (Map<String, Object>) Context.jsToJava(globalScope.get("ctx", globalScope), Map.class);
    var jsRunQuery = (Function) ctx.get("runQuery");
    var bpJProxy = (BProgramJsProxy) Context.jsToJava(bpJsProxy, BProgramJsProxy.class);
    ContextBProgramProxyForEffects bpProxyForEffect = new ContextBProgramProxyForEffects(nextStore, bpJProxy);
    Context cx = BPjs.enterRhinoContext();
    try {
      globalScope.put("bp", globalScope, Context.javaToJS(bpProxyForEffect, globalScope));
      return calculateChanges(nextStore, currentStore, event, proxy, cx, bpProxyForEffect, jsRunQuery);
    } finally {
      globalScope.put("bp", globalScope, bpJsProxy);
      Context.exit();
    }
  }

  public HashSet<ContextChange> calculateChanges(MapProxy<String, Object> nextStore, DirectMapProxy<String, Object> currentStore,
                                                 BEvent event, ContextProxy proxy, Context cx, ContextBProgramProxyForEffects cbpProxy, Function jsRunQuery) {
    executeEffect(proxy.effectFunctions.get("CTX.Effect: " + event.name), event.maybeData, cx);
    Map<String, MapProxy.Modification<Object>> updates = nextStore.getModifications();
    HashSet<ContextChange> changes = new HashSet<>();
    if (updates.isEmpty() || updates.keySet().stream().noneMatch(k -> k.startsWith("CTX.Entity: "))) {
      return changes;
    }
    //region Entities
    cbpProxy.setStore(currentStore);
    Map<String, List<NativeObject>> currentQueriesEntities = getQueriesEntities(proxy, cx, jsRunQuery);
    cbpProxy.setStore(nextStore);
    Map<String, List<NativeObject>> nextQueriesEntities = getQueriesEntities(proxy, cx, jsRunQuery);

    Map<String, Set<NativeObject>> newQueriesEntities = subtractCurrentEntitiesMaps(nextQueriesEntities, currentQueriesEntities);
    Map<String, Set<NativeObject>> removedQueriesEntities = subtractCurrentEntitiesMaps(currentQueriesEntities, nextQueriesEntities);

    changes.addAll(newQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "new", (String) entity.get("id")))).collect(Collectors.toList()));
    changes.addAll(removedQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "end", (String) entity.get("id")))).collect(Collectors.toList()));

    consolidate(updates, currentStore);
    return changes;
  }

  private void consolidate(Map<String, MapProxy.Modification<Object>> updates, DirectMapProxy<String, Object> store) {
    updates.entrySet().stream()
        .filter(e -> e.getValue() instanceof MapProxy.PutValue)
        .forEach(e -> store.put(e.getKey(), ((MapProxy.PutValue<Object>) e.getValue()).getValue()));
    updates.entrySet().stream()
        .filter(e -> e.getValue() == MapProxy.Modification.DELETE)
        .forEach(e -> store.remove(e.getKey()));
  }

  private static Map<String, Set<NativeObject>> subtractCurrentEntitiesMaps(Map<String, List<NativeObject>> queriesEntities1, Map<String, List<NativeObject>> queriesEntities2) {
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

  private static Map<String, List<NativeObject>> getQueriesEntities(ContextProxy proxy, Context cx, Function jsRunQuery) {
    Map<String, List<NativeObject>> queriesEntities = proxy.queries.keySet().stream()
            .collect(Collectors.toMap(
                    java.util.function.Function.identity(),
                    key -> Arrays.stream(runQuery( key, cx, jsRunQuery)).map(o->((NativeObject)o)).collect(Collectors.toList())
            ));
    return queriesEntities;
  }

  private static Object[] runQuery(String func, Context cx, Function jsRunQuery) {
    var funcScope = jsRunQuery.getParentScope();
    var result = (NativeArray)Context.jsToJava(jsRunQuery.call(cx, funcScope, funcScope, new Object[]{func}), NativeArray.class);
    return result.toArray();
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
