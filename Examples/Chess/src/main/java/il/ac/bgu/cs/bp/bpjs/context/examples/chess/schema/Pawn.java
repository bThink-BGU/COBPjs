package il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.util.HashMap;

/**
 * Created By: Assaf, On 17/02/2020
 * Description:
 */
@Entity
@NamedQueries(value = {
        @NamedQuery(name = "UnmovedPawns", query = "SELECT p FROM Pawn p WHERE didMove = false "),
        @NamedQuery(name = "WhitePawns", query = "SELECT p FROM Pawn p WHERE p.color = 'White'"),
        @NamedQuery(name = "BlackPawn", query = "SELECT p FROM Pawn p WHERE p.color = 'Black'"),
        @NamedQuery(name = "UpdatePawnMoved", query = "UPDATE Pawn p SET p.didMove=TRUE WHERE p=:pawn"),
})
public class Pawn extends Piece
{
    @Column
    public final boolean didMove = false;

    public Pawn()
    {
        super();
    }

    public Pawn(Type type, Color color)
    {
        super(type, color);
    }
}
