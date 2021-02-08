package il.ac.bgu.cs.bp.bpjs.context.examples.room;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.effectFunctions.MarkRoomEffect;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.effectFunctions.MotionStartedEffect;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.effectFunctions.TickEffect;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

import java.util.Map;

public class Verifier {

    public static void main(String[] args) throws Exception {
        ContextService contextService = ContextService.getInstance();
        contextService.enableVerificationMode();

        try {
            String[] programs = new String[]{"context.js", "db_population.js", "program.js", "verification.js"};
            contextService.initFromResources("ContextDB", programs);
            contextService.addEffectFunction(new MotionStartedEffect());
            contextService.addEffectFunction(new MarkRoomEffect());
            contextService.addEffectFunction(new TickEffect());
            BProgram program = contextService.getBProgram();
            DfsBProgramVerifier vrf = new DfsBProgramVerifier();      // ... and a verifier
            vrf.setProgressListener(new PrintDfsVerifierListener());  // add a listener to print progress
//        vrf.setDebugMode(true);
            vrf.addInspection(ExecutionTraceInspections.FAILED_ASSERTIONS);
            vrf.addInspection(ExecutionTraceInspections.DEADLOCKS);
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
