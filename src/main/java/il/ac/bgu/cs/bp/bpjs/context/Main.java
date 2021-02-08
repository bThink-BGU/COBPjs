package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class Main {
  public static void main(final String[] args) {

    BProgram bprog = new ContextBProgram("test.js");
    final BProgramRunner rnr = new BProgramRunner(bprog);
    rnr.addListener(new PrintBProgramRunnerListener());
    rnr.run();
  }
}
