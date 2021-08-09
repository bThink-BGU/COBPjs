package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.BaseFunction;

import java.io.ObjectStreamException;
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
  private final ContextChangesCalculator ccc = new ContextChangesCalculator();

  public static ContextProxy proxy;
  private static ContextProxySer proxySer;
  public static ScriptableObjectCloner cloner;

  private ContextProxy() {
  }

  @SuppressWarnings("unused")
  public void rethrowException(Throwable t) throws Throwable {
    throw t;
  }

  @SuppressWarnings("unused")
  public HashSet<ContextChangesCalculator.ContextChange> getContextChanges(MapProxy<String, Object> modificationMap) {
    return ccc.calculateChanges(modificationMap);
  }

  public static ContextProxy Create(BProgram bprog) {
    proxy = new ContextProxy();
    proxySer = new ContextProxySer();
    cloner = new ScriptableObjectCloner(bprog);
    return proxy;
  }

  private static class ContextProxySer implements Serializable {
    private static final long serialVersionUID = -7901897016797999371L;

    private Object readResolve() throws ObjectStreamException {
      return proxy;
    }
  }

  private Object writeReplace() throws ObjectStreamException {
    return proxySer;
  }
}
