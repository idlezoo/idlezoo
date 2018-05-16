package idlezoo.game.domain;

import java.util.Map;

public final class TopEntry<V> {
    private final String name;
    private final V value;

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
