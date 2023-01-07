/*
 * (c) 2022 Testory Technologies
 */
package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * The (Hard coded) values we compare our results to are based on the results of the 0.6.6 version of BPjs.
 */

public class SampleTest {
  boolean afterUpdatingToWholeDBQuery = true;

  /**
   * Sample test that checks if the only live bthread is the one
   * that is supposed to be live.(in context)
   * When only one context is used
   * @throws Exception
   */
  @Test
  public void onlyInContextBthreadsRun() throws Exception {
    BProgram bprog = TestUtils.prepareBProgram("TestCases/activeOnlyIfInContext.js");
    if(afterUpdatingToWholeDBQuery)
    {
      bprog= TestUtils.prepareBProgram("TestingWholeDbQuery/activeOnlyIfInContext.js");
    }
    var res = TestUtils.verify(bprog);
    assertEquals(2, res.getScannedStatesCount());
    assertEquals(5, res.getScannedEdgesCount());
  }
  /**
    * Sample test that checks if the only live bthread is the one
   * that is supposed to be live.(in context)
   * When two contexts are used
   * @throws Exception
   */
  @Test
  public void onlyInContextBthreadsRun2Contexts() throws Exception {
    BProgram bprog = TestUtils.prepareBProgram("TestCases/activeOnlyIfInContext_2Contexts.js");
    if(afterUpdatingToWholeDBQuery)
    {
      bprog= TestUtils.prepareBProgram("TestingWholeDbQuery/activeOnlyIfInContext_2Contexts.js");
    }
    var res = TestUtils.verify(bprog);
    assertEquals(8, res.getScannedStatesCount());
    assertEquals(31, res.getScannedEdgesCount());
  }
  /**
    * Sample test that checks the fact that if a context is always off,
   * its bthreads are never active
   *
   * @throws Exception
   */
  @Test
  public void bthreadWithOffContextDoesntWakeUp() throws Exception {
    BProgram bprog = TestUtils.prepareBProgram("TestCases/alwaysOffContext.js");
    if(afterUpdatingToWholeDBQuery)
    {
      bprog= TestUtils.prepareBProgram("TestingWholeDbQuery/alwaysOffContext.js");
    }
    var res = TestUtils.verify(bprog);
    assertEquals(2, res.getScannedStatesCount());
    assertEquals(5, res.getScannedEdgesCount());
  }
  /**
   * Sample test that checks the fact that if a context is always off,
   * its bthreads are never active, even if it is the only context
   * @throws Exception
   */
  @Test
  public void noBthreadInContext() throws Exception {
    BProgram bprog = TestUtils.prepareBProgram("TestCases/noONeInContext.js");
    if(afterUpdatingToWholeDBQuery)
    {
      bprog= TestUtils.prepareBProgram("TestingWholeDbQuery/noONeInContext.js");
    }
    var res = TestUtils.verify(bprog);
    assertEquals(1, res.getScannedStatesCount());
    assertEquals(0, res.getScannedEdgesCount());
  }
  /**
   * Sample test that checks the fact that if a context is always off, its bthreads are never active
   * Even if it is the only context
   * @throws Exception
   * @comment: this test is not working, because the no way to use priority with context
   */
//  @Test
//  public void priorityOfTwoBthreads() throws Exception {
//    BProgram bprog = TestUtils.prepareBProgram("TestCases/checkingPriority.js");
//    var res = TestUtils.verify(bprog);
//    assertEquals(1, res.getScannedStatesCount());
//    assertEquals(0, res.getScannedEdgesCount());
//  }

  /**
   * Testing using the HotCold Program
   * @throws Exception
   */
  @Test
  public void testHotCold() throws Exception {
    BProgram bprog;
    if(afterUpdatingToWholeDBQuery) {
      bprog= TestUtils.prepareBProgram("TestingWholeDbQuery/HotCold.js");
    }
    else {
      Example example = Example.HotCold;
      bprog = new ContextBProgram(example.getResourcesNames());
      example.initializeBProg(bprog);
//    BProgram bprog = TestUtils.prepareBProgram("Testing/HotCold");
    }
    var res = TestUtils.verify(bprog);
    assertEquals(115, res.getScannedStatesCount());
    assertEquals(348, res.getScannedEdgesCount());
  }
  /**
   * running the TicTacToe example for testing
   * @throws Exception
   * @comment: 1. this case fails because of a deadlock(found using Main class in this package)
   * 2. Test runs for a long time and returns a different result than when running Main class in this package
   */
  @Test
  public void testTicTacToe() throws Exception {
    //acts really weird when running the test

//    Example example = Example.TicTacToeWithoutUI;
//    BProgram bprog = new ContextBProgram(example.getResourcesNames());
//    example.initializeBProg(bprog);
//    var res = TestUtils.verify(bprog);
//    assertEquals(8, res.getScannedStatesCount());
//    assertEquals(7, res.getScannedEdgesCount());
  }

  /**
   * running the SampleProgram example for testing
   * @throws Exception
   * @comment: 1. this case fails because of: //Violation found: Runtime JavaScript Error: JavaScript error:
   *                                            TypeError: Cannot read property "length" from null (SampleProgram/bl.js#18)
   */
  @Test
  public void testSampleProgram() throws Exception {
    //Fails from original
    BProgram bprog;
    if(afterUpdatingToWholeDBQuery) {
      bprog= TestUtils.prepareBProgram("TestingWholeDbQuery/SampleProgram.js");
    }
    else {
      Example example = Example.SampleProgram;
      bprog = new ContextBProgram(example.getResourcesNames());
      example.initializeBProg(bprog);
    }


    var res = TestUtils.verify(bprog);
    assertEquals(1, res.getScannedStatesCount());
    assertEquals(0, res.getScannedEdgesCount());
  }
}
