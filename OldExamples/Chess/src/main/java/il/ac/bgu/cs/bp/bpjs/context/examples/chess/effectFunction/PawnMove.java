package il.ac.bgu.cs.bp.bpjs.context.examples.chess.effectFunction;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Cell;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Piece;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

public class PawnMove extends ContextService.EffectFunction
{
    public PawnMove() {
        super(bEvent -> bEvent.name.equals("Move") && ((Map<String, Cell>)bEvent.maybeData).get("source").piece.type.equals(Piece.Type.Pawn));
    }

    @Override
    protected void innerExecution(EntityManager em, BEvent e) {
        Map<String, Cell> data = (Map<String, Cell>) e.maybeData;
        Cell source = data.get("source");
        Query q1 = em.createNamedQuery("UpdatePawnMoved");
        ContextService.setParameters(q1, new HashMap<>() {{
            put("pawn", source.piece);
        }});
        q1.executeUpdate();
    }
}
