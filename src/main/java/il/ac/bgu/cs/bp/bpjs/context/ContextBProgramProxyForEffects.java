package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.*;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.FailedAssertionException;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.JsEventSet;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;

public class ContextBProgramProxyForEffects {
    private static RuntimeException error(String name) {
        return new RuntimeException("Cannot call function '" + name + "' within an effect or query function");
    }

    public MapProxy mapProxy;
    private final BProgramJsProxy bpjsProxy;
    public final BpLog log;
    public final EventSetsJsProxy eventSets;
    public final RandomProxy random;

    public ContextBProgramProxyForEffects(MapProxy mapProxy, BProgramJsProxy bpjsProxy) {
        this.mapProxy = mapProxy;
        this.bpjsProxy = bpjsProxy;
        this.log = bpjsProxy.log;
        this.eventSets = bpjsProxy.eventSets;
        this.random = bpjsProxy.random;
    }

    public BEvent Event(String name) {
        return bpjsProxy.Event(name);
    }

    public BEvent Event(String name, Object jsData) {
        return bpjsProxy.Event(name, jsData);
    }

    public JsEventSet EventSet(String name, Object predicateObj) {
        return bpjsProxy.EventSet(name, predicateObj);
    }

    public EventSet allExcept(EventSet es) {
        return EventSets.not(es);
    }

    public void registerBThread(String name, Object data, Function func) {
        throw error("registerBThread");
    }

    public void registerBThread(String name, Function func) {
        throw error("registerBThread");
    }

    public void registerBThread(Function func) {
        throw error("registerBThread");
    }

    public void ASSERT(boolean value, String message) throws FailedAssertionException {
        bpjsProxy.ASSERT(value, message);
    }

    public void fork() throws ContinuationPending {
        throw error("fork");
    }

    public void setInterruptHandler(Object aPossibleHandler) {
        throw error("setInterruptHandler");
    }

    public MapProxy getStore() {
        return mapProxy;
    }

    void setStore(MapProxy<String, Object> store) {
        this.mapProxy = store;
    }

    public void sync(NativeObject jsRWB) {
        throw error("sync");
    }

    public void sync(NativeObject jsRWB, Object data) {
        throw error("sync");
    }

    void synchronizationPoint(NativeObject jsRWB, Boolean hot, Object data) {
        throw error("synchronizationPoint");
    }

    public SyncStatementBuilder hot(boolean isHot) {
        throw error("hot");
    }

    public BEvent enqueueExternalEvent(BEvent evt) {
        throw error("enqueueExternalEvent");
    }

    public void setWaitForExternalEvents(boolean newDaemonMode) {
        throw error("setWaitForExternalEvents");
    }

    public boolean isWaitForExternalEvents() {
        throw error("isWaitForExternalEvents");
    }

    public long getTime() {
        return bpjsProxy.getTime();
    }

    public BThreadDataProxy getThread() {
        throw error("getThread");
    }

    public String getJavaThreadName() {
        throw error("getJavaThreadName");
    }

    @Override
    public int hashCode() {
        return 42;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        } else {
            return this.getClass() == obj.getClass();
        }
    }
}