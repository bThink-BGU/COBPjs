package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class Main {
  /**
   * Choose the desired COBP program...
   */
  private static final Example example =
//      Example.HotCold;
//      Example.SampleProgram;
      Example.TicTacToeWithUI;
//      Example.TicTacToeWithoutUI;

  public static void main(final String[] args) {
    BProgram bprog = new ContextBProgram(example.getResourcesNames());
    final BProgramRunner rnr = new BProgramRunner(bprog);
    rnr.addListener(new PrintBProgramRunnerListener());

    example.initializeExecution(bprog,rnr);
    rnr.run();
  }
}
