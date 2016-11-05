package idlezoo.game.domain;

import java.util.Map;

public class TopEntry<V> {
  private final String name;
  private final V value;

  public static <V> TopEntry<V> of(Map.Entry<String, V> entry) {
    return new TopEntry<>(entry);
  }

  public TopEntry(Map.Entry<String, V> entry) {
    this.name = entry.getKey();
    this.value = entry.getValue();
  }

  public TopEntry(String name, V value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public V getValue() {
    return value;
  }
}
