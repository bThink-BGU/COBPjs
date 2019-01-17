package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Building extends BasicEntity {
    @OneToMany()
    private List<Room> rooms = new ArrayList<>();


    public Building() {
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
