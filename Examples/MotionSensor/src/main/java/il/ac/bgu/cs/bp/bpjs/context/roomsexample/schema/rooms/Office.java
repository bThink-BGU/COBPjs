package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.Building;
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

    protected Office() {
        super();
    }

    public Office(String id, Building building, Worker worker) {
        super(id, building);
        airConditioner = new AirConditioner(getId() + ".AirConditioner");
        this.worker = worker;
    }

    public AirConditioner getAirConditioner() {
        return airConditioner;
    }
}
