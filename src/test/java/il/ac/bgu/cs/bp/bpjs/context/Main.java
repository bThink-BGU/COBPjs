package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.io.IOException;
import java.text.MessageFormat;

public class Main {
  private static BProgram bprog;
  private static Example example = Example.HotCold;

  public static void main(final String[] args) throws IOException {
    createBProgam();
//    runProgram();
    verifyProgram();
  }

  private static void createBProgam() {
    //region Load example program
    bprog = new ContextBProgram(example.getResourcesNames());
    //print example.getResoucesNames()
    System.out.println(MessageFormat.format("Loading resources: {0}", example.getResourcesNames()));
    example.initializeBProg(bprog);
    //endregion

    //region Load non-sample program
    /*
    // To load non-sample program (i.e., for example the program <Project>/src/main/resources/HelloBPjsWorld.js)
     this.bprog = new ContextBProgram("HelloBPjsWorld.js"); // you can add more files

    // You can use a different EventSelectionStrategy, for example:
     bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
     */
    //endregion
  }

  private static void runProgram() {
    var rnr = new BProgramRunner(bprog);
    if(example != null) {
      example.initializeRunner(rnr);
    } else {
      rnr.addListener(new PrintBProgramRunnerListener());
    }
    rnr.run();
  }

  private static void verifyProgram() throws IOException {
    if (example != null) {
      example.addVerificationResources(bprog);
    }
    var vfr = new DfsBProgramVerifier();
    vfr.setMaxTraceLength(2000);
    vfr.setProgressListener(new PrintDfsVerifierListener());
    vfr.setIterationCountGap(100);
//    vfr.setDebugMode(true);
    try {
      var res = vfr.verify(bprog);
      System.out.println(MessageFormat.format(
          "States = {0}\n" +
              "Edges = {1}\n" +
              "Time = {2}",
          res.getScannedStatesCount(), res.getScannedEdgesCount(), res.getTimeMillies()));
      if (res.isViolationFound())
        System.out.println(MessageFormat.format("Found violation: {0}", res.getViolation().get()));
      else
        System.out.println("No violation found");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
