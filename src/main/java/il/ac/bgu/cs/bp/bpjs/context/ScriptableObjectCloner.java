package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BPJSStubInputStream;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BPJSStubOutputStream;
import il.ac.bgu.cs.bp.bpjs.bprogramio.StreamObjectStub;
import il.ac.bgu.cs.bp.bpjs.bprogramio.StubProvider;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScriptableObjectCloner {
  private static StubProvider stubProvider = null;
  private BProgram bprog;

  public ScriptableObjectCloner(BProgram bprog) {
    this.bprog = bprog;
  }

  public ScriptableObject clone(ScriptableObject obj) {
    byte[] ser = serialize(obj);
    return deserialize(ser);
  }

  private ScriptableObject deserialize(byte[] bytes) {
    try {
      BPjs.enterRhinoContext();
      try (BPJSStubInputStream in = new BPJSStubInputStream(new ByteArrayInputStream(bytes),
          bprog.getGlobalScope(),
          getStubProvider())
      ) {
        return (ScriptableObject) in.readObject();
      } catch (ClassNotFoundException | IOException ex) {
        throw new RuntimeException("Error reading a serialized b-thread: " + ex.getMessage(), ex);
      }
    } finally {
      Context.exit();
    }
  }

  private byte[] serialize(ScriptableObject obj) {
    try {
      BPjs.enterRhinoContext();
      try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
           BPJSStubOutputStream outs = new BPJSStubOutputStream(bytes, bprog.getGlobalScope())) {
        outs.writeObject(obj);
        outs.flush();
        return bytes.toByteArray();
      } catch (IOException ex) {
        throw new RuntimeException("IO exception serializing a b-thread. Error message: " + ex.getMessage(), ex);
      }
    } finally {
      Context.exit();
    }
  }

  private StubProvider getStubProvider() {
    final BProgramJsProxy bpProxy = new BProgramJsProxy(bprog);
    if (stubProvider == null) {
      stubProvider = (StreamObjectStub stub) -> {
        if (stub == StreamObjectStub.BP_PROXY) {
          return bpProxy;
        }
        throw new IllegalArgumentException("Unknown stub " + stub);
      };
    }
    return stubProvider;
  }
}
