package com.ksc.urltopn.shuffle;

import com.ksc.urltopn.task.KeyValue;
import com.ksc.urltopn.task.TaskStatus;
import com.ksc.urltopn.task.map.MapStatus;
import com.ksc.urltopn.task.reduce.ReduceStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.stream.Stream;

public class DirectShuffleWriter implements ShuffleWriter<KeyValue> {

    String baseDir;

    int reduceTaskNum;

    ObjectOutputStream[] fileWriters;

    ShuffleBlockId[] shuffleBlockIds ;

    public DirectShuffleWriter(String baseDir,String shuffleId,String  applicationId,int mapId, int reduceTaskNum) {
        this.baseDir = baseDir;
        this.reduceTaskNum = reduceTaskNum;
        fileWriters = new ObjectOutputStream[reduceTaskNum];
        shuffleBlockIds = new ShuffleBlockId[reduceTaskNum];
        for (int i = 0; i < reduceTaskNum; i++) {
            try {
                // 生成Map后获得的shuffle文件的元信息
                shuffleBlockIds[i]=new ShuffleBlockId(baseDir,applicationId,shuffleId,mapId,i);
                new File(shuffleBlockIds[i].getShuffleParentPath()).mkdirs();
                fileWriters[i] = new ObjectOutputStream(new FileOutputStream(shuffleBlockIds[i].getShufflePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //todo copy 学生实现 将maptask的处理结果写入shuffle文件中
    @Override
    public void write(Stream<KeyValue> entryStream) throws IOException {
        Iterator<KeyValue> iterator = entryStream.iterator();
        while (iterator.hasNext()) {
            KeyValue next = iterator.next();
            System.out.println("写入KV到shuffle:"+next.getKey() + " " + next.getValue());
            // 通过hash来保证同一个key写入同一个shuffle文件中
            fileWriters[Math.abs(next.getKey().hashCode()) % reduceTaskNum].writeObject(next);
        }
    }

    @Override
    public void commit() {
        for (int i = 0; i < reduceTaskNum; i++) {
            try {
                fileWriters[i].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MapStatus getMapStatus(int mapTaskId) {
        return new MapStatus(mapTaskId,shuffleBlockIds);
    }

    public ReduceStatus getReduceStatus(int mapTaskId) {
        return new ReduceStatus(mapTaskId,shuffleBlockIds);
    }

}
