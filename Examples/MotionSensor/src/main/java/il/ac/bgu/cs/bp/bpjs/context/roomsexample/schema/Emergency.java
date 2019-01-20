package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema;

import javax.persistence.Column;
import javax.persistence.Entity;


@Entity
public class Emergency extends BasicEntity {
    protected Emergency() {
        super();
    }

    public Emergency(String id) {
        super(id);
    }
}
