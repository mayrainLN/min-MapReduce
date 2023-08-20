package com.ksc.urltopn.task;

import com.ksc.urltopn.rpc.Executor.ExecutorRpc;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Task<T>  implements Runnable {

    protected String applicationId;
    protected String stageId;
    protected int taskId;
    protected int partionId;

    public Task(TaskContext taskContext) {
        this.applicationId = taskContext.getApplicationId();
        this.stageId = taskContext.getStageId();
        this.taskId = taskContext.taskId;
        this.partionId = taskContext.getPartionId();
    }

    public void run() {
        try{
            // 向Driver更新task的状态
            ExecutorRpc.updateTaskMapStatue(new TaskStatus(taskId,TaskStatusEnum.RUNNING));
            TaskStatus taskStatus = runTask();// 运行实际工作
            // 更新task的执行结果。其中还会补充Excutor的Url和Port。方便后续流程其他节点请求本节点的shuffle结果
            if (taskStatus!=null){
                ExecutorRpc.updateTaskMapStatue(taskStatus);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            System.err.println("task："+taskId+" failed：" );
            e.printStackTrace();
            TaskStatus taskStatus = new TaskStatus(taskId,TaskStatusEnum.FAILED,e.getMessage(),stackTrace);
            ExecutorRpc.updateTaskMapStatue(taskStatus);
            Thread.currentThread().interrupt();
        }
    }

    public abstract TaskStatus runTask() throws Exception;


}
