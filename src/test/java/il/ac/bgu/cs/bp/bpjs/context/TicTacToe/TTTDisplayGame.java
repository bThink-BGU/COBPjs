package il.ac.bgu.cs.bp.bpjs.context.TicTacToe;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Class that implements the Graphical User Interface for the game
 */
public class TTTDisplayGame implements ActionListener {
  private final BProgram bp;

  public JButton[][] buttons = new JButton[3][];
  public JLabel message = new JLabel();

  public TTTDisplayGame(BProgram bp, BProgramRunner rnr) {
    this.bp = bp;

    // Create window
    JFrame window = new JFrame("Tic-Tac-Toe");
    window.setSize(150, 150);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setLayout(new BorderLayout());
    window.setLocation(new Point(600, 100));

    // The board
    JPanel board = new JPanel();
    board.setLayout(new GridLayout(3, 3));
    // The message label
    message.setHorizontalAlignment(SwingConstants.CENTER);

    // Create buttons
    for (int i = 0; i < 3; i++) {
      buttons[i] = new JButton[3];
      for (int j = 0; j < 3; j++) {
        buttons[i][j] = new TTTButton(i, j);
        board.add(buttons[i][j]);
        buttons[i][j].addActionListener(this);
      }
    }

    // Add the board and the message component to the window
    window.add(board, BorderLayout.CENTER);
    window.add(message, BorderLayout.SOUTH);

    // Make the window visible
    window.setVisible(true);

    // Writs 'X' and 'O' on the buttons
    rnr.addListener(new BProgramRunnerListenerAdapter() {
      @Override
      public void eventSelected(BProgram bp, BEvent e) {
        if (e.name.equals("X") || e.name.equals("O")) {
          var cell = (Map<String, Double>) e.maybeData;
          buttons[cell.get("i").intValue()][cell.get("j").intValue()].setText(e.name);
        } else {
          switch (e.name) {
            case "XWin":
              message.setText("X Wins!");
              break;
            case "OWin":
              message.setText("O Wins!");
              break;
            case "Draw":
              message.setText("It's a Draw!");
              break;
          }
        }
      }

    });

  }

  /**
   * @see ActionListener#actionPerformed(ActionEvent)
   */
  public void actionPerformed(ActionEvent a) {
    final TTTButton btt = ((TTTButton) a.getSource());
    bp.enqueueExternalEvent(new BEvent("Click(" + btt.row + "," + btt.col + ")"));
  }

  /**
   * A button that remembers its position on the board
   */
  @SuppressWarnings("serial")
  static class TTTButton extends JButton {
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
}