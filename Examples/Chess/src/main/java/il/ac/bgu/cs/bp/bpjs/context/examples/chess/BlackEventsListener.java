package il.ac.bgu.cs.bp.bpjs.context.examples.chess;

import il.ac.bgu.cs.bp.bpjs.context.examples.chess.events.Move;
import il.ac.bgu.cs.bp.bpjs.context.examples.chess.schema.Color;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class BlackEventsListener extends BProgramRunnerListenerAdapter {
    private UCI uci;
    private Color myColor;

    public BlackEventsListener(UCI uci) {
        this.uci = uci;
    }
    public void setColor( Color c){myColor = c;}
    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {
        if (theEvent instanceof Move) {
            Move mv = (Move) theEvent;
            if(mv.source.piece.color.equals(myColor))
                uci.sendMove(MoveTranslator.MoveToString(mv));
        }
    }
}
