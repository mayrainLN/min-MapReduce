package com.ksc.urltopn.driver;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ksc.urltopn.AppManager;
import com.ksc.urltopn.AppStatusEnum;
import com.ksc.urltopn.ApplicationResultDB;
import com.ksc.urltopn.UrlTopNService;
import com.ksc.urltopn.rpc.Driver.DriverActor;
import com.ksc.urltopn.rpc.Driver.DriverSystem;
import com.ksc.urltopn.thriftService.UrlTopNServiceImp;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;

import java.io.*;

public class UrlTopNDriver {

    public static String ip;
    public static int akkaPort;
    public static int thriftPort;
    public static String memory; // 不知道在哪用

    private static void readConf() throws IOException {
//        String masterPath = System.getProperty("master.conf.path");
        String filePath = "./master.conf";
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        br.readLine(); //把注释读完
        line = br.readLine();
        String[] parts = line.split("\\s+");
        ip = parts[0];
        akkaPort = Integer.parseInt(parts[1]);
        thriftPort = Integer.parseInt(parts[2]);
        memory = parts[3];

        System.out.println("读取master.conf配置...");
        // 使用读取的数据进行操作，例如打印或者传递给启动代码
        System.out.println("IP: " + ip);
        System.out.println("Akka Port: " + akkaPort);
        System.out.println("Thrift Port: " + thriftPort);
        System.out.println("Memory: " + memory);

        DriverEnv.host = ip;
        DriverEnv.port = akkaPort;

        ActorSystem executorSystem = DriverSystem.getExecutorSystem();
        ActorRef driverActorRef = executorSystem.actorOf(Props.create(DriverActor.class), "driverActor");
        System.out.println("ServerActor started at: " + driverActorRef.path().toString());
    }

    public static void submitTest() throws IOException {
        String filePath = "./urltopn.conf";
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\s+");
            String inputDir = parts[1];
            String outputDir = parts[2];
            String appId = parts[0];
            int topN = Integer.parseInt(parts[3]);
            int reduceTaskNum = Integer.parseInt(parts[4]);
            int splitSize = Integer.parseInt(parts[5]);
            ApplicationResultDB.setStatus(appId, AppStatusEnum.ACCEPT);
            ApplicationResultDB.addOutputPath(appId, outputDir);
            AppManager.submitApplication(inputDir, outputDir,appId, reduceTaskNum, splitSize,topN);
            System.out.println("提交测试用例:");
            System.out.println(appId+ " 输入路径" + inputDir+" 输出路径：" + outputDir+" topN" + topN+ " reduceTaskNum" + reduceTaskNum+ " splitSize" + splitSize);
        }
        System.out.println("测试用例已经全部提交");
    }

    public static void main(String[] args) throws IOException {
        readConf();
        submitTest();

        /**
         * 开启thrift RPC服务
         */
        try {
            TServerSocket tServerSocket = new TServerSocket(thriftPort);
            TBinaryProtocol.Factory factory = new TBinaryProtocol.Factory();
            TSimpleServer.Args tArgs = new TSimpleServer.Args(tServerSocket);
            tArgs.inputProtocolFactory(factory);
            tArgs.outputProtocolFactory(factory);

            UrlTopNServiceImp urlTopNServiceImp = new UrlTopNServiceImp();
            tArgs.processor(new UrlTopNService.Processor<UrlTopNService.Iface>(urlTopNServiceImp));
            TServer tServer = new TSimpleServer(tArgs);
            System.out.println("Starting the simple server...");
            tServer.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
