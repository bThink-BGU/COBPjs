package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.text.MessageFormat;

public class Verification {
  /**
   * Choose the desired COBP program...
   */
  private static final Example example =
//      Example.SampleProgram;
//      Example.HotCold;
      Example.TicTacToeWithoutUI;

  public static void main(final String[] args) {
    BProgram bprog = new ContextBProgram(example.getVerificationResourcesNames());
    example.initializeExecution(bprog, null);

    DfsBProgramVerifier vfr = new DfsBProgramVerifier();
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
