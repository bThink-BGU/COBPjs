package il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Cell", query = "SELECT c FROM Cell c"),
//        @NamedQuery(name = "NGB_8", query = "SELECT c FROM Cell c WHERE NGB_COUNT(c)=8"),
        @NamedQuery(name = "N_Neighbours", query = "SELECT c FROM Cell c WHERE "+Cell.countNeighbours+" = :n"),
        @NamedQuery(name = "Less_Than_2_Neighbours", query = "SELECT c FROM Cell c WHERE "+Cell.countNeighbours+" < 2"),
        @NamedQuery(name = "2_or_3_Neighbours", query = "SELECT c FROM Cell c WHERE "+Cell.countNeighbours+" = 2 OR "+Cell.countNeighbours+" = 3"),
        @NamedQuery(name = "More_Than_3_Neighbours", query = "SELECT c FROM Cell c WHERE "+Cell.countNeighbours+" > 3"),
        @NamedQuery(name = "Die", query = "UPDATE Cell SET alive = false WHERE i = :i AND j = :j"),
        @NamedQuery(name = "Spawn", query = "UPDATE Cell SET alive = true WHERE i = :i AND j = :j"),
})
public class Cell extends BasicEntity {
    public static final String countNeighbours = "(SELECT COUNT(n) from Cell n WHERE (" +
            "(n.i=c.i-1     AND     n.j=c.j-1   AND      n.alive = true) OR " +
            "(n.i=c.i-1     AND     n.j=c.j     AND      n.alive = true) OR " +
            "(n.i=c.i-1     AND     n.j=c.j+1   AND      n.alive = true) OR " +
            "(n.i=c.i       AND     n.j=c.j-1   AND      n.alive = true) OR " +
            "(n.i=c.i       AND     n.j=c.j+1   AND      n.alive = true) OR " +
            "(n.i=c.i+1     AND     n.j=c.j-1   AND      n.alive = true) OR " +
            "(n.i=c.i+1     AND     n.j=c.j     AND      n.alive = true) OR " +
            "(n.i=c.i+1     AND     n.j=c.j+1   AND      n.alive = true)))";

    @Column
    public final int i;
    @Column
    public final int j;
    @Column
    public final boolean alive;

    protected Cell() {
        super();
        i=0;
        j=0;
        alive = false;
    }

    public Cell(int i, int j, boolean alive) {
        super("Cell("+i+","+j+")");
        this.i = i;
        this.j = j;
        this.alive = alive;
    }

    @Override
    public String toString() {
        return String.format("Cell(%d,%d,%b)",i,j,alive);
    }
}


