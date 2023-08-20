package com.ksc.urltopn.task.merge;

import com.ksc.urltopn.UrlTopNResult;
import com.ksc.urltopn.conf.AppConfig;
import com.ksc.urltopn.datasourceapi.PartionWriter;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.shuffle.nettyimpl.client.ShuffleClient;
import com.ksc.urltopn.task.KeyValue;
import com.ksc.urltopn.task.Task;
import com.ksc.urltopn.task.TaskStatusEnum;
import com.ksc.urltopn.task.reduce.ReduceFunction;
import com.ksc.urltopn.task.reduce.ReduceStatus;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/20 15:36
 * @description :
 */
public class MergeTask extends Task {
    ShuffleBlockId[] shuffleBlockId;
    //String destDir;
    MergeFunction mergeFunction;
    PartionWriter partionWriter;

    public MergeTask(MergeTaskContext mergeTaskContext) {
        super(mergeTaskContext);
        this.shuffleBlockId = mergeTaskContext.getShuffleBlockId();
        //this.destDir = reduceTaskContext.getDestDir();
        this.mergeFunction = mergeTaskContext.getMergeFunction();
        this.partionWriter = mergeTaskContext.getPartionWriter();
    }

    public MergeStatus runTask() throws Exception {
        Stream<KeyValue> allStream = Stream.empty();
        for (ShuffleBlockId shuffleBlockId : shuffleBlockId) {
            allStream = Stream.concat(allStream, new ShuffleClient().fetchShuffleData(shuffleBlockId));
        }
        // reduce计算结果
        Stream<KeyValue> mergeStream = mergeFunction.reduce(allStream);
        MergeStatus mergeStatus = new MergeStatus(super.taskId, TaskStatusEnum.FINISHED);

        List<UrlTopNResult> collect = mergeStream.map(kv -> new UrlTopNResult((String) kv.getKey(), (Integer) kv.getValue()))
                .collect(Collectors.toList());
        mergeStatus.seturlTopNResults(collect);
        return mergeStatus;
    }
}
