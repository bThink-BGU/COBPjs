package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspection;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class Verifier {

    public static void main(String[] args) throws Exception {
        ContextService contextService = ContextService.getInstance();
        contextService.enableVerificationMode();

        try {
            String[] programs = new String[]{"ContextualChess-Population.js", "ContextualChess.js", "assertions.js", "verification.js"};
            contextService.initFromResources("ContextDB", programs);
            BProgram program = contextService.getBProgram();
            DfsBProgramVerifier vrf = new DfsBProgramVerifier();           // ... and a verifier
            vrf.setProgressListener(new PrintDfsVerifierListener());  // add a listener to print progress
//        vrf.setDebugMode(true);
            vrf.addInspection(ExecutionTraceInspections.FAILED_ASSERTIONS);
            VerificationResult res = vrf.verify(program);                  // this might take a while

            if (res.isViolationFound()) {
                System.out.println(res.getViolation());
            }
        } finally {
            contextService.close();
        }

        /*
        /v/ context.js+context.js+db_population.js+program.js+assertions.js+verification.js+internal_context_verification.js: iterations: 1000 statesHit: 521
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
         */
    }
}