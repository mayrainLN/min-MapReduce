package com.ksc.urltopn;

import com.ksc.urltopn.datasourceapi.FileFormat;
import com.ksc.urltopn.datasourceapi.PartionFile;
import com.ksc.urltopn.datasourceapi.PartionWriter;
import com.ksc.urltopn.datasourceapi.SplitFileFormat;
import com.ksc.urltopn.driver.DriverEnv;
import com.ksc.urltopn.driver.TaskManager;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.KeyValue;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.map.MapFunction;
import com.ksc.urltopn.task.map.MapTaskContext;
import com.ksc.urltopn.task.merge.MergeFunction;
import com.ksc.urltopn.task.merge.MergeStatus;
import com.ksc.urltopn.task.merge.MergeTaskContext;
import com.ksc.urltopn.task.reduce.ReduceFunction;
import com.ksc.urltopn.task.reduce.ReduceTaskContext;
import org.checkerframework.checker.units.qual.K;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/20 11:37
 * @description :
 */
public class AppManager {
    private static final ExecutorService executorService = new ThreadPoolExecutor(1, 10, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    public static void submitApplication(String inputPath, String outputPath, String applicationId, int reduceTaskNum, int splitSize, int topN) {
        ApplicationResultDB.setStatus(applicationId, AppStatusEnum.ACCEPT);
        Application application = new Application(inputPath, outputPath, applicationId, reduceTaskNum, splitSize, topN);
        executorService.submit(application);
    }

    public static class Application implements Runnable, Serializable {

        public Application(String inputPath, String outputPath, String applicationId, int reduceTaskNum, int splitSize, int topN) {
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.applicationId = applicationId;
            this.reduceTaskNum = reduceTaskNum;
            this.splitSize = splitSize;
            this.topN = topN;
        }

        String inputPath;
        String outputPath;
        String applicationId;
        int reduceTaskNum;
        int splitSize;
        int topN;

        @Override
        public void run() {
            FileFormat fileFormat = new SplitFileFormat();
            PartionFile[] partionFiles = fileFormat.getSplits(inputPath, splitSize);

            TaskManager taskScheduler = DriverEnv.taskManager;

            /**
             * map任务的stageId为0
             */
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
                // 生成任务（每个分片文件需要生成一个map任务）
                MapTaskContext mapTaskContext = new MapTaskContext(applicationId, "stage_" + mapStageId, taskScheduler.generateTaskId(), partionFile.getPartionId(), partionFile,
                        fileFormat.createReader(), reduceTaskNum, wordCountMapFunction);
                // 注册任务。其中建立stageId和任务、任务id的映射
                taskScheduler.addTaskContext(mapStageId, mapTaskContext);
            }

            //提交map，开始调度
            DriverEnv.taskScheduler.submitTask(mapStageId);
            // 设置app为Running
            ApplicationResultDB.setStatus(applicationId, AppStatusEnum.RUNNING);
            DriverEnv.taskScheduler.waitStageFinish(mapStageId);

            /**
             * reduce任务的stageId为1
             */
            int reduceStageId = 1;
            taskScheduler.registerBlockingQueue(reduceStageId, new LinkedBlockingQueue());
            for (int i = 0; i < reduceTaskNum; i++) {
                // map后的shuffle文件的元信息。
                // executor在执行完任务后，会向driver更新任务状态，内部包含shuffle文件的元信息
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
             * merge任务的stageId为1
             */
            int mergeStageId = 2;
            taskScheduler.registerBlockingQueue(mergeStageId, new LinkedBlockingQueue());
            // map后的shuffle文件的元信息。
            // executor在执行完任务后，会向driver更新任务状态，内部包含shuffle文件的元信息
            ShuffleBlockId[] stageShuffleIds = taskScheduler.getAllReduceShuffle(reduceStageId);
            MergeFunction<String, Integer> mergeFunction = stream -> {
                Map<String, Integer> map = new HashMap<>();
                stream.forEach(kv -> {
                    map.put(kv.getKey(), kv.getValue());
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
            };
            PartionWriter partionWriter = fileFormat.createWriter(outputPath, 0);
            MergeTaskContext mergeTaskContext = new MergeTaskContext(applicationId, "stage_" + reduceStageId, taskScheduler.generateTaskId(), 0, stageShuffleIds, mergeFunction, partionWriter);
            taskScheduler.addTaskContext(mergeStageId, mergeTaskContext);
            DriverEnv.taskScheduler.submitTask(mergeStageId);
            DriverEnv.taskScheduler.waitStageFinish(mergeStageId);

            Integer mergeTaskId = DriverEnv.taskManager.stageMap.get(mergeStageId).get(0);
            TaskStatus taskStatus = DriverEnv.taskScheduler.taskManager.taskStatusMap.get(mergeTaskId);
            List<UrlTopNResult> urlTopNResults = ((MergeStatus) taskStatus).geturlTopNResults();

            ApplicationResultDB.setResult(applicationId, urlTopNResults);
            ApplicationResultDB.setStatus(applicationId, AppStatusEnum.FINISHED);
            System.out.println("完成任务:" + applicationId);

            //写结果到output文件
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath+"/"+applicationId+".txt"))) {
                for (UrlTopNResult urlTopNResult : urlTopNResults) {
                    bufferedWriter.write(urlTopNResult.getUrl() + "\t" + urlTopNResult.getCount());
                    bufferedWriter.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(mapStageId);
            DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(reduceStageId);
            DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(mergeStageId);
            DriverEnv.taskManager.stageMap.remove(mapStageId);
            DriverEnv.taskManager.stageMap.remove(reduceStageId);
            DriverEnv.taskManager.stageMap.remove(mergeStageId);


        }
    }
}
