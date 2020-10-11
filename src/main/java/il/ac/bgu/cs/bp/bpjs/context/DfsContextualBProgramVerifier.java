package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;

import java.util.ArrayList;

public class DfsContextualBProgramVerifier extends DfsBProgramVerifier {
    public DfsContextualBProgramVerifier(){
        super();
        this.listeners = new ArrayList<>();
    }

    public <R extends BProgramRunnerListener> R addListener(R aListener) {
        this.listeners.add(aListener);
        return aListener;
    }
}
