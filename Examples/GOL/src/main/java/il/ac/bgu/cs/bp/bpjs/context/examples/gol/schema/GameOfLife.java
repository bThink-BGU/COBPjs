package il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries(value = {
    @NamedQuery(name = "GameOfLife", query = "SELECT g FROM GameOfLife g"),
    @NamedQuery(name = "Generation", query = "SELECT g.generation FROM GameOfLife g"),
    @NamedQuery(name = "IncrementGeneration", query = "UPDATE GameOfLife SET generation = generation + 1"),
})
public class GameOfLife extends BasicEntity {
    @Column
    public final int boardSize;
    @Column
    public final int generation;
    @Column
    public final int maxGeneration;

    public GameOfLife() {
        this(0, 100, 16);
    }

    public GameOfLife(int generation, int maxGeneration, int boardSize) {
        super("GameOfLife");
        this.generation = generation;
        this.boardSize = boardSize;
        this.maxGeneration = maxGeneration;
    }
}
