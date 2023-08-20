package com.ksc.urltopn.datasourceapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SplitFileFormat implements FileFormat {

    @Override
    public boolean isSplitable(String filePath) {
        return true;
    }


    /**
     * 文件切分，返回文件分区数组
     * 关于切片的具体考虑，见下方splitFile函数的doc栏
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
     * 为了反正url被截断，考虑按照文本内容切分：
     * 可以用split，比如按照空格或者"去分割
     * 重大缺点：会改变文件的内容(没有分隔符)。
     * 我们的分片应当是普遍适用的，可以服务于各种计算任务，不能因为这样切割可以满足TopN的需求就这样搞。
     *
     * 如果要保留源文件的所有内容，只考虑这样做：每次读取一个字符，直到当前读取的字符超过了1M，就开始准备切片。
     * 为了防止切断完整的url，要求在读取到冒号或者空格或者逗号时，才能将当前所读的所有字符生成一个文件切片
     * 否则，继续向后读取，即使大小超过了1M。这样可以防止url被截断
     * 缺点：一个个读再判断，实在是太慢了。
     *
     * 考虑到在答疑文档总老师补充的信息：同一个url不会分布在上下两行。
     * 所以其实我们可以利用这个信息，每次读取一行即可。超过1M就生成切片
     * @param file 文件
     * @param size 切片大小上界
     * @return 切片数组
     */
    private FileSplit[] splitFile(File file, long size) {
        size = 100;
        List<FileSplit> fileSplitList = new ArrayList<>();
        AtomicInteger FileLineNum = new AtomicInteger(0);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            long currStart = 0L;
            long currSplitByteSize = 0L;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // 当前行size
                long currLineByteSize = line.getBytes("utf-8").length;
                if ( currSplitByteSize + currLineByteSize >= size) {
                    // 新建分片
                    fileSplitList.add(new FileSplit(file.getAbsolutePath(), currStart, currSplitByteSize));
                    // 重新规定下一个分片的其实位置
                    currStart += sb.toString().getBytes("utf-8").length;
                    currSplitByteSize = 0L;
                    sb = new StringBuilder();
                } else {
                    // 在当前分片追加
                    sb.append(line);
                    currSplitByteSize += currLineByteSize;
                }
            }
            if(sb.length() > 0){
                // 兜底最后一个分片
                fileSplitList.add(new FileSplit(file.getAbsolutePath(), currStart, currSplitByteSize));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取失败");
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
