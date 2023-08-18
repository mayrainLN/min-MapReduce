package com.ksc.urltopn.driver;

import com.ksc.urltopn.rpc.Driver.DriverRpc;
import com.ksc.urltopn.task.TaskContext;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class TaskScheduler {

    private TaskManager taskManager;
    private ExecutorManager executorManager ;

    /**
     * taskId和ExecutorUrl的映射
     */
    private Map<Integer,String> taskExecuotrMap=new HashMap<>();

    public TaskScheduler(TaskManager taskManager, ExecutorManager executorManager) {
        this.taskManager = taskManager;
        this.executorManager = executorManager;
    }

    public void submitTask(int stageId) {
        BlockingQueue<TaskContext> taskQueue = taskManager.getBlockingQueue(stageId);

        while (!taskQueue.isEmpty()) {
            //todo copy 学生实现 轮询给各个executor派发任务
            executorManager.getExecutorAvailableCoresMap().forEach((executorUrl, availableCores) -> {
                if (availableCores > 0 && !taskQueue.isEmpty()) {
                    TaskContext taskContext = taskQueue.poll();
                    if (taskContext != null) {
                        taskExecuotrMap.put(taskContext.getTaskId(), executorUrl);
                        executorManager.updateExecutorAvailableCores(executorUrl, -1);
                        DriverRpc.submit(executorUrl, taskContext);
                    }
                }
            });


            try {
                String executorAvailableCoresMapStr=executorManager.getExecutorAvailableCoresMap().toString();
                System.out.println("TaskScheduler submitTask stageId:"+stageId+",taskQueue size:"+taskQueue.size()+", executorAvailableCoresMap:" + executorAvailableCoresMapStr+ ",sleep 1000");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void waitStageFinish(int stageId){
        StageStatusEnum stageStatusEnum = taskManager.getStageTaskStatus(stageId);
        while (stageStatusEnum==StageStatusEnum.RUNNING){
            try {
                System.out.println("TaskScheduler waitStageFinish stageId:"+stageId+",sleep 1000");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stageStatusEnum = taskManager.getStageTaskStatus(stageId);
        }
        if(stageStatusEnum == StageStatusEnum.FAILED){
            System.err.println("stageId:"+stageId+" failed");
            System.exit(1);
        }
    }

    public void updateTaskStatus(TaskStatus taskStatus){
        if(taskStatus.getTaskStatus().equals(TaskStatusEnum.FINISHED)||taskStatus.getTaskStatus().equals(TaskStatusEnum.FAILED)){
            String executorUrl=taskExecuotrMap.get(taskStatus.getTaskId());
            // 某个任务完成了，给对应的executor增加一个可用的core
            executorManager.updateExecutorAvailableCores(executorUrl,1);
        }
    }


}
