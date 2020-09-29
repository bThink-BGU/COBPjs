package il.ac.bgu.cs.bp.bpjs.context;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Context implements Serializable {
    public static Context singleton;

    public static int generateUniqueId() {
        return singleton.idCounter.incrementAndGet();
    }

    public static Context CreateInstance(Object ctx) {
        if(singleton == null) {
            singleton = new Context(ctx);
        }
        return singleton;
    }
    public static Object GetInstance() {
        return singleton.CTX;
    }
    private Context(Object CTX) {
        this.CTX = CTX;
    }
    public Object CTX;
    private AtomicInteger idCounter = new AtomicInteger(0);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return Objects.equals(CTX, context.CTX);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CTX);
    }
}
