package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.context.TicTacToe.TicTacToeGameMain;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static il.ac.bgu.cs.bp.bpjs.context.PrintCOBProgramRunnerListener.Level;

public class Main {
  /**
   * Choose the desired COBP program...
   */
  private static final Example example = Example.TicTacToe;

  public static void main(final String[] args) throws URISyntaxException {
    var files =
        Arrays.stream(Objects.requireNonNull(Path.of(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(example.name())).toURI()).toFile().listFiles()))
            .map(f -> String.join("/", example.name(), f.getName()))
            .collect(Collectors.toList());
    BProgram bprog = new ContextBProgram(files);
    final BProgramRunner rnr = new BProgramRunner(bprog);
    rnr.addListener(new PrintBProgramRunnerListener());

    /** internal context events are: "CTX.Changed", "_____CTX_LOCK_____", "_____CTX_RELEASE_____"
     * You can filter these event from printing on console using the Level:
     * Level.ALL : print all
     * Level.NONE : print none
     * Level.CtxChanged: print only CTX.Changed events (i.e., filter the transaction lock/release events)
     */
    rnr.addListener(new PrintCOBProgramRunnerListener(Level.CtxChanged));

    if (example == Example.TicTacToe) {
      boolean useUI = true;
      TicTacToeGameMain.main(bprog, rnr, useUI);
    } else
      rnr.run();
  }

  private enum Example {
    Chess, TicTacToe, HotCold, SampleProgram
  }
}
