package il.ac.bgu.cs.bp.bpjs.context.examples.ttt;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

import java.util.Map;
import java.util.Optional;


public class Verifier {

    public static void main(String[] args) throws Exception {
        ContextService contextService = ContextService.getInstance();
        contextService.enableVerificationMode();

        try {
            String[] programs = new String[]{"context.js", "db_population.js", "program.js", "assertions.js", "verification.js"};
            contextService.initFromResources("ContextDB", programs);
            contextService.addContextUpdateListener(new ContextService.UpdateEffect(
                    (EventSet) bEvent -> bEvent.name.equals("X") || bEvent.name.equals("O"),
                    new String[]{"UpdateCell"},
                    e -> Map.of("cell", e.getData(), "val", e.name)));
            BProgram program = contextService.getBProgram();
            DfsBProgramVerifier vrf = new DfsBProgramVerifier();      // ... and a verifier
            vrf.setProgressListener(new PrintDfsVerifierListener());  // add a listener to print progress
//        vrf.setDebugMode(true);
            vrf.addInspection(ExecutionTraceInspections.FAILED_ASSERTIONS);
            VerificationResult res = vrf.verify(program);                  // this might take a while
            if (res.isViolationFound()) {
                for (ExecutionTrace.Entry nd : res.getViolation().get().getCounterExampleTrace().getNodes()) {
                    System.out.println(" " + nd.getEvent());
                }
            }
        } finally {
            contextService.close();
        }
    }
}
