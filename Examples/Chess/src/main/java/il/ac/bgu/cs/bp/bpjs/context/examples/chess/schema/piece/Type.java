package il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.piece;

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
