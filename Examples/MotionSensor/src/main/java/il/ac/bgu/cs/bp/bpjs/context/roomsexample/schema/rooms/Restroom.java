package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms;

import javax.persistence.Entity;

@Entity
public class Restroom extends Room {
    public Restroom() {
        super();
    }
    public Restroom(String id) {
        super(id);
    }
}
