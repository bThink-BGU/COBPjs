package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema;

import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms.Room;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Building extends BasicEntity {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "building", orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    protected Building() {
        super();
    }

    public Building(String id) {
        super(id);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void addRoom(Room r) {
        rooms.add(r);
    }
}
