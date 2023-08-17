package com.ksc.urltopn.task.reduce;

import com.ksc.urltopn.datasourceapi.PartionWriter;
import com.ksc.urltopn.shuffle.ShuffleBlockId;
import com.ksc.urltopn.shuffle.nettyimpl.client.ShuffleClient;
import com.ksc.urltopn.task.KeyValue;
import com.ksc.urltopn.task.Task;
import com.ksc.urltopn.task.TaskStatusEnum;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReduceTask extends Task {

    ShuffleBlockId[] shuffleBlockId;
    //String destDir;
    ReduceFunction reduceFunction;
    PartionWriter partionWriter;


    public ReduceTask(ReduceTaskContext reduceTaskContext) {
        super(reduceTaskContext);
        this.shuffleBlockId = reduceTaskContext.getShuffleBlockId();
        //this.destDir = reduceTaskContext.getDestDir();
        this.reduceFunction = reduceTaskContext.getReduceFunction();
        this.partionWriter = reduceTaskContext.getPartionWriter();
    }


    public ReduceStatus runTask() throws Exception {
        System.out.println("stageId:" + stageId);
        // reduce任务
        if (stageId.equals("stage_1")) {
            Stream<KeyValue> stream = Stream.empty();
            for (ShuffleBlockId shuffleBlockId : shuffleBlockId) {
                stream = Stream.concat(stream, new ShuffleClient().fetchShuffleData(shuffleBlockId));
            }
            Stream reduceStream = reduceFunction.reduce(stream);
            partionWriter.write(reduceStream);

            return new ReduceStatus(super.taskId, TaskStatusEnum.FINISHED);
        }

        if(stageId.equals("stage_2")){
            return merge(3,"E:/MapReduce/output");
        }

        return new ReduceStatus(super.taskId, TaskStatusEnum.FAILED);
    }

    public ReduceStatus merge(int topN, String outputPath) throws IOException {
        // 先写死
        // merge阶段
        File parent = new File(outputPath);
        File[] reduceResultFiles = parent.listFiles();

        Map<String, Integer> map = new HashMap();
        for (File reduceResultFile : reduceResultFiles) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(reduceResultFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("\\s+");
                if (split.length != 2) {
                    continue;
                }
                Integer count = Integer.valueOf(split[1]);
                map.put(split[0], count);
            }
            reduceResultFile.delete();
        }

        List<Map.Entry<String, Integer>> urlCountKVList = map.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());

        int lastTopNValue;
        if (urlCountKVList.isEmpty()) {
            return new ReduceStatus(super.taskId, TaskStatusEnum.FINISHED);
        }
        if (urlCountKVList.size() < topN) {
            lastTopNValue = urlCountKVList.get(urlCountKVList.size() - 1).getValue();
        } else {
            lastTopNValue = urlCountKVList.get(topN - 1).getValue();
        }

        File outputFile = new File(outputPath + "/mergeResult.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        map.entrySet().stream()
                .filter(e -> e.getValue() >= lastTopNValue)
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(e -> {
                    try {
                        writer.write(e.getKey() + " " + e.getValue() + "\n");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

        writer.close();
        return new ReduceStatus(super.taskId, TaskStatusEnum.FINISHED);
    }
}
