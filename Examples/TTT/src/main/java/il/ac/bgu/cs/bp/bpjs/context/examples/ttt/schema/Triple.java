package il.ac.bgu.cs.bp.bpjs.context.examples.ttt.schema;

import javax.persistence.*;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Triple", query = "SELECT t FROM Triple t")
})
public class Triple extends BasicEntity {
    @OneToOne(cascade = CascadeType.MERGE)
    public final Cell cell1;
    @OneToOne(cascade = CascadeType.MERGE)
    public final Cell cell2;
    @OneToOne(cascade = CascadeType.MERGE)
    public final Cell cell3;

    protected Triple() {
        this("", new Cell[]{null, null, null});
    }

    public Triple(String id, Cell[] cell) {
        super(id);
        this.cell1 = cell[0];
        this.cell2 = cell[1];
        this.cell3 = cell[2];
    }
}
