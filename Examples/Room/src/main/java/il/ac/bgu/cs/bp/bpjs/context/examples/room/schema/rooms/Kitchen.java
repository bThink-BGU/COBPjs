package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms;

import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.Building;

import javax.persistence.Entity;

@Entity
public class Kitchen extends Room {
    protected Kitchen() {
        super();
    }
    public Kitchen(String id, Building building) {
        super(id, building);
    }
}
