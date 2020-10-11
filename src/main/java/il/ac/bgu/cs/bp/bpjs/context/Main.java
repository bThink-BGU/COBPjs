package il.ac.bgu.cs.bp.bpjs.context;

import com.google.devtools.common.options.OptionsParser;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;

import java.io.IOException;

/**
 * Code to invoke the , run the b-threads, and generate the Gherkin file.
 */
public class Main {

    // To be set by the test script //////////////
    public static String testname = "testname";
    ///////////////////////////////////////////////

    public static BProgramOptions options = new BProgramOptions();

    public static void main(final String[] args) throws IOException {
        final OptionsParser parser = OptionsParser.newOptionsParser(BProgramOptions.class);
        parser.parseAndExitUponError(args);
        options = parser.getOptions(BProgramOptions.class);
        System.out.println("\nRunning example from path: " + options.path);
        final ContextBProgram bprog = new ContextBProgram(options.path);
        bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategyWithDefault());
        final BProgramRunner rnr = new BProgramRunner(bprog);
        ContextService.CreateInstance(bprog, rnr, null);

        rnr.addListener(new PrintBProgramRunnerListener());

        rnr.run();
    }

}
