package com.doctordark.hcf.util;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConcurrentValueOrderedMap<K, V extends Comparable<V>> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

    private final Set<InternalEntry<K, V>> ordering = new ConcurrentSkipListSet<>();
    private final ConcurrentMap<K, InternalEntry<K, V>> lookup = new ConcurrentHashMap<>();

    public V get(Object key) {
        InternalEntry<K, V> old = lookup.get(key);
        return old != null ? old.getValue() : null;
    }

    public V put(K key, V val) {
        InternalEntry<K, V> entry = new InternalEntry<>(key, val);
        InternalEntry<K, V> old = lookup.put(key, entry);
        if (old == null) {
            ordering.add(entry);
            return null;
        }

        ordering.remove(old);
        ordering.add(entry);
        return old.getValue();
    }

    public V remove(Object key) {
        InternalEntry<K, V> old = lookup.remove(key);
        if (old != null) {
            ordering.remove(old);
            return old.getValue();
        }

        return null;
    }

    public void clear() {
        ordering.clear();
        lookup.clear();
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(ordering);
    }

    private static class InternalEntry<K, V extends Comparable<V>> implements Comparable<InternalEntry<K, V>>, Map.Entry<K, V> {

        private final K key;
        private final V value;

        InternalEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(@Nonnull InternalEntry<K, V> o) {
            return o.value.compareTo(value);
        }
    }
}