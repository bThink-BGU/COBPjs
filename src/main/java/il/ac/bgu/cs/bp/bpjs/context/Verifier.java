package il.ac.bgu.cs.bp.bpjs.context;

import com.google.devtools.common.options.OptionsParser;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;

public class Verifier {
    
    public static BProgramOptions options;
    
    public static void main(final String[] args) throws Exception {
        final OptionsParser parser = OptionsParser.newOptionsParser(BProgramOptions.class);
        parser.parseAndExitUponError(args);
        options = parser.getOptions(BProgramOptions.class);
        options.path = "examples/room";

        System.out.println("\nRunning tests from path: " + options.path);
        final ContextBProgram bprog = new ContextBProgram(options.path);
        bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategyWithDefault());
        ContextService contextService = ContextService.CreateInstance(bprog, null);
        DfsBProgramVerifier vrf = new DfsBProgramVerifier();      // ... and a verifier
        vrf.setIterationCountGap(200);
        vrf.setProgressListener(new PrintDfsVerifierListener());  // add a listener to print progress
//        vrf.setDebugMode(true);
//        vrf.addInspection(ExecutionTraceInspections.FAILED_ASSERTIONS);
        vrf.addInspection(ExecutionTraceInspections.DEADLOCKS);
        VerificationResult res = vrf.verify(bprog);                  // this might take a while
        System.out.println("# state = " + res.getScannedStatesCount());
        System.out.println("time in millis = " + res.getTimeMillies());
        if (res.isViolationFound()) {
            for (ExecutionTrace.Entry nd : res.getViolation().get().getCounterExampleTrace().getNodes()) {
                System.out.println(" " + nd.getEvent());
            }
        }
    }
    
}
