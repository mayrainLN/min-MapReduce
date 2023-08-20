package com.ksc.urltopn.rpc.Executor;

import akka.actor.AbstractActor;
import com.ksc.urltopn.task.map.MapTaskContext;
import com.ksc.urltopn.task.map.ShuffleMapTask;
import com.ksc.urltopn.task.merge.MergeTask;
import com.ksc.urltopn.task.merge.MergeTaskContext;
import com.ksc.urltopn.task.reduce.ReduceTask;
import com.ksc.urltopn.task.reduce.ReduceTaskContext;
import com.ksc.urltopn.worker.ExecutorThreadPoolFactory;

public class ExecutorActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MapTaskContext.class, taskContext -> {
                    System.out.println("ExecutorActor received mapTaskContext:"+taskContext);
                    ExecutorThreadPoolFactory.getExecutorService().submit(new ShuffleMapTask(taskContext));
                })
                .match(ReduceTaskContext.class, taskContext -> {
                    System.out.println("ExecutorActor received reduceTaskContext:"+taskContext);
                    ExecutorThreadPoolFactory.getExecutorService().submit(new ReduceTask(taskContext));
                })
                .match(MergeTaskContext.class, taskContext -> {
                    System.out.println("ExecutorActor received mergeTaskContext:"+taskContext);
                    ExecutorThreadPoolFactory.getExecutorService().submit(new MergeTask(taskContext));
                })
                .match(Object.class, message -> {
                    //处理不了的消息
                    System.out.println("unhandled message:" + message);
                })
                .build();
    }
}
