package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ContextProxy implements Serializable {
  public static final Map<String, BaseFunction> queries = new HashMap<>();
  public static final Map<String, BaseFunction> effectFunctions = new HashMap<>();

  public static ContextProxy proxy;
  private static ContextProxySer proxySer = new ContextProxySer();
  public static BProgram bprog;

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

  @Override
  public String toString() {
    return "ContextProxy{}";
  }
}
