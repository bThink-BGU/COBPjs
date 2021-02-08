package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import static il.ac.bgu.cs.bp.bpjs.context.PrintCOBProgramRunnerListener.Level;

public class Main {
  public static void main(final String[] args) {
    BProgram bprog = new ContextBProgram("SampleContextualProgram.js");
    final BProgramRunner rnr = new BProgramRunner(bprog);
//    rnr.addListener(new PrintCOBProgramRunnerListener(Level.ALL));
    rnr.addListener(new PrintCOBProgramRunnerListener(Level.CtxChanged));
    rnr.run();
  }
}
