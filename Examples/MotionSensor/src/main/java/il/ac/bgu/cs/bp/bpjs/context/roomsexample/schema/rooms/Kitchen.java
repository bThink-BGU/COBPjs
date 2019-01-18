package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms;

import javax.persistence.Entity;

@Entity
public class Kitchen extends Room {
    public Kitchen() {
        super();
    }
    public Kitchen(String id) {
        super(id);
    }
}
