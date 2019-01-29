package il.ac.bgu.cs.bp.bpjs.context.examples.ttt.schema;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;


@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Triple", query = "SELECT t FROM Triple t")
})
public class Triple extends BasicEntity {

    @OneToMany
    private final List<Cell> cells = new ArrayList<>(3);

    protected Triple() {
        super();
    }

    public Triple(String id, Cell cell0,Cell cell1,Cell cell2) {
        super(id);
    }

    public Cell get(int i) {
        return cells.get(i);
    }
}
