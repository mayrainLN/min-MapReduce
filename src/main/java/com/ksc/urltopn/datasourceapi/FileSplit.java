package com.ksc.urltopn.datasourceapi;

import java.io.Serializable;

public class FileSplit implements Serializable {
    private String fileName;
    private long start;
    private long length;

    public FileSplit(String fileName, long start, long length) {
        this.fileName = fileName;
        this.start = start;
        this.length = length;
    }

    public String getFileName() {
        return fileName;
    }

    public long getStart() {
        return start;
    }

    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "FileSplit{" +
                "fileName='" + fileName + '\'' +
                ", start=" + start +
                ", length=" + length +
                '}';
    }
}
