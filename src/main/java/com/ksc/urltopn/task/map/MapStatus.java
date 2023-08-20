package com.ksc.urltopn.task.map;

import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;

public class MapStatus extends TaskStatus {
    // Map的结果shuffle文件的元信息。在Executor执行完毕后，会通过akka发送过来，内部包含executor的host和port
    ShuffleBlockId[] shuffleBlockIds;

    public MapStatus(int taskId,ShuffleBlockId[] shuffleBlockIds) {
        super(taskId, TaskStatusEnum.FINISHED);
        this.shuffleBlockIds = shuffleBlockIds;
    }

    public MapStatus(int taskId,TaskStatusEnum taskStatus,ShuffleBlockId[] shuffleBlockIds) {
        super(taskId,taskStatus);
        this.shuffleBlockIds = shuffleBlockIds;
    }

    public MapStatus(int taskId,TaskStatusEnum taskStatus, String errorMsg,String errorStackTrace) {
        super(taskId,taskStatus, errorMsg,errorStackTrace);
    }

    public ShuffleBlockId[] getShuffleBlockIds() {
        return shuffleBlockIds;
    }

    public void setShuffleBlockHostAndPort(String host,int port){
        for(ShuffleBlockId shuffleBlockId:shuffleBlockIds){
            shuffleBlockId.setHostAndPort(host,port);
        }
    }



}
