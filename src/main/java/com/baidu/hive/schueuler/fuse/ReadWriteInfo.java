package com.baidu.hive.schueuler.fuse;

public class ReadWriteInfo {

    private boolean hasWriter;
    private int readCount;
    // if readFinishedCount == readCount, drop from cache.
    private int readFinishedCount;
}
