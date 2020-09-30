package il.ac.bgu.cs.bp.bpjs.context;

import com.google.common.base.Objects;

import java.io.Serializable;

public abstract class ContextEntity<T extends ContextEntity<T>> implements Serializable {
    public final String id;
    private boolean attached = false;

    protected ContextEntity(String id) {
        this.id = id;
    }
    protected ContextEntity(ContextEntity<T> other) {
        this.id = other.id;
    }

    public abstract void mergeChanges(T other);

    public ContextEntity<T> attachedCopy() {
        ContextEntity<T> clone = cloneEntity();
        clone.attached = true;
        return clone;
    }

    protected abstract ContextEntity<T> cloneEntity();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextEntity that = (ContextEntity) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public String toString() {
        return getClass() + "{" +
                "id='" + id + '\'' +
                '}';
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(id);
    }
}
