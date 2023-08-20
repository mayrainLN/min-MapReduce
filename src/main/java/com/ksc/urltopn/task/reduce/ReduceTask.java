package com.ksc.urltopn.task.reduce;

import com.ksc.urltopn.conf.AppConfig;
import com.ksc.urltopn.datasourceapi.PartionWriter;
import com.ksc.urltopn.shuffle.DirectShuffleWriter;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.shuffle.nettyimpl.client.ShuffleClient;
import com.ksc.urltopn.task.KeyValue;
import com.ksc.urltopn.task.Task;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

public class ReduceTask extends Task {

    ShuffleBlockId[] shuffleBlockId;
    //String destDir;
    ReduceFunction reduceFunction;
    PartionWriter partionWriter;


    public ReduceTask(ReduceTaskContext reduceTaskContext) {
        super(reduceTaskContext);
        this.shuffleBlockId = reduceTaskContext.getShuffleBlockId();
        //this.destDir = reduceTaskContext.getDestDir();
        this.reduceFunction = reduceTaskContext.getReduceFunction();
        this.partionWriter = reduceTaskContext.getPartionWriter();
    }


    public TaskStatus runTask() throws Exception {
        System.out.println("stageId:" + stageId);

        Stream<KeyValue> kvStream = Stream.empty();
        for (ShuffleBlockId shuffleBlockId : shuffleBlockId) {
            kvStream = Stream.concat(kvStream, new ShuffleClient().fetchShuffleData(shuffleBlockId));
        }
        // reduce计算结果
        Stream reduceStream = reduceFunction.reduce(kvStream);
        System.out.println("reduce计算成功");
        // 写入shuffle文件
        String shuffleId= UUID.randomUUID().toString();

        // reduce分布式计算，结果统一写在某一台机器中
        DirectShuffleWriter shuffleWriter = new DirectShuffleWriter(AppConfig.shuffleTempDir, shuffleId,applicationId, partionId, 1);
        //将task执行结果写入shuffle文件中
        shuffleWriter.write(reduceStream);
        shuffleWriter.commit();

        // 写入到output文件
//        废弃 partionWriter.writeReduce(reduceStream, applicationId);
        return shuffleWriter.getReduceStatus(taskId);
    }
}