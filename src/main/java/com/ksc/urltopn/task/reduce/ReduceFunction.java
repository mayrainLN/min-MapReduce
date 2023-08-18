package com.ksc.urltopn.task.reduce;

import com.ksc.urltopn.task.KeyValue;

import java.io.IOException;
import java.util.stream.Stream;


@FunctionalInterface
public interface ReduceFunction<K,V,K2,V2> extends java.io.Serializable {

    public Stream<KeyValue<K,V>> reduce(Stream<KeyValue<K,V>> stream) throws IOException;
}
