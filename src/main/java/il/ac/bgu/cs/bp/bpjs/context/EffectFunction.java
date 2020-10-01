package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

public class EffectFunction extends BProgramRunnerListenerAdapter {
    private final Function effect;

    public EffectFunction(Function effect) {
        this.effect = effect;
    }

    @Override
    public void eventSelected(BProgram bp, BEvent event) {
        effect.call(Context.getCurrentContext(), bp.getGlobalScope(), bp.getGlobalScope(), new Object[]{event});
    }
}
