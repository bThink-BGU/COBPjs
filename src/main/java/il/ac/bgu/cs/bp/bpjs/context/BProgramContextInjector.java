package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

public class BProgramContextInjector {
  public static void Inject(BProgram bprog) {
    if (!(bprog.getEventSelectionStrategy() instanceof SimpleEventSelectionStrategy)) {
      throw new IllegalArgumentException("Contextual BProgram cannot have an EventSelectionStrategy, though it gets a default one - PrioritizedBSyncEventSelectionStrategy");
    }
    if (bprog.getStorageModificationStrategy() != StorageModificationStrategy.PASSTHROUGH) {
      throw new IllegalArgumentException("Contextual BProgram cannot have an StorageModificationStrategy");
    }

    bprog.setEventSelectionStrategy(new CtxEventSelectionStrategy());
    bprog.setStorageModificationStrategy(new ContextStorageModificationStrategy());
    bprog.appendSource("context.js");
    bprog.putInGlobalScope("ctx_proxy", new ContextProxy());
  }
}
