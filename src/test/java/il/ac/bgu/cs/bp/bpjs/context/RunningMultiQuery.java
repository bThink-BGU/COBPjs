package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class RunningMultiQuery
{
    public static void main(String[] args)
    {
//        BProgram bprog = TestUtils.prepareBProgram("TestingWholeDbQuery/activeOnlyIfInContext.js");
        BProgram bprog = TestUtils.prepareBProgramWithPriority("TestCases/checkingPriority.js");
        BProgramRunner rnr = new BProgramRunner(bprog);
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.run();

    }

}
