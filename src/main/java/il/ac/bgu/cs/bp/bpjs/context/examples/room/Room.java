package il.ac.bgu.cs.bp.bpjs.context.examples.room;

import il.ac.bgu.cs.bp.bpjs.context.Context;
import il.ac.bgu.cs.bp.bpjs.context.ContextEntity;
import il.ac.bgu.cs.bp.bpjs.context.Query;

public class Room extends ContextEntity<Room> {
    public boolean isEmpty = true;
    public int lastMovement = 0;
    public final String light;

    public Room(String id) {
        super(id);
        this.light = id + ".light";
    }

    public Room(Room other) {
        super(other);
        this.isEmpty = other.isEmpty;
        this.lastMovement = other.lastMovement;
        this.light = other.light;
    }

    @Override
    public void mergeChanges(Room other) {
        isEmpty = other.isEmpty;
        lastMovement = other.lastMovement;
    }

    @Override
    protected ContextEntity<Room> cloneEntity() {
        return new Room(this);
    }

    static {
        Query.addQuery("Room.NoMovement3", ctx -> ctx instanceof Room && !((Room) ctx).isEmpty && ((Time) Context.GetInstance().getActive("Time.All").get(0)).time - ((Room) ctx).lastMovement >= 3);
        Query.addQuery("Room.Empty", ctx -> ctx instanceof Room && ((Room) ctx).isEmpty);
        Query.addQuery("Room.Nonempty", ctx -> ctx instanceof Room && !((Room) ctx).isEmpty);
        Query.addQuery("Room.All", ctx -> ctx instanceof Room);
    }
}
