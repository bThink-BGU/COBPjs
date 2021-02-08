package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "System", query = "SELECT s FROM System s"),
        @NamedQuery(name = "Tick", query = "Update System S set S.time=S.time+1"),
})
public class System extends BasicEntity {
    @Column
    public final int time;

    protected System() {
        super();
        this.time = 0;
    }

    public System(int time) {
        super("time="+time);
        this.time = time;
    }
}
