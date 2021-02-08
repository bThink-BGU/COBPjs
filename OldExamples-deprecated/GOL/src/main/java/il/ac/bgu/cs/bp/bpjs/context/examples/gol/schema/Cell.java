package il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema;

import javax.persistence.*;
import java.util.List;

@Entity
@NamedQueries(value = {
//        @NamedQuery(name = "Cell", query = "SELECT c FROM Cell c"),
//        @NamedQuery(name = "NGB_8", query = "SELECT c FROM Cell c WHERE NGB_COUNT(c)=8"),
        @NamedQuery(name = "N_Neighbours", query = "SELECT c FROM Cell c WHERE "+Cell.countNeighbours1 +" = :n"),
        @NamedQuery(name = "Alive_With_Less_Than_2_Neighbours", query = "SELECT c FROM Cell c, GameOfLife g WHERE g.tick = 2 AND c.alive = true AND " + Cell.notInMatingArea + " AND "+Cell.countNeighbours1 +" < 2"),
//        @NamedQuery(name = "2_or_3_Neighbours", query = "SELECT c FROM Cell c WHERE "+Cell.countNeighbours+" = 2 OR "+Cell.countNeighbours+" = 3"),
        @NamedQuery(name = "Dead_With_3_Neighbours", query = "SELECT c FROM Cell c, GameOfLife g WHERE g.tick = 2 AND c.alive = false AND " + Cell.notInMatingArea + " AND "+Cell.countNeighbours1 +" = 3"),
        @NamedQuery(name = "Alive_With_More_Than_3_Neighbours", query = "SELECT c FROM Cell c, GameOfLife g WHERE g.tick = 2 AND c.alive = true AND " + Cell.notInMatingArea + " AND "+Cell.countNeighbours1 +" > 3"),
        @NamedQuery(name = "Mating", query = "SELECT c, n1, n2, n3 FROM Cell c, Cell n1, Cell n2, Cell n3, GameOfLife g WHERE " +
                "g.tick = 1 AND " +
                "c.alive = false AND " +
                Cell.countNeighbours1 +" = 3 AND " +
                Cell.countNeighbours2 +" = 0 AND " +
                "n1 != n2 AND n2 != n3 AND n1 != n3 AND " +
                "n1.i <= n2.i AND n2.i <= n3.i AND " +
                "n1.j <= n2.j AND n2.j <= n3.j AND " +
                Cell.n1 + " AND " + Cell.n2 + " AND " + Cell.n3),
        @NamedQuery(name = "MatingArea", query = "SELECT c FROM Cell c WHERE c.i >= :deadCellI-2 AND c.i <= :deadCellI+2 AND c.j >= :deadCellJ-2 AND c.j <= :deadCellJ+2"),
        @NamedQuery(name = "Die", query = "UPDATE Cell c SET c.alive = false WHERE c=:cell"),
        @NamedQuery(name = "Reproduce", query = "UPDATE Cell c SET c.alive = true WHERE c=:cell"),
})
public class Cell extends BasicEntity {
    private static final String ngb1format = "(" +
            "(%1$s.i=c.i-1     AND     %1$s.j=c.j-1   AND      %1$s.alive = true) OR " +
            "(%1$s.i=c.i-1     AND     %1$s.j=c.j     AND      %1$s.alive = true) OR " +
            "(%1$s.i=c.i-1     AND     %1$s.j=c.j+1   AND      %1$s.alive = true) OR " +
            "(%1$s.i=c.i       AND     %1$s.j=c.j-1   AND      %1$s.alive = true) OR " +
            "(%1$s.i=c.i       AND     %1$s.j=c.j+1   AND      %1$s.alive = true) OR " +
            "(%1$s.i=c.i+1     AND     %1$s.j=c.j-1   AND      %1$s.alive = true) OR " +
            "(%1$s.i=c.i+1     AND     %1$s.j=c.j     AND      %1$s.alive = true) OR " +
            "(%1$s.i=c.i+1     AND     %1$s.j=c.j+1   AND      %1$s.alive = true))";

