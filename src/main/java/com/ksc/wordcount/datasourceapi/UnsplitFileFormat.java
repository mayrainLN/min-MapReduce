package com.ksc.wordcount.datasourceapi;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class UnsplitFileFormat implements FileFormat {

        @Override
        public boolean isSplitable(String filePath) {
            return false;
        }


        @Override
        public PartionFile[] getSplits(String filePath, long size) {
            File parentFile = new File(filePath);
            if(parentFile.isFile()){
                return new PartionFile[]{new PartionFile(0,new FileSplit[]{new FileSplit(filePath,0,size)})};
            }
            List<PartionFile> partiongFileList=new ArrayList<>();
            File[] files = parentFile.listFiles();
            int partionId = 0;
            for (File file:files){
                FileSplit[] fileSplits = {new FileSplit(file.getAbsolutePath(),0,file.length())};
                partiongFileList.add(new PartionFile(partionId, fileSplits));
                partionId++;
            }
            //todo copy一个文件只切一个split 学生实现 driver端切分split的逻辑

            return partiongFileList.toArray(new PartionFile[partiongFileList.size()]);
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
