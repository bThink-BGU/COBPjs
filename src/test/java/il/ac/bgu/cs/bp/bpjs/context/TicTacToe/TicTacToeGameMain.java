package il.ac.bgu.cs.bp.bpjs.context.TicTacToe;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;

import javax.swing.*;

/**
 * Runs the TicTacToe game in interactive mode, with GUI.
 *
 * @author reututy
 */
public class TicTacToeGameMain extends JFrame {

  // GUI for interactively playing the game
  private static TTTDisplayGame TTTdisplayGame;

  public static void initBProg(BProgram bprog, boolean useUI) {
    bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
    if (useUI) {
      bprog.setWaitForExternalEvents(true);
      JFrame f = new TicTacToeGameMain();
      TTTdisplayGame = new TTTDisplayGame(bprog);
    } else {
      bprog.appendSource("ctx.bthread(\"simulate x\", \"Cell.All\", function (cell) {\n" +
          "  sync({request: Event(\"X\", cell)})\n" +
          "})");
    }
  }

  public static void initRNR(BProgramRunner rnr) {
    if (TTTdisplayGame != null) {
      TTTdisplayGame.initBProgramRunner(rnr);
    }
  }
}

@SuppressWarnings("serial")
class TTTButton extends JButton {
  int row;
  int col;

  /**
   * Constructor.
   *
   * @param row The row of the button.
   * @param col The column of the button.
   */
  public TTTButton(int row, int col) {
    super();
    this.row = row;
    this.col = col;
  }
}
