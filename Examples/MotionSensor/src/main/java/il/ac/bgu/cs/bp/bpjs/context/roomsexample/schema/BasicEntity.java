package il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BasicEntity {
    @Id
	protected String id = null;

    public BasicEntity() { }
    public BasicEntity(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
}
