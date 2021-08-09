package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CtxDirectMapProxy<K, V> extends MapProxy<K, V> {
  public CtxDirectMapProxy(MapProxy<K, V> mapProxy) {
    super(mapProxy.seed);
  }

  public int size() {
    return this.seed.size();
  }

  public Set<K> keys() {
    return this.seed.keySet();
  }

  public boolean has(K key) {
    return this.seed.containsKey(key);
  }

  public V get(K key) {
    return this.seed.get(key);
  }

  public Map<K, V> filter(BiFunction<K, V, Boolean> func) {
    return (Map)this.seed.entrySet().stream().filter((entry) -> {
      return (Boolean)func.apply(entry.getKey(), entry.getValue());
    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public void remove(K key) {
    this.seed.remove(key);
  }

  public void put(K key, V value) {
    this.seed.put(key, value);
  }
}
