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
public class MarkRoomEffect extends ContextService.EffectFunction
{
    public MarkRoomEffect() {
        super(bEvent -> bEvent.name.startsWith("MarkRoomAs"));
    }

    @Override
    protected void innerExecution(EntityManager em, BEvent e) {
        Room room = (Room) e.maybeData;
        Query q1 = em.createNamedQuery(e.name);
        ContextService.setParameters(q1, new HashMap<>() {{
            put("room", room);
        }});
        q1.executeUpdate();
    }
}
