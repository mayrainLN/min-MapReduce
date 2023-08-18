package com.ksc.urltopn.datasourceapi;

import com.ksc.urltopn.task.KeyValue;

import java.io.IOException;
import java.util.stream.Stream;

public interface PartionWriter<T>   {

    void writeReduce(Stream<T> stream, String applicationId) throws IOException;

    void writeMerge(Stream<KeyValue> stream, String applicationId) throws IOException;
}
