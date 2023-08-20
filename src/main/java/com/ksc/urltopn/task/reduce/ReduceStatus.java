package com.ksc.urltopn.task.reduce;

import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;

public class ReduceStatus extends TaskStatus {
    // Map的结果shuffle文件的元信息。在Executor执行完毕后，会通过akka发送过来，内部包含executor的host和port
    ShuffleBlockId[] shuffleBlockIds;

    public ReduceStatus(int taskId, ShuffleBlockId[] shuffleBlockId, TaskStatusEnum finished) {
        super(taskId, TaskStatusEnum.FINISHED);
        this.shuffleBlockIds = shuffleBlockId;
    }

    public ShuffleBlockId[] getShuffleBlockIds() {
        return shuffleBlockIds;
    }

    public ReduceStatus(int taskId, ShuffleBlockId[] shuffleBlockIds) {
        super(taskId,TaskStatusEnum.FINISHED);
        this.shuffleBlockIds = shuffleBlockIds;
    }

    public ReduceStatus(int taskId,TaskStatusEnum taskStatus) {
        super(taskId,taskStatus);
    }

    public ReduceStatus(int taskId,TaskStatusEnum taskStatus, String errorMsg,String errorStackTrace) {
        super(taskId,taskStatus, errorMsg,errorStackTrace);
    }

    public void setShuffleBlockHostAndPort(String host,int port){
        for(ShuffleBlockId shuffleBlockId:shuffleBlockIds){
            shuffleBlockId.setHostAndPort(host,port);
        }
    }
}
