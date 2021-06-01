package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SafetyViolationTag;

public class PrintCOBProgramRunnerListener implements BProgramRunnerListener {
  private final Level level;
  private final BProgramRunnerListener listener;

  public PrintCOBProgramRunnerListener(Level level, BProgramRunnerListener listener) {
    this.level = level;
    this.listener = listener;
  }

  /**
   * Default level = Level.CtxChanged
   */
  public PrintCOBProgramRunnerListener(BProgramRunnerListener listener) {
    this(Level.CtxChanged, listener);
  }

  @Override
  public void starting(BProgram bProgram) {
    listener.starting(bProgram);
  }

  @Override
  public void started(BProgram bProgram) {
    listener.started(bProgram);
  }

  @Override
  public void superstepDone(BProgram bProgram) {
    listener.superstepDone(bProgram);
  }

  @Override
  public void ended(BProgram bProgram) {
    listener.ended(bProgram);
  }

  @Override
  public void assertionFailed(BProgram bProgram, SafetyViolationTag safetyViolationTag) {
    listener.assertionFailed(bProgram, safetyViolationTag);
  }

  @Override
  public void bthreadAdded(BProgram bProgram, BThreadSyncSnapshot bThreadSyncSnapshot) {
    listener.bthreadAdded(bProgram, bThreadSyncSnapshot);
  }

  @Override
  public void bthreadRemoved(BProgram bProgram, BThreadSyncSnapshot bThreadSyncSnapshot) {
    listener.bthreadRemoved(bProgram, bThreadSyncSnapshot);
  }

  @Override
  public void bthreadDone(BProgram bProgram, BThreadSyncSnapshot bThreadSyncSnapshot) {
    listener.bthreadDone(bProgram, bThreadSyncSnapshot);
  }

  @Override
  public void eventSelected(BProgram bp, BEvent theEvent) {
    if (level == Level.ALL || !ContextProxy.CtxEvents.contains(theEvent.name) ||
        (level == Level.CtxChanged && theEvent.name.equals("CTX.Changed")))
      listener.eventSelected(bp, theEvent);
  }

  @Override
  public void halted(BProgram bProgram) {
    listener.halted(bProgram);
  }

  @Override
  public void error(BProgram bp, Exception ex) {
    listener.error(bp, ex);
  }

  public enum Level {
    ALL, NONE, CtxChanged
  }
}
