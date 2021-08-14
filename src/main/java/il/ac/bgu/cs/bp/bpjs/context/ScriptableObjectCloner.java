package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BPJSStubInputStream;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BPJSStubOutputStream;
import il.ac.bgu.cs.bp.bpjs.bprogramio.StreamObjectStub;
import il.ac.bgu.cs.bp.bpjs.bprogramio.StubProvider;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScriptableObjectCloner {

  public ScriptableObject clone(ScriptableObject obj) {
    var scope = obj.getParentScope();
    byte[] ser = serialize(obj, scope);
    return deserialize(ser, scope);
  }

  private ScriptableObject deserialize(byte[] bytes, Scriptable scope) {
    try {
      BPjs.enterRhinoContext();
      try (var in = new ScriptableInputStream(new ByteArrayInputStream(bytes), scope)) {
        return (ScriptableObject) in.readObject();
      } catch (ClassNotFoundException | IOException ex) {
        throw new RuntimeException("Error reading a serialized b-thread: " + ex.getMessage(), ex);
      }
    } finally {
      Context.exit();
    }
  }

  private byte[] serialize(ScriptableObject obj, Scriptable scope) {
    try {
      BPjs.enterRhinoContext();
      try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
           ScriptableOutputStream outs = new ScriptableOutputStream(bytes, scope)) {
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
}
