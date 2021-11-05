package il.ac.bgu.cs.bp.bpjs.context.TicTacToe;

import il.ac.bgu.cs.bp.bpjs.analysis.BThreadSnapshotVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.context.ContextBProgram;
import il.ac.bgu.cs.bp.bpjs.context.Main;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Verification of the TicTacToe strategy.
 *
 * @author reututy
 */
public class TicTacToeVerificationMain {
  /**
   * Choose the desired COBP program...
   */
  private static final Main.Example example = Main.Example.TicTacToe;

  public static void main(String[] args) throws InterruptedException, URISyntaxException {

    // Create a program
    var files =
        Arrays.stream(Objects.requireNonNull(Path.of(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(example.name())).toURI()).toFile().listFiles()))
            .map(f -> String.join("/", example.name(), f.getName()))
            .collect(Collectors.toList());
    BProgram bprog = new ContextBProgram(files);

    bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
    bprog.setWaitForExternalEvents(true);

    String simulatedPlayer = "ctx.bthread(\"simulate x\", \"Cell.All\", function (cell) {\n" +
        "  sync({request: Event(\"X\", cell)})\n" +
        "})";

    // This bthread models the requirement that X never wins.
    String xCantWinRequirementBThread = "bp.registerBThread( \"req:NoXWin\", function(){\n" +
        "	bp.sync({waitFor:bp.Event('XWin')});\n" +
        "	bp.ASSERT(false, \"Found a trace where X wins.\");\n" +
        "});";

    bprog.appendSource(simulatedPlayer);
    bprog.appendSource(xCantWinRequirementBThread);
//		bprog.appendSource(infiBThread);
    try {
      DfsBProgramVerifier vfr = new DfsBProgramVerifier();
      vfr.addInspection(ExecutionTraceInspections.FAILED_ASSERTIONS);

      vfr.setMaxTraceLength(70);
//            vfr.setDebugMode(true);
      vfr.setVisitedStateStore(new BThreadSnapshotVisitedStateStore());
      vfr.setProgressListener(new PrintDfsVerifierListener());

      final VerificationResult res = vfr.verify(bprog);
      if (res.isViolationFound()) {
        System.out.println("Found a counterexample");
        res.getViolation().get()
            .getCounterExampleTrace()
            .getNodes()
            .forEach(nd -> System.out.println(" " + nd.getEvent()));

      } else {
        System.out.println("No counterexample found.");
      }
      System.out.printf("Scanned %,d states\n", res.getScannedStatesCount());
      System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies());

    } catch (Exception ex) {
      ex.printStackTrace(System.out);
    }

    System.out.println("end of run");
  }

}