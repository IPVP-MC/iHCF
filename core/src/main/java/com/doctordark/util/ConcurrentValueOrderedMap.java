package com.doctordark.util;

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
        InternalEntry<K, V> old = this.lookup.get(key);
        return old != null ? old.getValue() : null;
    }

    public V put(K key, V val) {
        InternalEntry<K, V> entry = new InternalEntry<>(key, val);
        InternalEntry<K, V> old = this.lookup.put(key, entry);
        if (old == null) {
            this.ordering.add(entry);
            return null;
        }

        this.ordering.remove(old);
        this.ordering.add(entry);
        return old.getValue();
    }

    public V remove(Object key) {
        InternalEntry<K, V> old = this.lookup.remove(key);
        if (old != null) {
            this.ordering.remove(old);
            return old.getValue();
        }

        return null;
    }

    public void clear() {
        this.ordering.clear();
        this.lookup.clear();
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(this.ordering);
    }

    public static class InternalEntry<K, V extends Comparable<V>> implements Comparable<InternalEntry<K, V>>, Map.Entry<K, V> {

        private final K key;
        private final V value;

        InternalEntry(K key, V val) {
            this.key = key;
            this.value = val;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(@Nonnull InternalEntry<K, V> o) {
            return o.value.compareTo(this.value);
        }
    }
}