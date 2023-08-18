package com.ksc.urltopn.task.reduce;

import com.ksc.urltopn.conf.AppConfig;
import com.ksc.urltopn.datasourceapi.PartionWriter;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.shuffle.nettyimpl.client.ShuffleClient;
import com.ksc.urltopn.task.KeyValue;
import com.ksc.urltopn.task.Task;
import com.ksc.urltopn.task.TaskStatusEnum;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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


    public ReduceStatus runTask() throws Exception {
        System.out.println("stageId:" + stageId);
        // reduce任务
        if (stageId.equals("stage_1")) {
            Stream<KeyValue> stream = Stream.empty();
            // 读取shuffle文件
            for (ShuffleBlockId shuffleBlockId : shuffleBlockId) {
                stream = Stream.concat(stream, new ShuffleClient().fetchShuffleData(shuffleBlockId));
            }
            // reduce计算结果
            Stream reduceStream = reduceFunction.reduce(stream);

            // 写入到output文件
            partionWriter.writeReduce(reduceStream, applicationId);

            return new ReduceStatus(super.taskId, TaskStatusEnum.FINISHED);
        }

        if (stageId.equals("stage_2")) {
            Stream<String> allStream = Stream.empty();
            File file = new File(AppConfig.shuffleTempDir + "/" + applicationId + "/midFile");
            File[] reduceResultFiles = file.listFiles();

            for (File midFile : reduceResultFiles) {
                Stream<String> stream = Files.lines(Paths.get(midFile.getAbsolutePath()));
                allStream = Stream.concat(allStream, stream);
            }
            // reduce计算结果
            Stream<KeyValue> mergeStream = reduceFunction.reduce(allStream);

            partionWriter.writeMerge(mergeStream, applicationId);

            return new ReduceStatus(super.taskId, TaskStatusEnum.FINISHED);
        }

        return new ReduceStatus(super.taskId, TaskStatusEnum.FAILED);
    }
}