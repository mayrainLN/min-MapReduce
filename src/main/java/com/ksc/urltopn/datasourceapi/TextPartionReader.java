package com.ksc.urltopn.datasourceapi;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TextPartionReader implements PartionReader<String>, Serializable {


    @Override
    public Stream<String> toStream(PartionFile partionFile) throws IOException {
        Stream<String> allStream = Stream.empty();
        //todo copy 学生实现 maptask读取原始数据文件的内容

        for (FileSplit fileSplit:partionFile.getFileSplits()){
            Stream<String> stream = Files.lines(Paths.get(fileSplit.getFileName()))
                    .skip(fileSplit.getStart())
                    .limit(fileSplit.getLength());
            allStream = Stream.concat(allStream,stream);
        }
        return allStream;
    }
}
