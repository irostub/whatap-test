package com.irostub.filedb;

//24byte
public class RecordHeader {
    //최대 8192 페비바이트
    //8byte
    private long dataIndex;

    //4byte
    private int dataSize;

    //8byte
    private long nextDeleteKey = -1;

    private long selfIndex;

    public RecordHeader(long dataIndex, int dataSize, long nextDeleteKey) {
        this.dataIndex = dataIndex;
        this.dataSize = dataSize;
        this.nextDeleteKey = nextDeleteKey;
    }

    public RecordHeader(long dataIndex, int dataSize) {
        this.dataIndex = dataIndex;
        this.dataSize = dataSize;
    }

    public void setNextDeleteKey(long index){
        this.nextDeleteKey = index;
    }

    public void setSelfIndex(long selfIndex) {
        this.selfIndex = selfIndex;
    }

    public long getSelfIndex() {
        return selfIndex;
    }

    public void setDataIndex(long dataIndex) {
        this.dataIndex = dataIndex;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public long getDataIndex() {
        return dataIndex;
    }

    public int getDataSize() {
        return dataSize;
    }

    public long getNextDeleteKey() {
        return nextDeleteKey;
    }
}
