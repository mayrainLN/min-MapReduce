package com.ksc.urltopn.rpc.Driver;

import akka.actor.ActorRef;
import com.ksc.urltopn.task.TaskContext;

public class DriverRpc {

    public static void submit(String executorUrl,TaskContext taskContext){
        System.out.println("DriverRpc submit executorUrl:"+executorUrl+",taskContext:"+taskContext);
        DriverSystem.getExecutorRef(executorUrl).tell(taskContext, ActorRef.noSender());
    }


}
