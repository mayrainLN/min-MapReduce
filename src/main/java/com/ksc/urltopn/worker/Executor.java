package com.ksc.urltopn.worker;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ksc.urltopn.rpc.Executor.ExecutorActor;
import com.ksc.urltopn.rpc.Executor.ExecutorRpc;
import com.ksc.urltopn.rpc.Executor.ExecutorSystem;
import com.ksc.urltopn.rpc.ExecutorRegister;
import com.ksc.urltopn.shuffle.nettyimpl.server.ShuffleService;

public class Executor {


    public static void main(String[] args) throws InterruptedException {

        ExecutorEnv.host="127.0.0.1";
        ExecutorEnv.port=15050;
        ExecutorEnv.memory="512m";
        ExecutorEnv.driverUrl="akka.tcp://DriverSystem@127.0.0.1:4040/user/driverActor";
        ExecutorEnv.core=2;
        ExecutorEnv.executorUrl="akka.tcp://ExecutorSystem@"+ ExecutorEnv.host+":"+ExecutorEnv.port+"/user/executorActor";
        ExecutorEnv.shufflePort=7337;

        new Thread(() -> {
            try {
                new ShuffleService(ExecutorEnv.shufflePort).start();
            } catch (InterruptedException e) {
                new RuntimeException(e);
            }
        }).start();

        ActorSystem executorSystem = ExecutorSystem.getExecutorSystem();
        ActorRef clientActorRef = executorSystem.actorOf(Props.create(ExecutorActor.class), "executorActor");
        System.out.println("ServerActor started at: " + clientActorRef.path().toString());
        ExecutorRpc.register(new ExecutorRegister(ExecutorEnv.executorUrl,ExecutorEnv.memory,ExecutorEnv.core));


    }

}
