package com.ksc.wordcount.datasourceapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class UnsplitFileFormat implements FileFormat {

    @Override
    public boolean isSplitable(String filePath) {
        return true;
    }


    /**
     * 文件切分，返回文件分区数组
     *
     * @param filePath
     * @param size
     * @return
     */
    @Override
    public PartionFile[] getSplits(String filePath, long size) {
        //todo copy一个文件只切一个split 学生实现 driver端切分split的逻辑
        File parentFile = new File(filePath);
        if (parentFile.isFile()) {
            return new PartionFile[]{new PartionFile(0, splitFile(parentFile, size))};
        }
        List<PartionFile> partiongFileList = new ArrayList<>();
        File[] files = parentFile.listFiles();
        int partionId = 0;
        for (File file : files) {
            FileSplit[] fileSplits = splitFile(file, size);
            partiongFileList.add(new PartionFile(partionId, fileSplits));
            partionId++;
        }

        return partiongFileList.toArray(new PartionFile[partiongFileList.size()]);
    }

    /**
     * 关于切片的考虑，详见Readme
     * @param file
     * @param size
     * @return
     */
    private FileSplit[] splitFile(File file, long size) {
        List<FileSplit> fileSplitList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            long currStart = 0L;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                int currLineByteLength = line.getBytes("utf-8").length;
                if (sb.length() + currLineByteLength > size) {
                    int SplitLength = sb.toString().getBytes("utf-8").length;
                    // 新建分片
                    fileSplitList.add(new FileSplit(file.getAbsolutePath(), currStart, SplitLength));
                    currStart += SplitLength;
                    sb = new StringBuilder();
                } else {
                    // 在当前分片追加
                    sb.append(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileSplitList.toArray(new FileSplit[fileSplitList.size()]);
    }

    @Override
    public PartionReader createReader() {
        return new TextPartionReader();
    }

    @Override
    public PartionWriter createWriter(String destPath, int partionId) {
        return new TextPartionWriter(destPath, partionId);
    }


}
