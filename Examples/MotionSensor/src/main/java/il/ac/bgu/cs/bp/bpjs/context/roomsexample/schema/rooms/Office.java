package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.Worker;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices.AirConditioner;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class Office extends Room {
    @OneToOne(cascade = CascadeType.MERGE)
    private AirConditioner airConditioner;

    @OneToOne
    private Worker worker;

    public Office() {
        super();
    }

    public Office(String id, Worker worker) {
        super(id);
        airConditioner = new AirConditioner(id + ".AirConditioner");
        this.worker = worker;
    }

    public AirConditioner getAirConditioner() {
        return airConditioner;
    }
}
