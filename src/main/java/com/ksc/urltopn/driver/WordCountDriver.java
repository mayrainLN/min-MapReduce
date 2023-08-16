package com.ksc.urltopn.driver;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ksc.urltopn.datasourceapi.*;
import com.ksc.urltopn.rpc.Driver.DriverActor;
import com.ksc.urltopn.rpc.Driver.DriverSystem;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.*;
import com.ksc.urltopn.task.map.MapFunction;
import com.ksc.urltopn.task.map.MapTaskContext;
import com.ksc.urltopn.task.reduce.ReduceFunction;
import com.ksc.urltopn.task.reduce.ReduceTaskContext;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordCountDriver {

    public static void main(String[] args) {
        DriverEnv.host = "127.0.0.1";
        DriverEnv.port = 4040;
//        String inputPath = "/tmp/input";
        String inputPath = "E:/MapReduce/input";
//        String outputPath = "/tmp/output";
        String outputPath = "E:/MapReduce/output";
        String applicationId = "wordcount_001";
        int reduceTaskNum = 2;

        FileFormat fileFormat = new UnsplitFileFormat();
        PartionFile[] partionFiles = fileFormat.getSplits(inputPath, 1000);

        TaskManager taskScheduler = DriverEnv.taskManager;

        ActorSystem executorSystem = DriverSystem.getExecutorSystem();
        ActorRef driverActorRef = executorSystem.actorOf(Props.create(DriverActor.class), "driverActor");
        System.out.println("ServerActor started at: " + driverActorRef.path().toString());


        int mapStageId = 0;
        //添加stageId和任务的映射
        taskScheduler.registerBlockingQueue(mapStageId, new LinkedBlockingQueue());
        for (PartionFile partionFile : partionFiles) {
            MapFunction wordCountMapFunction = new MapFunction<String, KeyValue>() {
                //todo copy 学生实现 定义maptask处理数据的规则
                @Override
                public Stream<KeyValue> map(Stream<String> stream) {
                    String regex = "http://[^\\s\"\\n]*";
                    Pattern pattern = Pattern.compile(regex);
                    AtomicInteger n = new AtomicInteger();
                    return stream.flatMap(line -> {
                                n.getAndIncrement();
                                // 已修正 只读取url
                                Matcher matcher = pattern.matcher(line);
                                List<String> matchedStrings = new ArrayList<>();
                                while (matcher.find()) {
                                    String group = matcher.group();
                                    System.out.println(group);
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

                    int topN = 3;
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
        System.out.println("job finished");

        /**
         * 开启RPC服务
         */
//        try {
//            TServerSocket tServerSocket = new TServerSocket(9090);
//            TBinaryProtocol.Factory factory = new TBinaryProtocol.Factory();
//            TSimpleServer.Args tArgs = new TSimpleServer.Args(tServerSocket);
//            tArgs.inputProtocolFactory(factory);
//            tArgs.outputProtocolFactory(factory);
//
//            UrlTopNServiceImp urlTopNServiceImp = new UrlTopNServiceImp(outputPath);
//            tArgs.processor(new com.ksc.urltopn.thrift.UrlTopNService.Processor<>(urlTopNServiceImp));
//            TServer tServer = new TSimpleServer(tArgs);
//            System.out.println("Starting the simple server...");
//            tServer.serve();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }
}
