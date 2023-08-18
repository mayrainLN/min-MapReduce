package com.ksc.urltopn.driver;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ksc.urltopn.UrlTopNService;
import com.ksc.urltopn.datasourceapi.*;
import com.ksc.urltopn.rpc.Driver.DriverActor;
import com.ksc.urltopn.rpc.Driver.DriverSystem;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.*;
import com.ksc.urltopn.task.map.MapFunction;
import com.ksc.urltopn.task.map.MapTaskContext;
import com.ksc.urltopn.task.reduce.ReduceFunction;
import com.ksc.urltopn.task.reduce.ReduceStatus;
import com.ksc.urltopn.task.reduce.ReduceTaskContext;
import com.ksc.urltopn.thriftService.UrlTopNServiceImp;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.checkerframework.checker.units.qual.K;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordCountDriver {

    public static String ip;
    public static int akkaPort;
    public static int thriftPort;
    public static String memory; // 不知道在哪用

    private static void readConf() throws IOException {
        String filePath = "./bin/master.conf";
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
    }

    public static void main(String[] args) throws IOException {
        readConf();
        DriverEnv.host = ip;
        DriverEnv.port = akkaPort;
        String inputPath = "E:/MapReduce/1input";
        String outputPath = "E:/MapReduce/1output";

        TaskManager taskScheduler = DriverEnv.taskManager;

        ActorSystem executorSystem = DriverSystem.getExecutorSystem();
        ActorRef driverActorRef = executorSystem.actorOf(Props.create(DriverActor.class), "driverActor");
        System.out.println("ServerActor started at: " + driverActorRef.path().toString());



        int topN = 3;
        String applicationId = "application_001";

        /**
         * map任务的stageId为0
         */
        int mapStageId = 0;
        //添加stageId和任务的映射
        int reduceTaskNum = 3;
        FileFormat fileFormat = new UnsplitFileFormat();
        PartionFile[] partionFiles = fileFormat.getSplits(inputPath, 1000);

        taskScheduler.registerBlockingQueue(mapStageId, new LinkedBlockingQueue());
        for (PartionFile partionFile : partionFiles) {
            MapFunction wordCountMapFunction = new MapFunction<String, KeyValue>() {
                //todo copy 学生实现 定义maptask处理数据的规则
                @Override
                public Stream<KeyValue> map(Stream<String> stream) {
                    String regex = "http://[^\\s\"\\n]*";
                    Pattern pattern = Pattern.compile(regex);
                    return stream.flatMap(line -> {
                                // 已修正 只读取url
                                Matcher matcher = pattern.matcher(line);
                                List<String> matchedStrings = new ArrayList<>();
                                while (matcher.find()) {
                                    String group = matcher.group();
                                    matchedStrings.add(group);
                                }
                                return matchedStrings.stream()
                                        .map(url -> new KeyValue(url, 1));
                            }
                    );
                }
            };
            MapTaskContext mapTaskContext = new MapTaskContext(applicationId, "stage_" + mapStageId, taskScheduler.generateTaskId(), partionFile.getPartionId(), partionFile,
                    fileFormat.createReader(), reduceTaskNum, wordCountMapFunction);
            taskScheduler.addTaskContext(mapStageId, mapTaskContext);
        }

        //提交stageId
        DriverEnv.taskScheduler.submitTask(mapStageId);
        DriverEnv.taskScheduler.waitStageFinish(mapStageId);


        /**
         * reduce任务的stageId为1
         */
        int reduceStageId = 1;
        taskScheduler.registerBlockingQueue(reduceStageId, new LinkedBlockingQueue());
        for (int i = 0; i < reduceTaskNum; i++) {
            ShuffleBlockId[] stageShuffleIds = taskScheduler.getStageShuffleIdByReduceId(mapStageId, i);
            ReduceFunction<String, Integer, String, Integer> reduceFunction = new ReduceFunction<String, Integer, String, Integer>() {

                @Override
                public Stream<KeyValue<String, Integer>> reduce(Stream<KeyValue<String, Integer>> stream) {
                    HashMap<String, Integer> map = new HashMap<>();

                    //todo finish 学生实现 定义reducetask处理数据的规则 也就是Reduce的处理逻辑
                    stream.forEach(e -> {
                        String key = e.getKey();
                        Integer value = e.getValue();
                        if (map.containsKey(key)) {
                            map.put(key, map.get(key) + value);
                        } else {
                            map.put(key, value);
                        }
                    });

                    /**
                     * 自启动的TopN没传
                     */
                    List<Map.Entry<String, Integer>> urlCountKVList = map.entrySet().stream()
                            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                            .limit(topN)
                            .collect(Collectors.toList());

                    int lastTopNValue;
                    if (urlCountKVList.isEmpty()) {
                        return Stream.empty();
                    }
                    if (urlCountKVList.size() < topN) {
                        lastTopNValue = urlCountKVList.get(urlCountKVList.size() - 1).getValue();
                    } else {
                        lastTopNValue = urlCountKVList.get(topN - 1).getValue();
                    }

                    return map.entrySet().stream()
                            .filter(e -> e.getValue() >= lastTopNValue)
                            .map(e -> new KeyValue(e.getKey(), e.getValue()));
                }
            };
            PartionWriter partionWriter = fileFormat.createWriter(outputPath, i);
            ReduceTaskContext reduceTaskContext = new ReduceTaskContext(applicationId, "stage_" + reduceStageId, taskScheduler.generateTaskId(), i, stageShuffleIds, reduceFunction, partionWriter);
            taskScheduler.addTaskContext(reduceStageId, reduceTaskContext);
        }

        DriverEnv.taskScheduler.submitTask(reduceStageId);
        DriverEnv.taskScheduler.waitStageFinish(reduceStageId);

        /**
         * merge任务的stageId为2
         */
        int mergeStageId = 2;
        taskScheduler.registerBlockingQueue(mergeStageId, new LinkedBlockingQueue());
        PartionWriter mergePartitionWriter = fileFormat.createWriter(outputPath, 0);
        ReduceTaskContext reduceTaskContext = new ReduceTaskContext(applicationId, "stage_" + mergeStageId, taskScheduler.generateTaskId(), 0, new ShuffleBlockId[]{null}, new ReduceFunction() {
            @Override
            public Stream<KeyValue> reduce(Stream stream) throws IOException {
                Map<String,Integer> map= new HashMap<>();
                stream.forEach(line -> {
                    String lineStr = (String) line;
                    String[] split = lineStr.split("\\s+");
                    if (split.length == 2) {
                        Integer count = Integer.valueOf(split[1]);
                        map.put(split[0], count);
                    }
                });

                List<Map.Entry<String, Integer>> urlCountKVList = map.entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .limit(topN)
                        .collect(Collectors.toList());

                int lastTopNValue;

                if (urlCountKVList.size() < topN) {
                    lastTopNValue = urlCountKVList.get(urlCountKVList.size() - 1).getValue();
                } else {
                    lastTopNValue = urlCountKVList.get(topN - 1).getValue();
                }

                return map.entrySet().stream()
                        .filter(e -> e.getValue() >= lastTopNValue)
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .map(e -> {
                            return new KeyValue(e.getKey(), e.getValue());
                        });
            }
        }, mergePartitionWriter);

        taskScheduler.addTaskContext(mergeStageId, reduceTaskContext);

        DriverEnv.taskScheduler.submitTask(mergeStageId);
        DriverEnv.taskScheduler.waitStageFinish(mergeStageId);

        DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(mapStageId);
        DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(reduceStageId);
        DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(mergeStageId);
        DriverEnv.taskManager.stageMap.remove(mapStageId);
        DriverEnv.taskManager.stageMap.remove(reduceStageId);
        DriverEnv.taskManager.stageMap.remove(mergeStageId);

        System.out.println("job finished");


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
