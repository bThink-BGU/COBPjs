package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.CtxDirectMapProxy;
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

  public HashSet<ContextChange> calculateChanges(MapProxy<String, Object> store, ContextProxy proxy, BEvent event, Scriptable globalScope) {
    var bpJsProxy = globalScope.get("bp", globalScope);
    var ctx = (Map<String, Object>) Context.jsToJava(globalScope.get("ctx", globalScope), Map.class);
    var jsRunQuery = (Function) ctx.get("runQuery");
    var bpJProxy = (BProgramJsProxy) Context.jsToJava(bpJsProxy, BProgramJsProxy.class);
    ContextBProgramProxyForEffects bpProxyForEffect = new ContextBProgramProxyForEffects(store, bpJProxy);
    Context cx = BPjs.enterRhinoContext();
    try {
      globalScope.put("bp", globalScope, Context.javaToJS(bpProxyForEffect, globalScope));
      bpProxyForEffect.setStore(new CtxDirectMapProxy<>(store));
      return calculateChanges(event, proxy, cx, jsRunQuery);
    } finally {
      globalScope.put("bp", globalScope, bpJsProxy);
      Context.exit();
    }
  }

  public HashSet<ContextChange> calculateChanges(BEvent event, ContextProxy proxy,
                                                 Context cx, Function jsRunQuery) {
    Map<String, List<String>> currentQueriesEntities = getQueriesEntities(proxy, cx, jsRunQuery);
    executeEffect(proxy.effectFunctions.get("CTX.Effect: " + event.name), event.maybeData, cx);
    Map<String, List<String>> nextQueriesEntities = getQueriesEntities(proxy, cx, jsRunQuery);

    Map<String, Set<String>> newQueriesEntities = subtractCurrentEntitiesMaps(nextQueriesEntities, currentQueriesEntities);
    Map<String, Set<String>> removedQueriesEntities = subtractCurrentEntitiesMaps(currentQueriesEntities, nextQueriesEntities);

    HashSet<ContextChange> changes = new HashSet<>();

    changes.addAll(newQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "new", entity))).collect(Collectors.toList()));
    changes.addAll(removedQueriesEntities.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(entity -> new ContextChange(entry.getKey(), "end", entity))).collect(Collectors.toList()));

//    consolidate(store);

    return changes;
  }

  /*private void consolidate(MapProxy<String, Object> store) {
    updates.entrySet().stream()
        .filter(e -> e.getValue() instanceof MapProxy.PutValue)
        .forEach(e -> store.put(e.getKey(), ((MapProxy.PutValue<Object>) e.getValue()).getValue()));
    updates.entrySet().stream()
        .filter(e -> e.getValue() == MapProxy.Modification.DELETE)
        .forEach(e -> store.remove(e.getKey()));
  }*/

  private static Map<String, Set<String>> subtractCurrentEntitiesMaps(Map<String, List<String>> queriesEntities1, Map<String, List<String>> queriesEntities2) {
    Map<String, Set<String>> newQueriesEntities = queriesEntities1.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().stream().filter(
                    v1 -> !queriesEntities2.containsKey(entry.getKey()) || queriesEntities2.get(entry.getKey()).stream()
                        .noneMatch(v2 -> v2.equals(v1)))
                .collect(Collectors.toSet())))
        .entrySet().stream().filter(entry -> entry.getValue().size() > 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return newQueriesEntities;
  }

  private static Map<String, List<String>> getQueriesEntities(ContextProxy proxy, Context cx, Function jsRunQuery) {
    Map<String, List<String>> queriesEntities = proxy.queries.keySet().stream()
            .collect(Collectors.toMap(
                    java.util.function.Function.identity(),
                    key -> Arrays.stream(runQuery( key, cx, jsRunQuery)).map(o->(String)((NativeObject)o).get("id")).collect(Collectors.toList())
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
