package com.ksc.urltopn.task.map;

import com.ksc.urltopn.datasourceapi.PartionFile;
import com.ksc.urltopn.datasourceapi.PartionReader;
import com.ksc.urltopn.task.TaskContext;

public class MapTaskContext extends TaskContext {

    PartionFile partiongFile;
    PartionReader partionReader;
    int reduceTaskNum;
    MapFunction mapFunction;


    public MapTaskContext(String applicationId, String stageId, int taskId, int partionId, PartionFile partiongFile, PartionReader partionReader, int reduceTaskNum, MapFunction mapFunction) {
        super(applicationId, stageId, taskId, partionId);
        this.partiongFile = partiongFile;
        this.partionReader = partionReader;
        this.reduceTaskNum = reduceTaskNum;
        this.mapFunction = mapFunction;
    }

    public PartionFile getPartiongFile() {
        return partiongFile;
    }

    public PartionReader getPartionReader() {
        return partionReader;
    }

    public int getReduceTaskNum() {
        return reduceTaskNum;
    }

    public MapFunction getMapFunction() {
        return mapFunction;
    }

}
