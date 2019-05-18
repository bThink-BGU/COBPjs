package il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.BasicEntity;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Color;

import javax.persistence.*;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Piece", query = "SELECT p FROM Piece p"),
        @NamedQuery(name = "PieceOfType", query = "SELECT p FROM Piece p WHERE p.type =:type"),
})
public class Piece extends BasicEntity {
    public Piece() {
        this(null, null, -1);
    }

    @Enumerated(EnumType.STRING)
    public final Color color;

    @Enumerated(EnumType.STRING)
    public final Type type;


    public Piece(Color color, Type type, int id) {
        super(color + "_" + type + "_" + id);
        this.color = color;
        this.type = type;
    }

    public enum Type {
        King(1),
        Queen(1),
        Pawn(8),
        Rook(2),
        Bishop(2),
        Knight(2);

        public final int Count;
        Type(int count) {
            this.Count = count;
        }
    }
}

