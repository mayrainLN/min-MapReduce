package com.ksc.urltopn.task.reduce;

import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;

public class ReduceStatus extends TaskStatus {

    public ReduceStatus(int taskId) {
        super(taskId, TaskStatusEnum.FINISHED);
    }

    public ReduceStatus(int taskId,TaskStatusEnum taskStatus) {
        super(taskId,taskStatus);
    }

    public ReduceStatus(int taskId,TaskStatusEnum taskStatus, String errorMsg,String errorStackTrace) {
        super(taskId,taskStatus, errorMsg,errorStackTrace);
    }

}
