package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class Verification {
  public static void main(final String[] args) {
    BProgram bprog = new ContextBProgram("SampleContextualProgram.js");
    DfsBProgramVerifier vfr = new DfsBProgramVerifier();
    vfr.setProgressListener( new PrintDfsVerifierListener() );
    try {
      vfr.verify(bprog);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
