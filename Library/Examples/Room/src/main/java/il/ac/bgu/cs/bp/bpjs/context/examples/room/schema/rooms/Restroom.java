package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms;


import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.Building;

import javax.persistence.Entity;

@Entity
public class Restroom extends Room {
    protected Restroom() {
        super();
    }
    public Restroom(String id, Building building) {
        super(id, building);
    }
}
