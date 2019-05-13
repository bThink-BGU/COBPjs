package il.ac.bgu.cs.bp.bpjs.context.examples.chess.events;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.MoveTranslator;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Cell;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.piece.Color;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.piece.Piece;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

public class Move extends BEvent {
    public final Cell source;
    public final Cell target;
    public final Piece piece;

    public Move(Cell source, Cell target, Piece piece){
        this.source = source;
        this.target = target;
        this.piece = piece;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isInstance(obj)) {
            return false;
        }
        Move other = (Move) obj;
        return source.equals(other.source) && target.equals(other.target) && piece.equals(other.piece);
    }

    @Override
    public String toString() {
        String move= MoveTranslator.MoveToString(this);
        return "Move(" +move.charAt(0)+move.charAt(1)+")->("+move.charAt(2)+move.charAt(3)+"),"+ piece ;
    }

    public static class AnyMoveEventSet implements EventSet {
        @Override
        public boolean contains(BEvent bEvent) {
            return bEvent instanceof Move;
        }
    }

    public static class SamePlaceMoveEventSet implements EventSet {
        @Override
        public boolean contains(BEvent bEvent) {
            //TODO: change to equals
            return bEvent instanceof Move && ((Move)bEvent).source.i==((Move)bEvent).target.i && ((Move)bEvent).source.j==((Move)bEvent).target.j;
        }
    }

    public static class OutOfBoardMoveEventSet implements EventSet {
        @Override
        public boolean contains(BEvent bEvent) {
            return bEvent instanceof Move && (((Move)bEvent).target.i<0 || ((Move)bEvent).target.i>7 || ((Move)bEvent).target.j<0 || ((Move)bEvent).target.j>7);
        }
    }

    public static class PieceMoveEventSet implements EventSet {
        private final Piece p;

        public PieceMoveEventSet(Piece p) {
            this.p = p;
        }

        @Override
        public boolean contains(BEvent bEvent) {
            return bEvent instanceof Move && ((Move)bEvent).piece.equals(p);
        }
    }

    public static class ColorMoveEventSet implements EventSet {
        private final Color c;

        public ColorMoveEventSet(Color c) {
            this.c = c;
        }

        @Override
        public boolean contains(BEvent bEvent) {
            return bEvent instanceof Move && ((Move)bEvent).piece.color.equals(c);
        }
    }

    public static class AnnounceEventSet implements EventSet {

        @Override
        public boolean contains(BEvent bEvent) {
            return bEvent.name.equals("EngineTurn") ||bEvent.name.equals("MyTurn") || bEvent.name.equals("Update Ended") ;

        }
    }

    public static class EngineEventSet implements EventSet {

        @Override
        public boolean contains(BEvent bEvent) {
           return bEvent.name.split("-")[0].equals("input");
        }
    }

    public static class ContextEventSet implements EventSet {

        @Override
        public boolean contains(BEvent bEvent) {

            return bEvent.name.contains("ContextEndedEvent") || bEvent.name.contains("NewContextEvent") ;
        }
    }
    public static class SpecificMoveEventSet implements EventSet {
        private final Piece p;
        private final int i;
        private final int j;


        public SpecificMoveEventSet(Piece p, int i, int j) {
            this.p = p;
            this.i = i;
            this.j = j;
        }

        @Override
        public boolean contains(BEvent bEvent) {
            return bEvent instanceof Move && ((Move)bEvent).piece.equals(p) && ((Move)bEvent).target.i==i && ((Move)bEvent).target.j==j;
        }
    }
}
