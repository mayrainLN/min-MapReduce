package com.ksc.urltopn.task.map;

import com.ksc.urltopn.datasourceapi.PartionFile;
import com.ksc.urltopn.datasourceapi.PartionReader;
import com.ksc.urltopn.conf.AppConfig;
import com.ksc.urltopn.shuffle.DirectShuffleWriter;
import com.ksc.urltopn.task.Task;
import com.ksc.urltopn.task.TaskStatus;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

public class ShuffleMapTask extends Task<MapStatus> {

    PartionFile partiongFile;
    PartionReader partionReader;
    int reduceTaskNum;
    MapFunction mapFunction;

    public ShuffleMapTask(MapTaskContext mapTaskContext) {
        super(mapTaskContext);
        this.partiongFile = mapTaskContext.getPartiongFile();
        this.partionReader = mapTaskContext.getPartionReader();
        this.reduceTaskNum = mapTaskContext.getReduceTaskNum();
        this.mapFunction = mapTaskContext.getMapFunction();
    }


    public TaskStatus runTask() throws IOException {
        Stream<String> stream = partionReader.toStream(partiongFile);


//        Stream<AbstractMap.SimpleEntry<String, Integer>> simpleEntryStream = stream.flatMap(line -> Arrays.stream(line.split("\\s+")))
//                .map(word -> new AbstractMap.SimpleEntry<String, Integer>(word, 1));
        Stream kvStream = mapFunction.map(stream);

        String shuffleId= UUID.randomUUID().toString();
        // 多少个reduce就生成多少个shuffle文件。
        DirectShuffleWriter shuffleWriter = new DirectShuffleWriter(AppConfig.shuffleTempDir, shuffleId,applicationId, partionId, reduceTaskNum);
        //将task执行结果写入shuffle文件中
        shuffleWriter.write(kvStream);
        shuffleWriter.commit();
        return shuffleWriter.getMapStatus(taskId);
    }




}
