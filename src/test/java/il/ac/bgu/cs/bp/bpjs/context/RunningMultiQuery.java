package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.context.Tests.SampleTest;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.io.IOException;


public class RunningMultiQuery {
    public static void main(String[] args) throws IOException {
//        BProgram bprog = TestUtils.prepareBProgram("TestingMultiQuery/startContext.js");
//        BProgram bprog = SampleTest.TestUtils.prepareBProgram("TestingResources/TestingMultiQuery/endContext.js");
//        BProgram bprog = SampleTest.TestUtils.prepareBProgram("TestingResources/TestingMultiQuery/startContext.js");
//        BProgram bprog = SampleTest.TestUtils.prepareBProgram("TestingResources/TestingMultiQuery/togglingContextConstantly.js");
//        BProgram bprog = TestUtils.prepareBProgram("TestingWholeDbQuery/activeOnlyIfInContext.js");
//        BProgram bprog = TestUtils.prepareBProgram("TestingMultiQuery/togglingContextConstantly.js");
        BProgram bprog = SampleTest.TestUtils.prepareBProgram("TestingResources/TestCases/tic-tac-toe-MultiQuery.js");
        BProgramRunner rnr = new BProgramRunner(bprog);
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.run();
    }
}
