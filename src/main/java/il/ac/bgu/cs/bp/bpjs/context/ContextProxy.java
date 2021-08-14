package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.ScriptableObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ContextProxy implements Serializable {
  public static List<String> CtxEvents =
      List.of("CTX.Changed", "_____CTX_LOCK_____", "_____CTX_RELEASE_____", "Context population completed");
  public final Map<String, BaseFunction> queries = new HashMap<>();
  public final Map<String, BaseFunction> effectFunctions = new HashMap<>();
  private static final ContextChangesCalculator ccc = new ContextChangesCalculator();
  private static ScriptableObjectCloner cloner = new ScriptableObjectCloner();

  @SuppressWarnings("unused")
  public void rethrowException(Throwable t) throws Throwable {
    throw t;
  }

  @SuppressWarnings("unused")
  public HashSet<ContextChangesCalculator.ContextChange> getContextChanges(MapProxy<String, Object> modificationMap) {
    return ccc.calculateChanges(modificationMap, this);
  }

  @SuppressWarnings("unused")
  public ScriptableObject clone(ScriptableObject obj) {
    return cloner.clone(obj);
  }

  @SuppressWarnings("unused")
  public void removeScope(ScriptableObject object) {
/*
//    Context cx = BPjs.enterRhinoContext();
    try {
//      var scope = BPjs.makeBPjsSubScope();
      object.setPrototype(BPjs.getBPjsScope());
      object.setParentScope(BPjs.makeBPjsSubScope());
    } finally {
      Context.exit();
    }*/
  }
}
