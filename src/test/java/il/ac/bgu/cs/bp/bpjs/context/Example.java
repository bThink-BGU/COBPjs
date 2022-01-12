package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.context.HotCold.HotColdActuator;
import il.ac.bgu.cs.bp.bpjs.context.TicTacToe.TicTacToeGameMain;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class Example {
  public static final Example Chess = new Example("Chess");
  public static final Example TicTacToeWithUI = new Example("TicTacToe",
      bprog -> TicTacToeGameMain.initBProg(bprog, true), TicTacToeGameMain::initRNR);
  public static final Example TicTacToeWithoutUI = new Example("TicTacToe",
      bprog -> TicTacToeGameMain.initBProg(bprog, false), TicTacToeGameMain::initRNR);
  public static final Example HotCold = new Example("HotCold", null, rnr -> rnr.addListener(new HotColdActuator()));
  public static final Example SampleProgram = new Example("SampleProgram");

  public final String name;
  private final Consumer<BProgramRunner> rnrConsumer;
  private final Consumer<BProgram> bprogConsumer;

  private Example(String name) {
    this(name, null, null);
  }

  private Example(String name, Consumer<BProgram> bprogConsumer, Consumer<BProgramRunner> rnrConsumer) {
    this.name = name;
    this.rnrConsumer = rnrConsumer;
    this.bprogConsumer = bprogConsumer;
  }

  public void initializeBProg(BProgram bProgram) {
    if (bprogConsumer != null)
      bprogConsumer.accept(bProgram);
    bProgram.setName(this.name);
  }

  public void initializeRunner(BProgramRunner runner) {
    if (rnrConsumer != null)
      rnrConsumer.accept(runner);
    runner.addListener(new PrintBProgramRunnerListener());
  }

  public void addVerificationResources(BProgram bprog) throws IOException {
    var resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(
        String.join("/", this.name, "verification.js"));
    if (resource != null) {
      bprog.appendSource(new String(resource.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  public List<String> getResourcesNames() {
    return List.of(String.join("/", this.name, "dal.js"), String.join("/", this.name, "bl.js"));
  }
}
