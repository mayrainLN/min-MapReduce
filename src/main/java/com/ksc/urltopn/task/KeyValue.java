package com.ksc.urltopn.task;

public class KeyValue<K,V> implements java.io.Serializable{
    K key;
    V value;
    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }
    public K getKey() {
        return this.key;
    }
    public V getValue() {
        return this.value;
    }
}
