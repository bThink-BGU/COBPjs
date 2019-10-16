package il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries(value = {
    @NamedQuery(name = "GameOfLife", query = "SELECT g FROM GameOfLife g"),
    @NamedQuery(name = "Tick", query = "UPDATE GameOfLife SET tick = 1"),
    @NamedQuery(name = "Tack", query = "UPDATE GameOfLife SET tick = 2"),
    @NamedQuery(name = "Tock", query = "UPDATE GameOfLife SET tick = 0"),
})
public class GameOfLife extends BasicEntity {
    @Column
    public final int boardSize;
    @Column
    public final int tick;
    @Column
    public final int maxGeneration;

    public GameOfLife() {
        this(100, 16);
    }

    public GameOfLife(int maxGeneration, int boardSize) {
        super("GameOfLife");
        this.tick = 0;
        this.boardSize = boardSize;
        this.maxGeneration = maxGeneration;
    }
}
