package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BThreadDataProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.SyncStatementBuilder;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;

import java.io.Serializable;
import java.util.Objects;

public class BProgramProxyForEffects implements Serializable {
  public final MapProxy mapProxy;

  public BProgramProxyForEffects(MapProxy mapProxy) {
    this.mapProxy = mapProxy;
  }


  public MapProxy getStore() {
    return mapProxy;
  }

  public void sync(NativeObject jsRWB) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public void sync(NativeObject nativeObject, Object o) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public static void setCurrentBThread(BProgramSyncSnapshot bpss, BThreadSyncSnapshot btss) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public static void clearCurrentBThread() {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public void synchronizationPoint(NativeObject var1, Boolean var2, Object var3) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public void registerBThread(String name, Object data, Function func) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public void registerBThread(String name, Function func) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public void registerBThread(Function func) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public SyncStatementBuilder hot(boolean isHot) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public void fork() throws ContinuationPending {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public void setInterruptHandler(Object aPossibleHandler) {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  public BThreadDataProxy getThread() {
    return null;
  }
  public String getJavaThreadName() {
    throw new IllegalAccessError("Cannot call this function within an effect function");
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.mapProxy);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BProgramProxyForEffects other = (BProgramProxyForEffects) obj;
    return Objects.equals(this.mapProxy, other.mapProxy);
  }
}