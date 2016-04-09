package com.gmail.sintinium.peacekeeper.data;

public class MutablePair<K, V> {

    public K key;
    public V value;

    public MutablePair(K key, V value) {
        set(key, value);
    }

    public void set(K key, V value) {
        this.key = key;
        this.value = value;
    }

}
