package com.ksc.urltopn.datasourceapi;

import com.ksc.urltopn.conf.AppConfig;
import com.ksc.urltopn.task.KeyValue;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.UUID;
import java.util.stream.Stream;

public class TextPartionWriter implements PartionWriter<KeyValue>, Serializable {

    private String destDest;
    private int partionId;

    public TextPartionWriter(String destDest, int partionId) {
        this.destDest = destDest;
        this.partionId = partionId;
    }

    //把partionId 前面补0，补成length位
    public String padLeft(int partionId, int length) {
        String partionIdStr = String.valueOf(partionId);
        int len = partionIdStr.length();
        if (len < length) {
            for (int i = 0; i < length - len; i++) {
                partionIdStr = "0" + partionIdStr;
            }
        }
        return partionIdStr;
    }

    //todo finish 学生实现 将reducetask的计算结果写入结果文件中
    @Override
    public void writeReduce(Stream<KeyValue> stream, String applicationId) throws IOException {
        File file = new File(destDest + File.separator + "part_" + padLeft(partionId, 3) + ".txt");

        // 写到中间文件
        String midFileId = UUID.randomUUID().toString();
        File midFile = new File(AppConfig.shuffleTempDir + "/" + applicationId + "/midFile/shuffle_" + midFileId + ".data");
        if (!midFile.getParentFile().exists()) {
            FileUtils.forceMkdir(midFile.getParentFile());
        }
        FileOutputStream midFileOs = new FileOutputStream(midFile);

        if(!file.getParentFile().exists()){
            FileUtils.forceMkdir(file.getParentFile());
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            stream.forEach(keyValue -> {
                try {
                    System.out.println("reduce时写："+keyValue.getKey() + ":" + keyValue.getValue());
                    fos.write((keyValue.getKey() + "\t" + keyValue.getValue() + "\n").getBytes("utf-8"));
                    midFileOs.write((keyValue.getKey() + "\t" + keyValue.getValue() + "\n").getBytes("utf-8"));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        midFileOs.close();
    }

    //todo finish 学生实现 将reducetask的计算结果写入结果文件中
    @Override
    public void writeMerge(Stream<KeyValue> stream, String applicationId) throws IOException {
        File file = new File(destDest + File.separator + "result_" + padLeft(partionId, 3) + ".txt");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            stream.forEach(keyValue -> {
                try {
                    fos.write((keyValue.getKey() + "\t" + keyValue.getValue() + "\n").getBytes("utf-8"));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
