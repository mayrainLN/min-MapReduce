package com.ksc.urltopn.rpc.Executor;

import akka.actor.ActorRef;
import com.ksc.urltopn.rpc.ExecutorRegister;
import com.ksc.urltopn.task.map.MapStatus;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.TaskStatusEnum;
import com.ksc.urltopn.task.merge.MergeStatus;
import com.ksc.urltopn.task.reduce.ReduceStatus;
import com.ksc.urltopn.worker.ExecutorEnv;

public class ExecutorRpc {

    public static void updateTaskMapStatue(TaskStatus taskStatus){
        if (taskStatus instanceof MapStatus && ((MapStatus) taskStatus).getTaskStatus() == TaskStatusEnum.FINISHED){
            ((MapStatus) taskStatus).setShuffleBlockHostAndPort(ExecutorEnv.host,ExecutorEnv.shufflePort);
        }
        if(taskStatus instanceof ReduceStatus && ((ReduceStatus) taskStatus).getTaskStatus() == TaskStatusEnum.FINISHED){
            ((ReduceStatus) taskStatus).setShuffleBlockHostAndPort(ExecutorEnv.host,ExecutorEnv.shufflePort);
        }
        ExecutorSystem.getDriverRef().tell(taskStatus, ActorRef.noSender());
    }

    public static void register(ExecutorRegister executorRegister){
        ExecutorSystem.getDriverRef().tell(executorRegister, ActorRef.noSender());
    }
}
