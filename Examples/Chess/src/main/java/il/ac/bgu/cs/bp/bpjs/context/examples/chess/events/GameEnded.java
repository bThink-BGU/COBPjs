package il.ac.bgu.cs.bp.bpjs.context.examples.chess.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class GameEnded extends BEvent {
    enum GameResult {Draw,White,Black}

    public GameResult result;

    public GameEnded(GameResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Game ended. Winner = " + result.toString();
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
        GameEnded other = (GameEnded) obj;
        if (result != other.result) {
            return false;
        }
        return true;
    }
}
