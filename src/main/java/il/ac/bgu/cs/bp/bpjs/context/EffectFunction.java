package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import java.io.Serializable;

public class EffectFunction extends BProgramRunnerListenerAdapter implements Serializable {
    private final Function effect;
    private final String eventName;

    public EffectFunction(Function effect, String eventName) {
        this.effect = effect;
        this.eventName = eventName;
    }


    @Override
    public void eventSelected(BProgram bp, BEvent event) {
        if(event.name.equals(eventName)) {
            Context ctx = Context.enter();
            try {
                effect.call(ctx, bp.getGlobalScope(), bp.getGlobalScope(), new Object[]{bp, event});
            } finally {
                Context.exit();
            }
        }
    }
}
