package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Verification {
  /**
   * Choose the desired COBP program...
   */
  private static final Main.Example example =
      Main.Example.SampleProgram;
//      Main.Example.HotCold;
//      Main.Example.TicTacToe;

  public static void main(final String[] args) throws URISyntaxException {
    var files =
        Arrays.stream(Objects.requireNonNull(Path.of(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(example.name())).toURI()).toFile().listFiles()))
            .map(f -> String.join("/", example.name(), f.getName()))
            .collect(Collectors.toList());
    BProgram bprog = new ContextBProgram(files);
    DfsBProgramVerifier vfr = new DfsBProgramVerifier();
    vfr.setMaxTraceLength(2000);
    vfr.setProgressListener(new PrintDfsVerifierListener());
//    vfr.setDebugMode(true);
    try {
      vfr.verify(bprog);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
