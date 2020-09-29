package il.ac.bgu.cs.bp.bpjs.context.examples.gol.schema;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
@SuppressWarnings("WeakerAccess")
public abstract class BasicEntity implements Serializable{
    @Id
	public final String id;

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
        return Objects.equals(this.id, other.id);
    }
}
