package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.BaseFunction;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ContextProxy implements Serializable {
  public static final Map<String, BaseFunction> queries = new HashMap<>();
  public static final Map<String, BaseFunction> effectFunctions = new HashMap<>();

  private static ContextProxy proxy;
  private static ContextProxySer proxySer = new ContextProxySer();
  public static ScriptableObjectCloner cloner;

  private ContextProxy() {}

  public static ContextProxy Create(BProgram bprog) {
    proxy = new ContextProxy();
    cloner = new ScriptableObjectCloner(bprog);
    return proxy;
  }

  private static class ContextProxySer implements Serializable {
    private Object readResolve() throws ObjectStreamException {
      return proxy;
    }
  }

  private Object writeReplace() throws ObjectStreamException {
    return proxySer;
  }

  public Map<String, BaseFunction> queries() {
    return ContextProxy.queries;
  }

  public final Map<String, BaseFunction> effectFunctions() {
    return ContextProxy.effectFunctions;
  }
}
