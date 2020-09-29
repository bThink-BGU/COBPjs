package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.effectFunctions;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms.Room;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;

/**
 * Created By: Assaf, On 26/02/2020
 * Description:
 */
public class TickEffect extends ContextService.EffectFunction
{
    public TickEffect() {
        super(bEvent -> bEvent.name.equals("tick"));
    }

    @Override
    protected void innerExecution(EntityManager em, BEvent e) {
        Query q1 = em.createNamedQuery("Tick");
        q1.executeUpdate();
    }
}
