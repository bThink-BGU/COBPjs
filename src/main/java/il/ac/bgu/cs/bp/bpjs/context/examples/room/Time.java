package il.ac.bgu.cs.bp.bpjs.context.examples.room;

import il.ac.bgu.cs.bp.bpjs.context.ContextEntity;
import il.ac.bgu.cs.bp.bpjs.context.Query;

public class Time extends ContextEntity<Time> {
    public int time = 3;

    public Time() {
        super("Time");
        this.time = 3;
    }

    public Time(Time other) {
        super(other);
        this.time = other.time;
    }

    @Override
    public String toString() {
        return "Time is " + time;
    }

    public int incTime() {
        return ++time;
    }

    @Override
    public void mergeChanges(Time other) {
        this.time = other.time;
    }

    @Override
    protected ContextEntity<Time> cloneEntity() {
        return new Time(this);
    }

    static {
        Query.addQuery("Time.All", ctx-> ctx instanceof Time);
    }
}
