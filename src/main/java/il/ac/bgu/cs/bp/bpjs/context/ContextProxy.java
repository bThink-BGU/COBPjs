package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import org.mozilla.javascript.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ContextProxy implements Serializable {
  private static final long serialVersionUID = -7832072043618491085L;
  public static List<String> CtxEvents =
      List.of("CTX.Changed", "_____CTX_LOCK_____", "_____CTX_RELEASE_____", "Context population completed");
  public final Map<String, BaseFunction> queries = new HashMap<>();
  public final Map<String, BaseFunction> effectFunctions = new HashMap<>();
  private static final ScriptableObjectCloner cloner = new ScriptableObjectCloner();
  private static final ContextChangesCalculator ccc = new ContextChangesCalculator();
  private boolean effectFinished = false;

  @SuppressWarnings("unused")
  public void rethrowException(Throwable t) throws Throwable {
    throw t;
  }

  public boolean isEndOfContextException(Throwable t) {
    return t instanceof EndOfContextException;
  }

  public synchronized void waitForEffect(MapProxy<String, Object> mapProxy, BEvent event) {
    if(!effectFinished) {
      ccc.calculateChanges(mapProxy,this, event);
      effectFinished = true;
    }
  }

  public synchronized void resetEffect() {
    effectFinished = false;
  }

  public boolean shouldWake(NativeObject jsRWB, BEvent event) {
    Map<String, Object> jRWB = (Map) Context.jsToJava(jsRWB, Map.class);

    SyncStatement stmt = SyncStatement.make();

    Object req = jRWB.get("request");
    if ( req != null ) {
        if ( req instanceof NativeArray) {
          NativeArray arr = (NativeArray) req;
          stmt = stmt.request(arr.getIndexIds().stream()
              .map( i -> (BEvent)arr.get(i) )
              .collect( toList() ));
        } else {
          stmt = stmt.request((BEvent)req);
        }
    }

    EventSet waitForSet   = convertToEventSet(jRWB.get("waitFor"));
    stmt = stmt.waitFor( waitForSet );

    return stmt.shouldWakeFor(event);
  }

  private EventSet convertToEventSet( Object jsObject ) {
    if ( jsObject == null ) return EventSets.none;

    // This covers event sets AND events.
    if ( jsObject instanceof EventSet ) {
      return (EventSet)jsObject;

    } else if ( jsObject instanceof NativeArray ) {
      NativeArray arr = (NativeArray) jsObject;
      if ( arr.isEmpty() ) return EventSets.none;

      if ( Stream.of(arr.getIds()).anyMatch(id -> arr.get(id)==null) ) {
        throw new RuntimeException("EventSet Array contains null sets.");
      }

      if ( arr.getLength() == 1 ) return (EventSet)arr.get(0);

      return EventSets.anyOf(
          arr.getIndexIds().stream()
              .map( i ->(EventSet)arr.get(i) )
              .collect( toSet() ) );
    } else {
      final String errorMessage = "Cannot convert " + jsObject + " of class " + jsObject.getClass() + " to an event set";
      throw new BPjsRuntimeException(errorMessage);
    }
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
