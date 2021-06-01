package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class PrintCOBProgramRunnerListener extends BProgramRunnerListenerAdapter {
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
  public void eventSelected(BProgram bp, BEvent theEvent) {
    if(level == Level.ALL || !ContextProxy.CtxEvents.contains(theEvent.name) ||
        (level == Level.CtxChanged && theEvent.name.equals("CTX.Changed")))
      listener.eventSelected(bp, theEvent);
  }

  public enum Level {
    ALL, NONE, CtxChanged
  }
}
