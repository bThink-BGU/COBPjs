package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.Building;

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
