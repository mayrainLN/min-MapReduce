package com.ksc.urltopn.task.merge;

import com.ksc.urltopn.datasourceapi.PartionWriter;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.TaskContext;
import com.ksc.urltopn.task.reduce.ReduceFunction;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/20 15:36
 * @description :
 */
public class MergeTaskContext extends TaskContext {
    ShuffleBlockId[] shuffleBlockId;
    //String destDir;
    MergeFunction reduceFunction;
    PartionWriter partionWriter;

    public MergeTaskContext(String applicationId, String stageId, int taskId, int partionId,ShuffleBlockId[] shuffleBlockId, MergeFunction mergeFunction,PartionWriter partionWriter) {
        super(applicationId, stageId, taskId, partionId);
        this.shuffleBlockId = shuffleBlockId;
        //this.destDir = destDir;
        this.reduceFunction = mergeFunction;
        this.partionWriter = partionWriter;
    }

    public ShuffleBlockId[] getShuffleBlockId() {
        return shuffleBlockId;
    }

    //public String getDestDir() {
//        return destDir;
//    }

    public MergeFunction getMergeFunction() {
        return reduceFunction;
    }

    public PartionWriter getPartionWriter() {
        return partionWriter;
    }
}
