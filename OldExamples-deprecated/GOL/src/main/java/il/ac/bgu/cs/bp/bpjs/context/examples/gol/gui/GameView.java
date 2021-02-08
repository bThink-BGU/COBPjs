package il.ac.bgu.cs.bp.bpjs.context.examples.gol.gui;

import il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema.GameOfLife;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Arrays;
import java.util.logging.Logger;

public class GameView {
    private static final Logger logger = Logger.getLogger(GameOfLife.class.getName());
    private static final int cellSize = 25;

    private final JFrame window = new JFrame("Game of Life");
    private JTable table;
    private JLabel generation;
    private JPanel panel;
    private JLabel pattern;
    private int gen = 0;
    private AbstractTableModel model;

    public GameView(int size, BProgram bprog) {
        model = new AbstractTableModel(){
            private Object[][] model = new Object[size][size];
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
                return model[rowIndex][columnIndex];
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                model[rowIndex][columnIndex] = aValue;
            }

            @Override
            public String toString() {
                return Arrays.deepToString(model);
            }
        };
        table.setModel(model);
        table.setRowHeight(cellSize);
        table.getColumnModel().getColumns().asIterator().forEachRemaining(c-> {
            c.setWidth(cellSize);
            c.setMinWidth(cellSize);
            c.setMaxWidth(cellSize);
        });
        table.setRowMargin(0);
        table.setRowSelectionAllowed(false);
        table.setVisible(true);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setOpaque(true);
                if ( value != null ) {
                    setBackground( Color.GREEN );
                } else {
                    setBackground( Color.WHITE );
                }
//                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                return this;
            }
        });

//        window.setSize((size + 2) * cellSize, generation.getHeight() + (size + 2) * cellSize);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());
        window.setLocation(new Point(600, 100));
        window.add(panel);
        window.pack();
//        window.setSize(window.getWidth()+50,window.getHeight()+50);
        window.setVisible(true);
        bprog.enqueueExternalEvent(new BEvent("UI is ready"));
    }

    public void setCell(int i, int j, Object value) {
        model.setValueAt(value, i, j);
    }

    public void incGeneration() {
        generation.setText("Generation - " + gen);
        model.fireTableDataChanged();
        gen++;
    }

    public void setPattern(String p) {
        this.pattern.setText("Pattern - " + p);
    }
}
