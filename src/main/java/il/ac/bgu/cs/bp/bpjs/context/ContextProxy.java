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
  private static final long serialVersionUID = -7832072043618491085L;
  public static List<String> CtxEvents =
      List.of("CTX.Changed", "_____CTX_LOCK_____", "_____CTX_RELEASE_____", "Context population completed");
  public final Map<String, BaseFunction> queries = new HashMap<>();
  public final Map<String, BaseFunction> effectFunctions = new HashMap<>();
  private static final ContextChangesCalculator ccc = new ContextChangesCalculator();
  private static final ScriptableObjectCloner cloner = new ScriptableObjectCloner();

  @SuppressWarnings("unused")
  public void rethrowException(Throwable t) throws Throwable {
    throw t;
  }

  public void throwEndOfContext() {
    throw new EndOfContextException();
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
