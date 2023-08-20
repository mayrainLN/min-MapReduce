package com.ksc.urltopn.task.merge;

import com.ksc.urltopn.task.KeyValue;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/20 15:33
 * @description :
 */
public interface MergeFunction<K,V> extends java.io.Serializable{
    public Stream<KeyValue<K,V>> reduce(Stream<KeyValue<K,V>> stream) throws IOException;
}
