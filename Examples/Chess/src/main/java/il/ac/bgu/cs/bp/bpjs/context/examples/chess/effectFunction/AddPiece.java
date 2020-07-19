package il.ac.bgu.cs.bp.bpjs.context.examples.chess.effectFunction;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Cell;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Piece;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

/**
 * Created By: Assaf, On 26/02/2020
 * Description:
 */
public class AddPiece extends ContextService.EffectFunction
{
    public AddPiece() {
        super(bEvent -> bEvent.name.equals("Add Piece"));
    }

    @Override
    protected void innerExecution(EntityManager em, BEvent e) {
        Map<String, Object> data = (Map<String, Object>) e.maybeData;
        Piece piece = (Piece) data.get("piece");
        Cell cell = (Cell) data.get("cell");
        em.persist(piece);
        Query q1 = em.createNamedQuery("UpdateCell");
        ContextService.setParameters(q1, new HashMap<>() {{
            put("piece", piece);
            put("cell", cell);
        }});
        q1.executeUpdate();
    }
}
