package il.ac.bgu.cs.bp.bpjs.context.examples.ttt;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.BriefPrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;

public class Verifier {
    public static void main(String[] args) throws Exception {
        ContextService contextService = ContextService.getInstance();
        contextService.init("ContextDB");
        BProgram program = new ResourceBProgram("context.js","db_population.js", "program.js", "runtime_assertions.js");
        DfsBProgramVerifier vrf = new DfsBProgramVerifier();           // ... and a verifier
        vrf.setProgressListener(new BriefPrintDfsVerifierListener());  // add a listener to print progress
        VerificationResult res = vrf.verify(program);                  // this might take a while

        if(res.isViolationFound()) {
            System.out.println(res.getViolation());
        }

    }
}
