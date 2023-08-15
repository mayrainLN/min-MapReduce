package com.ksc.wordcount.driver;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ksc.wordcount.datasourceapi.*;
import com.ksc.wordcount.rpc.Driver.DriverActor;
import com.ksc.wordcount.rpc.Driver.DriverSystem;
import com.ksc.wordcount.shuffle.ShuffleBlockId;
import com.ksc.wordcount.task.*;
import com.ksc.wordcount.task.map.MapFunction;
import com.ksc.wordcount.task.map.MapTaskContext;
import com.ksc.wordcount.task.reduce.ReduceFunction;
import com.ksc.wordcount.task.reduce.ReduceTaskContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WordCountDriver {

    public static void main(String[] args) {
        DriverEnv.host= "127.0.0.1";
        DriverEnv.port = 4040;
//        String inputPath = "/tmp/input";
        String inputPath = "E:/MapReduce/input";
//        String outputPath = "/tmp/output";
        String outputPath = "E:/MapReduce/output";
        String applicationId = "wordcount_001";
        int reduceTaskNum = 2;

        FileFormat fileFormat = new UnsplitFileFormat();
        PartionFile[]  partionFiles = fileFormat.getSplits(inputPath, 1000);

        TaskManager taskScheduler = DriverEnv.taskManager;

        ActorSystem executorSystem = DriverSystem.getExecutorSystem();
        ActorRef driverActorRef = executorSystem.actorOf(Props.create(DriverActor.class), "driverActor");
        System.out.println("ServerActor started at: " + driverActorRef.path().toString());


        int mapStageId = 0 ;
        //添加stageId和任务的映射
        taskScheduler.registerBlockingQueue(mapStageId, new LinkedBlockingQueue());
        for (PartionFile partionFile : partionFiles) {
            MapFunction wordCountMapFunction = new MapFunction<String, KeyValue>() {
                //todo copy 学生实现 定义maptask处理数据的规则
                @Override
                public Stream<KeyValue> map(Stream<String> stream) {
                    return stream.flatMap(line->
                                Stream.of(line.split("\\s+"))
                            )
                            .map(word->new KeyValue(word,1));

//                    stream.flatMap(line -> {
//                                        // Create a regex pattern to match URLs
//                                        String regex = "(?<=^|\\s|\")https?://(?:[^\"\\s]+)(?=\\s|\")";
//                                        Pattern pattern = Pattern.compile(regex);
//                                        // 已修正 只读取url
//                                        Matcher matcher = pattern.matcher(line);
//                                        if (matcher.find()) {
//                                            return matcher.group(1);
//                                        }
//                                        return null;
//                                    }
//                            )
//                            .map(word -> new KeyValue(word, 1));
                };
            };
            MapTaskContext mapTaskContext = new MapTaskContext(applicationId, "stage_"+mapStageId, taskScheduler.generateTaskId(), partionFile.getPartionId(), partionFile,
                    fileFormat.createReader(), reduceTaskNum, wordCountMapFunction);
            taskScheduler.addTaskContext(mapStageId,mapTaskContext);
        }

        //提交stageId
        DriverEnv.taskScheduler.submitTask(mapStageId);
        DriverEnv.taskScheduler.waitStageFinish(mapStageId);


        int reduceStageId = 1 ;
        taskScheduler.registerBlockingQueue(reduceStageId, new LinkedBlockingQueue());
        for(int i = 0; i < reduceTaskNum; i++){
            ShuffleBlockId[] stageShuffleIds = taskScheduler.getStageShuffleIdByReduceId(mapStageId, i);
            ReduceFunction<String, Integer, String, Integer> reduceFunction = new ReduceFunction<String, Integer, String, Integer>() {

                @Override
                public Stream<KeyValue<String, Integer>> reduce(Stream<KeyValue<String, Integer>> stream) {
                    HashMap<String, Integer> map = new HashMap<>();

                    //todo copy 学生实现 定义reducetask处理数据的规则 也就是Reduce的处理逻辑
                    stream.forEach(e->{
                        String key = e.getKey();
                        Integer value = e.getValue();
                        if(map.containsKey(key)) {
                            map.put(key, map.get(key) + value);
                        }else{
                            map.put(key, value);
                        }
                    });

                    return map.entrySet().stream().map(e -> new KeyValue(e.getKey(), e.getValue()));
                }
            };
            PartionWriter partionWriter = fileFormat.createWriter(outputPath, i);
            ReduceTaskContext reduceTaskContext = new ReduceTaskContext(applicationId, "stage_" + reduceStageId, taskScheduler.generateTaskId(), i, stageShuffleIds, reduceFunction, partionWriter);
            taskScheduler.addTaskContext(reduceStageId, reduceTaskContext);
        }

        DriverEnv.taskScheduler.submitTask(reduceStageId);
        DriverEnv.taskScheduler.waitStageFinish(reduceStageId);
        System.out.println("job finished");
    }
}
