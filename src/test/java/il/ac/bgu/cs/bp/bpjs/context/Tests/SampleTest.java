/*
 * (c) 2022 Testory Technologies
 */
package il.ac.bgu.cs.bp.bpjs.context.Tests;


import il.ac.bgu.cs.bp.bpjs.context.ContextBProgram;
import il.ac.bgu.cs.bp.bpjs.context.Example;
import il.ac.bgu.cs.bp.bpjs.context.TestUtils;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleTest {
    boolean afterUpdatingToWholeDBQuery = false;//this is true when using the whole DB query, and false when using the single query
    //I.e., when the code in context.js is updated to use the whole DB query, this should be true.


    /**
     * Sample test that checks if the only live bthread is the one
     * that is supposed to be live.(in context)
     * When only one context is used
     *
     * @throws Exception -- if the test fails
     */
    @Test
    public void onlyInContextBthreadsRun() throws Exception {
        String bprogName = "TestCases/activeOnlyIfInContext.js";
        if (afterUpdatingToWholeDBQuery) {
            bprogName = "TestingWholeDbQuery/activeOnlyIfInContext.js";
        }
        BProgram bprog = TestUtils.prepareBProgram(bprogName);
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
    @Test
    public void onlyInContextBthreadsRun2Contexts() throws Exception {
        String bprogName = "TestCases/activeOnlyIfInContext_2Contexts.js";
        if (afterUpdatingToWholeDBQuery) {
            bprogName = "TestingWholeDbQuery/activeOnlyIfInContext_2Contexts.js";
        }
        BProgram bprog = TestUtils.prepareBProgram(bprogName);
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
    @Test
    public void bthreadWithOffContextDoesntWakeUp() throws Exception {
        String bprogName = "TestCases/alwaysOffContext.js";
        if (afterUpdatingToWholeDBQuery) {
            bprogName = "TestingWholeDbQuery/alwaysOffContext.js";
        }
        BProgram bprog = TestUtils.prepareBProgram(bprogName);
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
    @Test
    public void noBthreadInContext() throws Exception {
        String bprogName = "TestCases/noOneInContext.js";
        if (afterUpdatingToWholeDBQuery) {
            bprogName = "TestingWholeDbQuery/noOneInContext.js";
        }
        BProgram bprog = TestUtils.prepareBProgram(bprogName);
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
    @Test
    public void priorityOfTwoBthreads() throws Exception {
        String bprogName = "TestCases/checkingPriority.js";
        if (afterUpdatingToWholeDBQuery) {
            bprogName = "TestingWholeDbQuery/checkingPriority.js";
        }
        BProgram bprog = TestUtils.prepareBProgramWithPriority(bprogName);
        var res = TestUtils.verify(bprog);
        assertEquals(3, res.getScannedStatesCount());
        assertEquals(2, res.getScannedEdgesCount());
    }

    /**
     * Testing using the HotCold Program
     *
     * @throws Exception -- if the test fails
     */
    @Test
    public void testHotCold() throws Exception {
        BProgram bprog;
        if (afterUpdatingToWholeDBQuery) {
            bprog = TestUtils.prepareBProgram("TestingWholeDbQuery/HotCold.js");
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

}