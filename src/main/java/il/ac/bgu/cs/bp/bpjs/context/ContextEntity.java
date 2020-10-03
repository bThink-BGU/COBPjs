package il.ac.bgu.cs.bp.bpjs.context;

import com.google.common.base.Objects;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;

import java.io.Serializable;
import java.util.Map;

public class ContextEntity implements Serializable {
    public final String id;
    public final String type;
    private boolean attached = false;
    public final Object data;

    public ContextEntity(String id, String type, Object data) {
        this.id = id;
        this.type = type;
        this.data = data;
    }

    private ContextEntity(ContextEntity other) {
        this.id = other.id;
        this.type = other.type;
        this.data = other.data;
    }

    public void mergeChanges(BProgram bp, ContextEntity other) {
        if(this!=other)
            assign(bp, this.data, other.data);
    }

    private Object assign(BProgram bp, Object target, Object source) {
        Function fct = bp.getFromGlobalScope("assign", Function.class).get();
        return fct.call(Context.getCurrentContext(), bp.getGlobalScope(), bp.getGlobalScope(), new Object[]{target, source});
    }

    public ContextEntity attachedCopy(BProgram bp) {
        Object clonedData = assign(bp, new NativeArray(0), data);
        ContextEntity clone = new ContextEntity(id, type, clonedData);
        clone.attached = true;
        return clone;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextEntity that = (ContextEntity) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public String toString() {
        return "Entity {" +
                "id='" + id + "'" +
                '}';
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(id);
    }
}
