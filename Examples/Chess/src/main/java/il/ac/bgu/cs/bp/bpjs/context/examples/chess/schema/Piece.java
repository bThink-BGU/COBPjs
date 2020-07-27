package il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.util.HashMap;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "RemovePiece", query = "DELETE FROM Piece p WHERE p = :piece"),
        @NamedQuery(name = "WhitePieces", query = "SELECT p FROM Piece p WHERE p.color = 'White'"),
        @NamedQuery(name = "BlackPieces", query = "SELECT p FROM Piece p WHERE p.color = 'Black'"),
})
public class Piece extends BasicEntity
{
    public enum Type
    {
        Pawn,Knight,Bishop,Rook,Queen,King
    }

    public enum Color
    {
        Black,White
    }

    @Enumerated(EnumType.STRING)
    @Column
    public final Type type;
    @Enumerated(EnumType.STRING)
    @Column
    public final Color color;
    @Column
    public final int counter;
    @OneToOne(mappedBy = "piece")
    public final Cell cell;

    private static HashMap<Type,Integer> whiteCounter = new HashMap<>();
    private static HashMap<Type,Integer> blackCounter = new HashMap<>();

    public void updateLocation() {
        //TODO: call db.
    }

    public Piece()
    {
        super();
        color = null;
        counter = 0;
        type = null;
        cell = null;
    }

    public Piece(Type type, Color color)
    {
        super(type + "-" + color + "-" + ((color.equals(Color.White) ? whiteCounter : blackCounter).get(type) != null ? (color.equals(Color.White) ? whiteCounter : blackCounter).get(type) : 1));

        HashMap<Type,Integer> map = (color.equals(Color.White) ? whiteCounter : blackCounter);
        if(!map.containsKey(type))
        {
            this.counter = 1;
            map.put(type,2);
        }
        else
        {
            this.counter = map.get(type);
            map.replace(type, this.counter + 1);
        }

        this.type = type;
        this.color = color;
        this.cell = null;
    }
}
