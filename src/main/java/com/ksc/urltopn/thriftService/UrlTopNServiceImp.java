package com.ksc.urltopn.thriftService;

import com.ksc.urltopn.*;
import com.ksc.urltopn.datasourceapi.*;
import com.ksc.urltopn.driver.DriverEnv;
import com.ksc.urltopn.driver.TaskManager;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.task.KeyValue;
import com.ksc.urltopn.task.map.MapFunction;
import com.ksc.urltopn.task.map.MapTaskContext;
import com.ksc.urltopn.task.reduce.ReduceFunction;
import com.ksc.urltopn.task.reduce.ReduceTaskContext;
import org.apache.thrift.TException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/16 15:06
 * @description :
 */
public class UrlTopNServiceImp implements UrlTopNService.Iface, Serializable {
    private static final ExecutorService executorService = new ThreadPoolExecutor(1, 10, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    class Application implements Runnable,Serializable{

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
            FileFormat fileFormat = new UnsplitFileFormat();
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
                MapTaskContext mapTaskContext = new MapTaskContext(applicationId, "stage_" + mapStageId, taskScheduler.generateTaskId(), partionFile.getPartionId(), partionFile,
                        fileFormat.createReader(), reduceTaskNum, wordCountMapFunction);
                taskScheduler.addTaskContext(mapStageId, mapTaskContext);
            }

            //提交stageId
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
             * merge任务的stageId为2
             */
            int mergeStageId = 2;
            taskScheduler.registerBlockingQueue(mergeStageId, new LinkedBlockingQueue());
            PartionWriter mergePartitionWriter = fileFormat.createWriter(outputPath, 0);
            ReduceTaskContext reduceTaskContext = new ReduceTaskContext(applicationId, "stage_" + mergeStageId, taskScheduler.generateTaskId(), 0, new ShuffleBlockId[]{null}, new ReduceFunction() {
                @Override
                public Stream<KeyValue> reduce(Stream stream) throws IOException {
                    Map<String, Integer> map = new HashMap<>();
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

            // TODO 去掉这个阻塞
            DriverEnv.taskScheduler.waitStageFinish(mergeStageId);

            DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(mapStageId);
            DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(reduceStageId);
            DriverEnv.taskManager.stageIdToBlockingQueueMap.remove(mergeStageId);
            DriverEnv.taskManager.stageMap.remove(mapStageId);
            DriverEnv.taskManager.stageMap.remove(reduceStageId);
            DriverEnv.taskManager.stageMap.remove(mergeStageId);

            System.out.println("job finished");
            ApplicationResultDB.setStatus(applicationId, AppStatusEnum.FINISHED);
        }
    }

    public UrlTopNServiceImp() {
    }

    @Override
    public List<UrlTopNResult> getTopNAppResult(String applicationId) throws TException {
        if(ApplicationResultDB.getResult(applicationId) == null){
            List<UrlTopNResult> urlTopNResults = new ArrayList<>();
            String outputPath = ApplicationResultDB.getOutputPath(applicationId);
            for (File file : new File(outputPath).listFiles()) {
                try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
                    String line;
                    while((line = bufferedReader.readLine()) != null){
                        String[] split = line.split("\\s+");
                        if(split.length == 2){
                            urlTopNResults.add(new UrlTopNResult(split[0], Integer.valueOf(split[1])));
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ApplicationResultDB.setResult(applicationId, urlTopNResults);
            return urlTopNResults;
        }
        return ApplicationResultDB.getResult(applicationId);
    }

    @Override
    public UrlTopNAppResponse submitApp(UrlTopNAppRequest urlTopNAppRequest) throws TException {
        String inputPath = urlTopNAppRequest.getInputPath();
        String outputPath = urlTopNAppRequest.getOuputPath();
        String applicationId = urlTopNAppRequest.getApplicationId();
        int reduceTaskNum = urlTopNAppRequest.getNumReduceTasks();
        int splitSize = urlTopNAppRequest.getSplitSize();
        int topN = urlTopNAppRequest.getTopN();

        ApplicationResultDB.setStatus(applicationId, AppStatusEnum.ACCEPT);
        ApplicationResultDB.addOutputPath(applicationId, outputPath);
        executorService.submit(new Application(inputPath, outputPath, applicationId, reduceTaskNum, splitSize, topN));
        //submitApp 直接返回，返回的是App的状态。
        return new UrlTopNAppResponse(applicationId, ApplicationResultDB.getStatus(applicationId).getCode());
    }

    @Override
    public UrlTopNAppResponse getAppStatus(String applicationId) throws TException {
        return new UrlTopNAppResponse(applicationId, ApplicationResultDB.getStatus(applicationId).getCode());
    }
}
