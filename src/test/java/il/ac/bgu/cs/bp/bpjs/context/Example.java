package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.context.HotCold.HotColdActuator;
import il.ac.bgu.cs.bp.bpjs.context.TicTacToe.TicTacToeGameMain;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Example {
  public static final Example Chess = new Example("Chess");
  public static final Example TicTacToeWithUI = new Example("TicTacToe",
      bprog -> TicTacToeGameMain.initBProg(bprog, true), TicTacToeGameMain::initRNR);
  public static final Example TicTacToeWithoutUI = new Example("TicTacToe");
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

  public void initializeExecution(BProgram bProgram, BProgramRunner runner) {
    if (bprogConsumer != null)
      bprogConsumer.accept(bProgram);
    bProgram.setName(this.name);

    if (runner != null && rnrConsumer != null)
      rnrConsumer.accept(runner);
  }

  private List<String> getResourcesNames(boolean isVerification) {
    var resources = new ArrayList<String>();
    resources.add(String.join("/", this.name, "dal.js"));
    resources.add(String.join("/", this.name, "bl.js"));
    var verificationResource = String.join("/", this.name, "verification.js");
    if (isVerification && Thread.currentThread().getContextClassLoader().getResource(verificationResource) != null) {
      resources.add(verificationResource);
    }
    return resources;
  }

  public List<String> getResourcesNames() {
    return getResourcesNames(false);
  }

  public List<String> getVerificationResourcesNames() {
    return getResourcesNames(true);
  }
}
