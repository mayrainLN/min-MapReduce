package com.ksc.urltopn.shuffle;

import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.map.MapStatus;

import java.io.IOException;
import java.util.stream.Stream;

public  interface ShuffleWriter<KeyValue> {

    void write(Stream<KeyValue> stream) throws IOException;

    void commit();

    TaskStatus getMapStatus(int mapTaskId);

}
