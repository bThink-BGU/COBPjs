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
 * A Test class whose main purpose is to examine testing techniques.
 *
 * @author michael
 */
public class SampleTest {

  /**
   * Sample test that records the BProgramSyncSnapshots.
   *
   * @throws IOException
   */
  @Test
  public void testBProgramSyncSnapshots() throws Exception {
    BProgram bprog = TestUtils.prepareBProgram("Testing/HotCold"); // bprogram's source file is in src/test/resources/sample-bprog.js
    var res = TestUtils.verify(bprog);
    assertEquals(10, res.getScannedStatesCount());
    assertEquals(20, res.getScannedEdgesCount());
  }
}
