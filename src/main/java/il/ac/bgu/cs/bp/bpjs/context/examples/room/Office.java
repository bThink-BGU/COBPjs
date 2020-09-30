package il.ac.bgu.cs.bp.bpjs.context.examples.room;

import il.ac.bgu.cs.bp.bpjs.context.ContextEntity;
import il.ac.bgu.cs.bp.bpjs.context.Query;

public class Office extends Room {
    public final String airConditioner;

    public Office(String id) {
        super(id);
        this.airConditioner = id + ".airConditioner";
    }

    public Office(Office other) {
        super(other);
        this.airConditioner = id + ".airConditioner";
    }

    @Override
    protected ContextEntity<Room> cloneEntity() {
        return new Office(this);
    }

    static{
        Query.addQuery("Office.Empty", ctx-> ctx instanceof Office && ((Office)ctx).isEmpty);
        Query.addQuery("Office.Nonempty", ctx-> ctx instanceof Office && !((Office)ctx).isEmpty);
        Query.addQuery("Office.All", ctx-> ctx instanceof Office);
    }
}
