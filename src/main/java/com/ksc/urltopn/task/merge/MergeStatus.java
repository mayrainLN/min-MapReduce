package com.ksc.urltopn.task.merge;

import com.ksc.urltopn.UrlTopNResult;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;

import java.util.List;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/20 15:35
 * @description :
 */
public class MergeStatus extends TaskStatus {
    // Map的结果shuffle文件的元信息。在Executor执行完毕后，会通过akka发送过来，内部包含executor的host和port
    List<UrlTopNResult> urlTopNResults;
    public MergeStatus(int taskId) {
        super(taskId, TaskStatusEnum.FINISHED);
    }

    public MergeStatus(int taskId,TaskStatusEnum taskStatus) {
        super(taskId,taskStatus);
    }

    public MergeStatus(int taskId,TaskStatusEnum taskStatus, String errorMsg,String errorStackTrace) {
        super(taskId,taskStatus, errorMsg,errorStackTrace);
    }

    public void seturlTopNResults(List<UrlTopNResult> urlTopNResults){
        this.urlTopNResults = urlTopNResults;
    }

    public List<UrlTopNResult> geturlTopNResults(){
        return urlTopNResults;
    }
}
