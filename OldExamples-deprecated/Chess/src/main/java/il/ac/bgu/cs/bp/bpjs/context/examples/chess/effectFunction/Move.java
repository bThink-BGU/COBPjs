package il.ac.bgu.cs.bp.bpjs.context.examples.chess.effectFunction;

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
public class Move extends ContextService.EffectFunction
{
    public Move() {
        super(bEvent -> bEvent.name.equals("Move"));
    }

    @Override
    protected void innerExecution(EntityManager em, BEvent e) {
        Map<String, Cell> data = (Map<String, Cell>) e.maybeData;
        Cell source = data.get("source");
        Cell target = data.get("target");
        Piece sourcePiece = source.piece;
        Piece targetPiece = target.piece;

        Query q1 = em.createNamedQuery("UpdateCell");
        ContextService.setParameters(q1, new HashMap<>() {{
            put("piece", source.piece);
            put("cell", target);
        }});
        q1.executeUpdate();

        Query q2 = em.createNamedQuery("UpdateCell");
        ContextService.setParameters(q2, new HashMap<>() {{
            put("piece", null);
            put("cell", source);
        }});
        q2.executeUpdate();

        if (targetPiece != null) {
            Query q3 = em.createNamedQuery("RemovePiece");
            ContextService.setParameters(q3, new HashMap<>() {{
                put("piece", targetPiece);
            }});
            q3.executeUpdate();
        }

        if (sourcePiece.type.equals(Piece.Type.Pawn)) {
            Query q4 = em.createNamedQuery("UpdatePawnMoved");
            ContextService.setParameters(q4, new HashMap<>() {{
                put("pawn", sourcePiece);
            }});
            q4.executeUpdate();
        }
    }
}
