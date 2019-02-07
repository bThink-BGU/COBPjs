package il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms;


import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.Building;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.Worker;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices.AirConditioner;

import javax.persistence.*;

@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Office", query = "SELECT o FROM Office o")
})
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
