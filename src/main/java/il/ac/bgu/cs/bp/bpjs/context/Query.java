package il.ac.bgu.cs.bp.bpjs.context;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Query {
    private static Map<String, Query> queries = new HashMap<>();

    public static void addQuery(String name, Function<ContextEntity<?>, Boolean> func) {
        queries.put(name, new Query(name, func));
    }

    public static Query getQuery(String name) {
        return queries.get(name);
    }

    public final String name;
    private final transient Function<ContextEntity<?>, Boolean> function;

    private Query(String name, Function<ContextEntity<?>, Boolean> function) {
        this.name = name;
        this.function = function;
    }

    @Override
    public String toString() {
        return "Query{" + name + '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return Objects.equal(name, query.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(name);
    }

    public final boolean func(ContextEntity<?> contextEntity) {
        return this.function.apply(contextEntity);
    }
}
