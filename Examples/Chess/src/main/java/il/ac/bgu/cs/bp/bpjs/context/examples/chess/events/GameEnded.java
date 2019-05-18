package il.ac.bgu.cs.bp.bpjs.context.examples.chess.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class GameEnded extends BEvent {
    enum GameResult {Draw,White,Black}

    public final GameResult result;

    public GameEnded(GameResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Game ended. Winner = " + result.toString();
    }
}
