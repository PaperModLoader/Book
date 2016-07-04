package xyz.papermodloader.book.util;

import java.util.HashMap;

public class DefaultedHashMap<K, V> extends HashMap<K, V> {
    private V defaultValue;

    public DefaultedHashMap(V defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public V get(Object k) {
        return this.containsKey(k) ? super.get(k) : this.defaultValue;
    }
}
