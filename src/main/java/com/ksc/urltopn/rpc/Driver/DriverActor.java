package com.ksc.urltopn.rpc.Driver;

import akka.actor.AbstractActor;
import com.ksc.urltopn.driver.DriverEnv;
import com.ksc.urltopn.rpc.ExecutorRegister;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;

public class DriverActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // 收到Executor发起的更新task状态请求
                .match(TaskStatus.class, mapStatus -> {
                    System.out.println("ExecutorActor received mapStatus:"+mapStatus);
                    if(mapStatus.getTaskStatus() == TaskStatusEnum.FAILED) {
                        System.err.println("task status taskId:"+mapStatus.getTaskId());
                        System.err.println("task status errorMsg:"+mapStatus.getErrorMsg());
                        System.err.println("task status errorStackTrace:\n"+mapStatus.getErrorStackTrace());
                    }
                    DriverEnv.taskManager.updateTaskStatus(mapStatus);
                    DriverEnv.taskScheduler.updateTaskStatus(mapStatus);
                })
                .match(ExecutorRegister.class, executorRegister -> {
                    System.out.println("ExecutorActor received executorRegister:"+executorRegister);
                    DriverEnv.executorManager.updateExecutorRegister(executorRegister);
                })
                .match(Object.class, message -> {
                    //处理不了的消息
                    System.err.println("unhandled message:" + message);
                })
                .build();
    }
}
