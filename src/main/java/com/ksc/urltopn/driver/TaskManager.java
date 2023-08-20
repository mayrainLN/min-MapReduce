package com.ksc.urltopn.driver;

import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.map.MapStatus;
import com.ksc.urltopn.task.TaskContext;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;
import com.ksc.urltopn.task.merge.MergeStatus;
import com.ksc.urltopn.task.reduce.ReduceStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class TaskManager {

    /**
     * stageId和task队列的映射
     */
    public Map<Integer,BlockingQueue<TaskContext>> stageIdToBlockingQueueMap = new HashMap<>();

    /**
     * stageId和taskId的映射
     */
    public Map<Integer, List<Integer>> stageMap = new HashMap<>();

    /**
     * taskId和task状态的映射
     */
    public Map<Integer, TaskStatus> taskStatusMap = new HashMap<>();


    public BlockingQueue<TaskContext> getBlockingQueue(int stageId) {
        return stageIdToBlockingQueueMap.get(stageId);
    }

    public void registerBlockingQueue(int stageId,BlockingQueue blockingQueue) {
        stageIdToBlockingQueueMap.put(stageId,blockingQueue);
    }

    public void addTaskContext(int stageId, TaskContext taskContext) {
        //建立stageId和任务的映射
        getBlockingQueue(stageId).offer(taskContext);
        if(stageMap.get(stageId) == null){
            stageMap.put(stageId, new ArrayList());
        }
        //建立stageId和任务Id的映射
        stageMap.get(stageId).add(taskContext.getTaskId());
    }


    public StageStatusEnum getStageTaskStatus(int stageId){
        //todo copy 学生实现 获取指定stage的执行状态，如果该stage下的所有task均执行成功，返回FINISHED
        for (int taskId: stageMap.get(stageId)){
            TaskStatus taskStatus = taskStatusMap.get(taskId);
            if(taskStatus==null){
                return StageStatusEnum.RUNNING;
            }
            if (taskStatus.getTaskStatus().equals(TaskStatusEnum.FAILED)){
                return StageStatusEnum.FAILED;
            }
            if (taskStatus.getTaskStatus().equals(TaskStatusEnum.RUNNING)){
                return StageStatusEnum.RUNNING;
            }
            if(taskStatus.getTaskStatus().equals(TaskStatusEnum.FINISHED)){
                continue;
            }
        }
        return StageStatusEnum.FINISHED;
    }

    public ShuffleBlockId[] getStageShuffleIdByReduceId(int stageId,int reduceId){
        List<ShuffleBlockId> shuffleBlockIds = new ArrayList<>();
        for(int taskId:stageMap.get(stageId)){
            ShuffleBlockId shuffleBlockId = ((MapStatus) taskStatusMap.get(taskId)).getShuffleBlockIds()[reduceId];
            shuffleBlockIds.add(shuffleBlockId);
        }
        return shuffleBlockIds.toArray(new ShuffleBlockId[shuffleBlockIds.size()]);
    }

    public ShuffleBlockId[] getAllReduceShuffle(int stageId){
        List<ShuffleBlockId> shuffleBlockIds = new ArrayList<>();
        for(int taskId:stageMap.get(stageId)){
            for (ShuffleBlockId shuffleBlockId : ((ReduceStatus) taskStatusMap.get(taskId)).getShuffleBlockIds()) {
                shuffleBlockIds.add(shuffleBlockId);
            }
        }
        return shuffleBlockIds.toArray(new ShuffleBlockId[shuffleBlockIds.size()]);
    }


    public void updateTaskStatus(TaskStatus taskStatus) {
        taskStatusMap.put(taskStatus.getTaskId(),taskStatus);
    }


    private int maxTaskId = 0;

    public int generateTaskId() {
        return maxTaskId++;
    }


}
