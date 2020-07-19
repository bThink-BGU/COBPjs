package il.ac.bgu.cs.bp.bpjs.context.examples.chess.effectFunction;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Cell;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import javax.persistence.EntityManager;

/**
 * Created By: Assaf, On 26/02/2020
 * Description:
 */
public class AddCell extends ContextService.EffectFunction
{
    public AddCell() {
        super(bEvent -> bEvent.name.equals("Add Cell"));
    }

    @Override
    protected void innerExecution(EntityManager em, BEvent e) {
        Cell cell = (Cell) e.maybeData;
        em.persist(cell);
    }
}
