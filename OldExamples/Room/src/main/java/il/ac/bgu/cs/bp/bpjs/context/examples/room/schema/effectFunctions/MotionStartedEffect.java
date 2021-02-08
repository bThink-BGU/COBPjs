package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.effectFunctions;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;

public class MotionStartedEffect extends ContextService.EffectFunction
{
    public MotionStartedEffect() {
        super(bEvent -> bEvent.name.equals("MotionStartedEvent"));
    }

    @Override
    protected void innerExecution(EntityManager em, BEvent e) {
        String roomId = (String) e.maybeData;
        Query q1 = em.createNamedQuery("UpdateMovement");
        ContextService.setParameters(q1, new HashMap<>() {{
            put("roomId", roomId);
        }});
        q1.executeUpdate();
    }
}
