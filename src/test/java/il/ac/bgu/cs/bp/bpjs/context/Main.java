package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import static il.ac.bgu.cs.bp.bpjs.context.PrintCOBProgramRunnerListener.Level;

public class Main {
  public static void main(final String[] args) {

    /** Choose the desired COBP program... */
//    BProgram bprog = new ContextBProgram("SampleContextualProgram.js");
    BProgram bprog = new ContextBProgram("chess/dal.js", "chess/bl.js");

    final BProgramRunner rnr = new BProgramRunner(bprog);

    /** internal context events are: "CTX.Changed", "_____CTX_LOCK_____", "_____CTX_RELEASE_____"
     * You can filter these event from printing on console using the Level:
     * Level.ALL : print all
     * Level.NONE : print none
     * Level.CtxChanged: print only CTX.Changed events (i.e., filter the transaction lock/release events)
     */
    rnr.addListener(new PrintCOBProgramRunnerListener(Level.CtxChanged));
    rnr.run();
  }
}
