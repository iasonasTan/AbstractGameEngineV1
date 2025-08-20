package com.engine.data;

import java.util.HashMap;
import java.util.Map;

/**
 * A Map implementation that allows each key to be inserted only once.
 * Any attempt to re-insert the same key will be ignored.
 * This class disables the standard {@link #put(Object, Object)} method and
 * requires the use of {@link #putPair(Object, Object)} for insertion.
 */
public class UniqueInsertMap<K,V> extends HashMap<K,V> {
    /**
     * Method is deprecated. Use {@link #putPair(Object, Object)} instead.
     */
    @Deprecated
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("This method is not available, use putPair instead");
    }

    /**
     * Replacement for {@link #put(Object, Object)} method.
     * @param key entry key.
     * @param value entry value.
     * @return returns {@code true} if kv pair is added to the map, {@code false} otherwise.
     */
    public boolean putPair(K key, V value) throws IllegalArgumentException {
        boolean added=!containsKey(key);
        if (added)
            super.put(key, value);
        else
            throw new IllegalArgumentException("key "+key+" already added with value "+get(key));
        return true;
    }

    /**
     * Copies all the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     * Modified to throw {@link IllegalArgumentException} if duplicate key is found.
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            if (containsKey(entry.getKey())) {
                throw new IllegalArgumentException("Duplicate key: " + entry.getKey());
            }
        }
        super.putAll(m);
    }
}
