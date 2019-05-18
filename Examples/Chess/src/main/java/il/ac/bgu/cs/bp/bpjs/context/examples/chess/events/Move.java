package il.ac.bgu.cs.bp.bpjs.context.examples.chess.events;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.MoveTranslator;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Cell;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Color;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Piece;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

public class Move extends BEvent {
    public final Cell source;
    public final Cell target;

    public Move(Cell source, Cell target) {
        super();
        this.source = source;
        this.target = target;
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
        return source.equals(other.source) && target.equals(other.target);
    }

    @Override
    public String toString() {
        String move= MoveTranslator.MoveToString(this);
        return "Move(" +move.charAt(0)+move.charAt(1)+")->("+move.charAt(2)+move.charAt(3)+")";
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
            if(!(bEvent instanceof Move)){
                return false;
            }
            Move move = (Move)bEvent;
            return move.source.equals(move.target);
        }
    }

    public static class OutOfBoardMoveEventSet implements EventSet {
        @Override
        public boolean contains(BEvent bEvent) {
            if(!(bEvent instanceof Move)){
                return false;
            }
            Move move = (Move)bEvent;
            return move.target.i < 0 || move.target.i > 7 || move.target.j < 0 || move.target.j > 7;
        }
    }

    public static class PieceMoveEventSet implements EventSet {
        private final Piece p;

        public PieceMoveEventSet(Piece p) {
            this.p = p;
        }

        @Override
        public boolean contains(BEvent bEvent) {
            return bEvent instanceof Move && ((Move)bEvent).source.piece.equals(p);
        }
    }

    public static class ColorMoveEventSet implements EventSet {
        private final Color c;

        public ColorMoveEventSet(Color c) {
            this.c = c;
        }

        @Override
        public boolean contains(BEvent bEvent) {
            return bEvent instanceof Move && ((Move)bEvent).source.piece.color.equals(c);
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
        private final Cell target;

        public SpecificMoveEventSet(Cell target) {
            this.target = target;
        }

        @Override
        public boolean contains(BEvent bEvent) {
            return ((Move)bEvent).target.equals(target);
        }
    }
}
