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

        String masterUrl = System.getProperty("masterHost")+":"+System.getProperty("masterPort");
        ExecutorEnv.driverUrl="akka.tcp://DriverSystem@"+masterUrl+"/user/driverActor";

        ExecutorEnv.core=Integer.parseInt(System.getProperty("core"));
        ExecutorEnv.host = System.getProperty("host");
        ExecutorEnv.port = Integer.parseInt(System.getProperty("port")); // akka
        ExecutorEnv.memory = System.getProperty("memory");
        ExecutorEnv.executorUrl="akka.tcp://ExecutorSystem@"+ ExecutorEnv.host+":"+ExecutorEnv.port+"/user/executorActor";
        ExecutorEnv.shufflePort=Integer.parseInt(System.getProperty("nettyPort"));

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