    public static final String n1 = "(" +
            "(n1.i=c.i-1     AND     n1.j=c.j-1   AND      n1.alive = true) OR " +
            "(n1.i=c.i-1     AND     n1.j=c.j     AND      n1.alive = true) OR " +
            "(n1.i=c.i-1     AND     n1.j=c.j+1   AND      n1.alive = true) OR " +
            "(n1.i=c.i       AND     n1.j=c.j-1   AND      n1.alive = true) OR " +
            "(n1.i=c.i       AND     n1.j=c.j+1   AND      n1.alive = true) OR " +
            "(n1.i=c.i+1     AND     n1.j=c.j-1   AND      n1.alive = true) OR " +
            "(n1.i=c.i+1     AND     n1.j=c.j     AND      n1.alive = true) OR " +
            "(n1.i=c.i+1     AND     n1.j=c.j+1   AND      n1.alive = true))";
    public static final String n2 = "(" +
            "(n2.i=c.i-1     AND     n2.j=c.j-1   AND      n2.alive = true) OR " +
            "(n2.i=c.i-1     AND     n2.j=c.j     AND      n2.alive = true) OR " +
            "(n2.i=c.i-1     AND     n2.j=c.j+1   AND      n2.alive = true) OR " +
            "(n2.i=c.i       AND     n2.j=c.j-1   AND      n2.alive = true) OR " +
            "(n2.i=c.i       AND     n2.j=c.j+1   AND      n2.alive = true) OR " +
            "(n2.i=c.i+1     AND     n2.j=c.j-1   AND      n2.alive = true) OR " +
            "(n2.i=c.i+1     AND     n2.j=c.j     AND      n2.alive = true) OR " +
            "(n2.i=c.i+1     AND     n2.j=c.j+1   AND      n2.alive = true))";
    public static final String n3 = "(" +
            "(n3.i=c.i-1     AND     n3.j=c.j-1   AND      n3.alive = true) OR " +
            "(n3.i=c.i-1     AND     n3.j=c.j     AND      n3.alive = true) OR " +
            "(n3.i=c.i-1     AND     n3.j=c.j+1   AND      n3.alive = true) OR " +
            "(n3.i=c.i       AND     n3.j=c.j-1   AND      n3.alive = true) OR " +
            "(n3.i=c.i       AND     n3.j=c.j+1   AND      n3.alive = true) OR " +
            "(n3.i=c.i+1     AND     n3.j=c.j-1   AND      n3.alive = true) OR " +
            "(n3.i=c.i+1     AND     n3.j=c.j     AND      n3.alive = true) OR " +
            "(n3.i=c.i+1     AND     n3.j=c.j+1   AND      n3.alive = true))";

    public static final String notInMatingArea = "(SELECT COUNT(a) FROM Mating a WHERE c IN ELEMENTS(a.matingArea)) = 0";

    public static final String countNeighbours1 = "(SELECT COUNT(n) from Cell n WHERE (" +
            "(n.i=c.i-1     AND     n.j=c.j-1   AND      n.alive = true) OR " +
            "(n.i=c.i-1     AND     n.j=c.j     AND      n.alive = true) OR " +
            "(n.i=c.i-1     AND     n.j=c.j+1   AND      n.alive = true) OR " +
            "(n.i=c.i       AND     n.j=c.j-1   AND      n.alive = true) OR " +
            "(n.i=c.i       AND     n.j=c.j+1   AND      n.alive = true) OR " +
            "(n.i=c.i+1     AND     n.j=c.j-1   AND      n.alive = true) OR " +
            "(n.i=c.i+1     AND     n.j=c.j     AND      n.alive = true) OR " +
            "(n.i=c.i+1     AND     n.j=c.j+1   AND      n.alive = true)))";

    public static final String countNeighbours2 = "(SELECT COUNT(n) from Cell n WHERE (" +
            "(n.i=c.i-2     AND     n.j=c.j-2   AND      n.alive = true) OR " +
            "(n.i=c.i-2     AND     n.j=c.j-1   AND      n.alive = true) OR " +
            "(n.i=c.i-2     AND     n.j=c.j     AND      n.alive = true) OR " +
            "(n.i=c.i-2     AND     n.j=c.j+1   AND      n.alive = true) OR " +
            "(n.i=c.i-2     AND     n.j=c.j+2   AND      n.alive = true) OR " +
            "(n.i=c.i-1     AND     n.j=c.j-2   AND      n.alive = true) OR " +
            "(n.i=c.i-1     AND     n.j=c.j+2   AND      n.alive = true) OR " +
            "(n.i=c.i       AND     n.j=c.j-2   AND      n.alive = true) OR " +
            "(n.i=c.i       AND     n.j=c.j+2   AND      n.alive = true) OR " +
            "(n.i=c.i+1     AND     n.j=c.j-2   AND      n.alive = true) OR " +
            "(n.i=c.i+1     AND     n.j=c.j+2   AND      n.alive = true) OR " +
            "(n.i=c.i+2     AND     n.j=c.j-2   AND      n.alive = true) OR " +
            "(n.i=c.i+2     AND     n.j=c.j-1   AND      n.alive = true) OR " +
            "(n.i=c.i+2     AND     n.j=c.j     AND      n.alive = true) OR " +
            "(n.i=c.i+2     AND     n.j=c.j+1   AND      n.alive = true) OR " +
            "(n.i=c.i+2     AND     n.j=c.j+2   AND      n.alive = true)))";

    @Column
    public final int i;
    @Column
    public final int j;
    @Column
    public final boolean alive;
    @ManyToMany(mappedBy = "matingArea")
    public final List<Mating> matings;

    protected Cell() {
        this(0,0,false);
    }

    public Cell(int i, int j, boolean alive) {
        super("Cell("+i+","+j+")");
        this.i = i;
        this.j = j;
        this.alive = alive;
        this.matings = List.of();
    }

    @Override
    public String toString() {
        return String.format("Cell(%d,%d,%b)",i,j,alive);
    }
}
