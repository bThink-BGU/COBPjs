/*
 * (c) 2022 Testory Technologies
 */
package il.ac.bgu.cs.bp.bpjs.context.Tests;


import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.context.ContextBProgram;
import il.ac.bgu.cs.bp.bpjs.context.Example;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class Tests {


    private BProgram createBProgramForTest(String resource, boolean usingWholeDBQuery) {

        String bprogName = "TestingResources/TestCases/" +resource;
        String bprogHelperName = "TestingResources/TestCases/helpFunctions.js";
        BProgram bprog = TestUtils.prepareBProgramWithPriority(bprogHelperName, bprogName);
        bprog.putInGlobalScope("usingWholeDBQuery", usingWholeDBQuery);
        return bprog;
    }



    static Stream<Boolean> valueSource() {
        return Stream.of(true, false);
    }
    /**
     * Sample test that checks if the only live bthread is the one
     * that is supposed to be live.(in context)
     * When only one context is used
     *
     * @throws Exception -- if the test fails
     */
    @ParameterizedTest
    @MethodSource("valueSource")
    public void onlyInContextBThreadsRun(boolean usingWholeDBQuery) throws Exception {

        BProgram bprog = createBProgramForTest("activeOnlyIfInContext.js", usingWholeDBQuery);
        var res = TestUtils.verify(bprog);
        assertEquals(2, res.getScannedStatesCount());
        assertEquals(5, res.getScannedEdgesCount());
    }

    /**
     * Sample test that checks if the only live bthread is the one
     * that is supposed to be live.(in context)
     * When two contexts are used
     *
     * @throws Exception-- if the test fails
     */
    @ParameterizedTest
    @MethodSource("valueSource")
    public void onlyInContextBThreadsRun2Contexts(boolean usingWholeDBQuery) throws Exception {
        BProgram bprog = createBProgramForTest("activeOnlyIfInContext_2Contexts.js", usingWholeDBQuery);
        var res = TestUtils.verify(bprog);
        assertEquals(8, res.getScannedStatesCount());
        assertEquals(31, res.getScannedEdgesCount());
    }

    /**
     * Sample test that checks the fact that if a context is always off,
     * its bthreads are never active
     *
     * @throws Exception-- if the test fails
     */
    @ParameterizedTest
    @MethodSource("valueSource")
    public void bthreadWithOffContextDoesntWakeUp(boolean usingWholeDBQuery) throws Exception {

        BProgram bprog = createBProgramForTest("alwaysOffContext.js", usingWholeDBQuery);
        var res = TestUtils.verify(bprog);
        assertEquals(2, res.getScannedStatesCount());
        assertEquals(5, res.getScannedEdgesCount());
    }

    /**
     * Sample test that checks the fact that if a context is always off,
     * its bthreads are never active, even if it is the only context
     *
     * @throws Exception-- if the test fails
     */
    @ParameterizedTest
    @MethodSource("valueSource")
    public void noBThreadInContext(boolean usingWholeDBQuery) throws Exception {

        BProgram bprog = createBProgramForTest("noOneInContext.js", usingWholeDBQuery);
        var res = TestUtils.verify(bprog);
        assertEquals(1, res.getScannedStatesCount());
        assertEquals(0, res.getScannedEdgesCount());
    }

    /**
     * Sample test that checks the fact that if a context is always off, its bthreads are never active
     * Even if it is the only context
     *
     * @throws Exception -- if the test fails
     * @comment: this test is not working, because the no way to use priority with context
     */
//  @ParameterizedTest
//  @ValueSource(booleans = {true, false})
    @ParameterizedTest
    @MethodSource("valueSource")
    public void priorityOfTwoBThreads(boolean usingWholeDBQuery) throws Exception {

        BProgram bprog = createBProgramForTest("checkingPriority.js", usingWholeDBQuery);
        var res = TestUtils.verify(bprog);
        assertEquals(3, res.getScannedStatesCount());
        assertEquals(2, res.getScannedEdgesCount());
    }

    /**
     * Testing using the HotCold Program
     *
     * @throws Exception -- if the test fails
     */
    @ParameterizedTest
    @MethodSource("valueSource")
    public void testHotCold(boolean usingWholeDBQuery) throws Exception {
        BProgram bprog;
        if (usingWholeDBQuery) {
            bprog = TestUtils.prepareBProgram("TestingResources/TestCases/HotCold.js");
        } else {
            Example example = Example.HotCold;
            bprog = new ContextBProgram(example.getResourcesNames());
            example.initializeBProg(bprog);
//    BProgram bprog = TestUtils.prepareBProgram("Testing/HotCold");
        }
        var res = TestUtils.verify(bprog);
        assertEquals(115, res.getScannedStatesCount());
        assertEquals(348, res.getScannedEdgesCount());
    }

    public static class TestUtils {

        public static ContextBProgram prepareBProgram(String ... resourceName) {
            return new ContextBProgram(resourceName);
        }
        public static ContextBProgram prepareBProgramWithPriority(String ... resourceName) {
            ContextBProgram bprog = new ContextBProgram(resourceName);
            PrioritizedBSyncEventSelectionStrategy priority = new PrioritizedBSyncEventSelectionStrategy();
            priority.setDefaultPriority(0);
            bprog.setEventSelectionStrategy(priority);
            //        BProgramRunner rnr = new BProgramRunner(bprog);
            return bprog;
        }


        public static void runBProgram(BProgram bprog) {
            var rnr = new BProgramRunner(bprog);
            rnr.addListener(new PrintBProgramRunnerListener());
            rnr.run();
        }


        public static String eventNamesString(List<BEvent> trace, String delimiter) {
            return trace.stream()
                    .map(BEvent::getName)
                    .collect(joining(delimiter));
        }

        public static String eventNamesString(List<BEvent> trace) {
            return eventNamesString(trace, "");
        }

        /**
         * Verify the bprogram with all standard BPjs-based inspections. Fail on any violation.
         * @param bprog The b-program to be verified.
         * @throws Exception
         */
        public static VerificationResult verify(BProgram bprog) throws Exception{
            var vfr = new DfsBProgramVerifier(bprog);
            vfr.addInspection( ExecutionTraceInspections.FAILED_ASSERTIONS );
            vfr.addInspection( ExecutionTraceInspections.HOT_SYSTEM );
            vfr.addInspection( ExecutionTraceInspections.HOT_TERMINATIONS  );
            vfr.addInspection( ExecutionTraceInspections.HOT_BTHREADS  );

            var res = vfr.verify(bprog);

            if ( res.isViolationFound() ) {
                final Violation violation = res.getViolation().get();
                System.out.println("Violation found: " + violation.decsribe() );
                String trace = violation.getCounterExampleTrace().getNodes().stream()
                        .map( n -> n.getEvent() ).filter( eo -> eo.isPresent() )
                        .map( Optional::get ).map(e->e.toString()).collect( joining("\n"));
                fail("Violation found " + violation.decsribe() + "\n" + trace );
            }
            return res;
        }


        /**
         * Checks whether {@code sought} is equal to {@code list}, after the latter is
         * filtered to contain only members of former.
         * <p>
         * e.g.:
         * <code>
         * 1,2,3,4 is embedded in a,1,d,2,g,r,cv,3,g,4
         * a,b,x is not embedded in x,a,b
         * a,b,x is not embedded in a,a,b,x
         * </code>
         *
         * @param <T>
         * @param sought
         * @param list
         * @return
         */
        public static <T> boolean isEmbeddedSublist(List<T> sought, List<T> list) {
            Set<T> filter = new HashSet<>(sought);
            return list.stream()
                    .filter(filter::contains)
                    .collect(toList()).equals(sought);
        }

        public static String getPath(String pathPrefix, String pathSuffix) {
            String path = pathPrefix + pathSuffix;
            return FileSystems.getDefault().getPath(path).toAbsolutePath().toString();
        }
    }
}