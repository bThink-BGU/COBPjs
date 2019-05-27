package il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema;

import javax.persistence.*;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Cell", query = "SELECT c FROM Cell c"),
        @NamedQuery(name = "NonEmptyCell", query = "SELECT c FROM Cell c WHERE not(c.piece is null)"),
        @NamedQuery(name = "EmptyCell", query = "SELECT c FROM Cell c WHERE c.piece is null"),
        @NamedQuery(name = "SetPiece", query = "Update Cell c set c.piece=:piece where c=:cell"),
        @NamedQuery(name = "CellWithPiece", query = "Select c FROM Cell c Where c.piece=:piece"),
})

public class Cell extends BasicEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Game game;

    @Column
    public final int i;

    @Column
    public final int j;
    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    public Piece piece;
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

    void setGame(Game game) {
        this.game = game;
    }

    @Override
    public String toString() {
        String ans="Cell(" + (char)('a' + j)  + (8 - i) + "):";
        if(piece == null){
            return ans+"Empty";
        }
        return ans+ piece;
    }
}