package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class PrintCOBProgramRunnerListener extends PrintBProgramRunnerListener {
  private Level level;

  public PrintCOBProgramRunnerListener(Level level) {
    this.level = level;
  }

  /**
   * Default level = Level.ALL
   */
  public PrintCOBProgramRunnerListener() {
    this(Level.CtxChanged);
  }

  @Override
  public void eventSelected(BProgram bp, BEvent theEvent) {
    if(level == Level.ALL || !ContextProxy.CtxEvents.contains(theEvent.name) ||
        (level == Level.CtxChanged && theEvent.name.equals("CTX.Changed")))
      super.eventSelected(bp, theEvent);
  }

  public enum Level {
    ALL, NONE, CtxChanged
  }
}
