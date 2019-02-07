package il.ac.bgu.cs.bp.bpjs.context.examples.ttt.schema;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@SuppressWarnings("WeakerAccess")
public abstract class BasicEntity implements Serializable{
    @Id
	private final String id;

    protected BasicEntity() {
        id = "";
    }
    protected BasicEntity(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BasicEntity other = (BasicEntity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
