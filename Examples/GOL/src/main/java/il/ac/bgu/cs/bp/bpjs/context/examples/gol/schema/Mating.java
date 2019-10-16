package il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema;


import javax.persistence.*;
import java.util.List;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "ActiveMating", query = "SELECT m FROM Mating m")
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
    public final List<Cell> matingArea;
    @Column
    public int round = 0;

    protected Mating() {
        this("", null, null, null, null, List.of());
    }

    public Mating(String id, Cell cell, Cell n1, Cell n2, Cell n3, List<Cell> matingArea) {
        super("Mating " + cell);
        this.cell = cell;
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;
        this.matingArea = matingArea;
    }
}
