package il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema;

import javax.persistence.*;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Pawns", query = "SELECT p FROM Pawn p"),
        @NamedQuery(name = "UnmovedPawns", query = "SELECT p FROM Pawn p WHERE p.didMove = FALSE"),
        @NamedQuery(name = "WhitePawns", query = "SELECT p FROM Pawn p WHERE p.color = 'White'"),
        @NamedQuery(name = "BlackPawns", query = "SELECT p FROM Pawn p WHERE p.color = 'Black'"),
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

    public Pawn(Color color)
    {
        super(Type.Pawn, color);
    }
}
