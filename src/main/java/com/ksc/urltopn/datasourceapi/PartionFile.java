package com.ksc.urltopn.datasourceapi;

import java.io.Serializable;

public class PartionFile implements Serializable {
    private int partionId;
    private FileSplit[] fileSplits;

    public PartionFile(int partionId, FileSplit[] fileSplits) {
        this.partionId = partionId;
        this.fileSplits = fileSplits;
    }

    public int getPartionId() {
        return partionId;
    }

    public FileSplit[] getFileSplits() {
        return fileSplits;
    }

    @Override
    public String toString() {
        return "PartionFile{" +
                "partionId=" + partionId +
                ", fileSplits=" + fileSplits +
                '}';
    }

}
