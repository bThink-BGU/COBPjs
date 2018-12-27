package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema;

import javax.persistence.Entity;

@Entity
public class Office extends Room {
    public Office() {
        super();
    }

    public Office(String id) {
        super(id);
    }
}
