package il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.piece.Piece;

import javax.persistence.*;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Cell", query = "SELECT c FROM Cell c"),
        @NamedQuery(name = "NonEmptyCell", query = "SELECT c FROM Cell c WHERE not(c.piece is null)"),
        @NamedQuery(name = "EmptyCell", query = "SELECT c FROM Cell c WHERE c.piece is null"),
        @NamedQuery(name = "SpecificCell", query = "SELECT c FROM Cell c WHERE c.i=:i AND c.j=:j"),
        @NamedQuery(name = "CellWithPiece", query = "SELECT c FROM Cell c WHERE c.piece=:p"),
        @NamedQuery(name = "UpdateCell", query = "Update Cell c set c.piece=:piece where c=:cell"),
        @NamedQuery(name = "CellWithColor", query = "SELECT c FROM Cell c WHERE c.piece.color=:color"),
        @NamedQuery(name = "CellWithType", query = "SELECT c FROM Cell c WHERE c.piece.type=:type"),
//        @NamedQuery(name = "DeleteCell", query = "DELETE FROM Cell c Where c=:cell"),
})

public class Cell extends BasicEntity {
    @Column
    public final int i;
    @Column
    public final int j;
    @OneToOne
    public Piece piece;

    public void setPiece (Piece p){
        this.piece = p;
    }
    protected Cell() {
        this(0, 0, null);
    }

    public Cell(int i, int j) {
        this(i, j, null);
    }

    public Cell(int i, int j, Piece p) {
        super("Cell(" + i + "," + j + ")");
        this.i = i;
        this.j = j;
        this.piece = p;
    }

    @Override
    public String toString() {
        String ans="Cell(" + (char) ('a' + i)  + (j+1) + "):";
        if(piece == null){
            return ans+"Empty";
        }
        return ans+ piece;
    }
}