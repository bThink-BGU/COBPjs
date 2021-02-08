package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Emergency", query = "SELECT e FROM Emergency e")
})
public class Emergency extends BasicEntity {
    protected Emergency() {
        super();
    }

    public Emergency(String id) {
        super(id);
    }
}
