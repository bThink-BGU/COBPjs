package il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "ActiveMatings", query = "SELECT m FROM Mating m")
})
@SuppressWarnings("WeakerAccess")
public class Mating extends BasicEntity {
    @OneToOne(cascade = CascadeType.MERGE)
    public final Cell cell;
    @OneToOne(cascade = CascadeType.MERGE)
    public final Cell n1;
    @OneToOne(cascade = CascadeType.MERGE)
    public final Cell n2;
    @OneToOne(cascade = CascadeType.MERGE)
    public final Cell n3;
    @OneToMany(cascade = CascadeType.MERGE)
    public final List<Cell> area;

    protected Mating() {
        this("", new Cell[]{null, null, null}, new ArrayList<Cell>());
    }

    public Mating(String id, Cell[] cell, List<Cell> area) {
        super("Mating " + cell[0]);
        this.cell = cell[0];
        this.n1 = cell[1];
        this.n2 = cell[2];
        this.n3 = cell[3];
        this.area = area;
    }
}
