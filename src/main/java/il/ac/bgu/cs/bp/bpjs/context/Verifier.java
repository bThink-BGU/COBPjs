package il.ac.bgu.cs.bp.bpjs.context;

import com.google.devtools.common.options.OptionsParser;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;

public class Verifier {
    
    public static void main(final String[] args) throws Exception {
        final OptionsParser parser = OptionsParser.newOptionsParser(BProgramOptions.class);
        parser.parseAndExitUponError(args);
        BProgramOptions options = parser.getOptions(BProgramOptions.class);
		Main.options.path = "examples/room";
        System.out.println("\nRunning tests from path: " + Main.options.path);
        final ContextBProgram bprog = new ContextBProgram(Main.options.path);
        bprog.setEventSelectionStrategy(new CtxEventSelectionStrategy());
        DfsContextualBProgramVerifier vfr = new DfsContextualBProgramVerifier();
        ContextService contextService = ContextService.CreateInstance(bprog, null, vfr);
        vfr.setIterationCountGap(50);
        vfr.setProgressListener(new PrintDfsVerifierListener());  // add a listener to print progress
//        vrf.setDebugMode(true);
        vfr.addInspection(ExecutionTraceInspections.FAILED_ASSERTIONS);
        vfr.addInspection(ExecutionTraceInspections.DEADLOCKS);
        VerificationResult res = vfr.verify(bprog);                  // this might take a while
        System.out.println("# state = " + res.getScannedStatesCount());
        System.out.println("time in millis = " + res.getTimeMillies());
        if (res.isViolationFound()) {
            for (ExecutionTrace.Entry nd : res.getViolation().get().getCounterExampleTrace().getNodes()) {
                System.out.println(" " + nd.getEvent());
            }
        }
    }
    
}
