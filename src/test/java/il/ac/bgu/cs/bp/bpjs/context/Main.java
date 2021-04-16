package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.context.HotCold.HotColdActuator;
import il.ac.bgu.cs.bp.bpjs.context.TicTacToe.TicTacToeGameMain;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
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
  private static final Example example =
//      Example.HotCold;
      Example.TicTacToe;

  /**
   * internal context events are: "CTX.Changed", "_____CTX_LOCK_____", "_____CTX_RELEASE_____"
   * You can filter these event from printing on console using the Level:
   * Level.ALL : print all
   * Level.NONE : print none
   * Level.CtxChanged: print only CTX.Changed events (i.e., filter the transaction lock/release events)
   */
  private static final Level logLevel = Level.CtxChanged;


  public static void main(final String[] args) throws URISyntaxException {
    var files =
        Arrays.stream(Objects.requireNonNull(Path.of(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(example.name())).toURI()).toFile().listFiles()))
            .map(f -> String.join("/", example.name(), f.getName()))
            .collect(Collectors.toList());
    BProgram bprog = new ContextBProgram(files);
    final BProgramRunner rnr = new BProgramRunner(bprog);
    rnr.addListener(new PrintCOBProgramRunnerListener(logLevel));

    if (example == Example.TicTacToe) {
      boolean useUI = true;
      TicTacToeGameMain.main(bprog, rnr, useUI);
      return;
    } else if (example == Example.HotCold) {
      bprog.setWaitForExternalEvents(true);
      rnr.addListener(new HotColdActuator());
    }
    rnr.run();
  }

  private enum Example {
    Chess, TicTacToe, HotCold, SampleProgram
  }
}
