package il.ac.bgu.cs.bp.bpjs.context.examples.gol.gui;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.View;
import java.awt.*;

public class GameView implements TableModelListener {
    private final JFrame window = new JFrame("Game of Life");
    private JTable table;
    private JLabel generation;
    private JPanel panel;
    private String[][] population;

    public GameView(int size) {
        this.population = new String[size][size];
        table.setModel(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return size;
            }

            @Override
            public int getColumnCount() {
                return size;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return population[rowIndex][columnIndex];
            }
        });

        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            public Component getTableCellRenderer(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if ( value != null ) {
                    setBackground( Color.GRAY );
                } else {
                    setBackground( Color.WHITE );
                }
                return this;
            }
        });

        window.setSize(150, 150);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());
        window.setLocation(new Point(600, 100));
        window.add(panel);
        window.setVisible(true);
    }

    public void setCell(int i, int j, String value) {
        population[i][j] = value;
    }

    public void setGeneration(int gen) {
        generation.setText("Generation - " + gen);
    }

    @Override
    public void tableChanged(TableModelEvent e) {

    }
}
