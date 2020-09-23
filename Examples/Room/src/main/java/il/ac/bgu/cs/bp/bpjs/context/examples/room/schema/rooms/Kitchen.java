package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms;

import javax.persistence.Entity;

@Entity
public class Kitchen extends Room {
    protected Kitchen() {
        super();
    }
    public Kitchen(String id) {
        super(id);
    }
}
