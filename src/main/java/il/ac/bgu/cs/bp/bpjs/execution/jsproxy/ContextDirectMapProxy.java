package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ContextDirectMapProxy<K,V> extends MapProxy<K,V> {
  public ContextDirectMapProxy(Map<K, V> aSeed) {
    super(aSeed);
  }

  public ContextDirectMapProxy(MapProxy<K, V> store) {
    this(store.seed);
  }

  @Override
  public int size() {
    return seed.size();
  }

  @Override
  public Set<K> keys() {
    return seed.keySet();
  }

  @Override
  public boolean has(K key) {
    return seed.containsKey(key);
  }

  @Override
  public V get(K key) {
    return seed.get(key);
  }

  @Override
  public Map<K, V> filter(BiFunction<K, V, Boolean> func) {
    return seed.entrySet().stream()
        .filter(entry -> func.apply(entry.getKey(), entry.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public void remove(K key) {
    seed.remove(key);
  }

  @Override
  public void put(K key, V value) {
    seed.put(key, value);
  }
}
