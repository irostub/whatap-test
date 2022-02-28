package com.irostub.filedb;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndexFile {
    private RandomAccessFile file;
    private Map<Long, RecordHeader> index;

    private static final String INDEX_FILE_NAME_PREFIX = "index_";

    //인덱스 파일 헤더 크기
    private static final byte INDEX_FILE_HEADER_SIZE = 16;

    //마지막에 삭제된 레코드의 인덱스 노드 시작 주소(long, 8byte)
    private static final byte LAST_DELETE_RECORD_INDEX = 0;

    //키값 시퀀스 인덱스(long, 8byte)
    private static final byte KEY_SEQUENCE_INDEX = 8;

    //인덱스 키 길이(long, 8byte)
    private static final byte INDEX_KEY_LENGTH = 8;

    //인덱스 헤더의 크기(RecordHeader size)
    private static final byte RECORD_HEADER_LENGTH = 20;

    //인덱스 총 길이
    //(FILE_HEADER_SIZE + INDEX_NODE_LENGTH * x) 는 다음 인덱스 시작 주소를 나타냄
    private static final byte INDEX_NODE_LENGTH = INDEX_KEY_LENGTH + RECORD_HEADER_LENGTH;

    public long generateKey() throws IOException {
        long key = readKeySeq();
        writeKeySeq(readKeySeq() + 1);
        return key;
    }

    public void addIndex(long indexPosition, long key, RecordHeader recordHeader) throws IOException {

        file.seek(indexPosition);
        file.writeLong(key);

        writeHeader(recordHeader);

        index.put(key, recordHeader);
    }

    public void deleteEntryIndex(long targetKey) throws IOException {
        RecordHeader targetRecord = index.get(targetKey);
        long lastDeleteRecordKey = readLastDeleteRecordKey();
        //삭제하려는 인덱스가 처음 인덱스인 경우
        if(targetKey == lastDeleteRecordKey){
            writeLastDeleteRecordKey(targetRecord.getNextDeleteKey());
            return;
        }

        RecordHeader prevRecord = getRecordHeaderFromKey(lastDeleteRecordKey);
        while(prevRecord.getNextDeleteKey() != targetKey){
            prevRecord = getRecordHeaderFromKey(prevRecord.getNextDeleteKey());
        }
        //삭제하려는 인덱스가 처음이 아닌 경우
        prevRecord.setNextDeleteKey(targetRecord.getNextDeleteKey());
        writeHeaderFromIndex(prevRecord.getSelfIndex(),prevRecord);

        //인메모리 동기화
        long prevKey = readKeyFromPosition(prevRecord.getSelfIndex());
        index.replace(prevKey, prevRecord);
        index.remove(targetKey);
    }

    //삭제된 레코드에 대해서 메모리 접근
    public long getWorstFitKey() throws IOException {
        long lastDeleteKey = readLastDeleteRecordKey();
        RecordHeader lastDeleteHeader = getRecordHeaderFromKey(lastDeleteKey);

        int dataSize = lastDeleteHeader.getDataSize();
        long nextDeleteKey = lastDeleteHeader.getNextDeleteKey();
        long maxSizeRecordKey = lastDeleteKey;
        RecordHeader recordHeader = lastDeleteHeader;
        while(nextDeleteKey != -1){
            recordHeader = getRecordHeaderFromKey(recordHeader.getNextDeleteKey());
            if(dataSize < recordHeader.getDataSize()){
                dataSize = recordHeader.getDataSize();
                maxSizeRecordKey = nextDeleteKey;
            }
            nextDeleteKey = recordHeader.getNextDeleteKey();
        }
        return maxSizeRecordKey;
    }

    public RecordHeader getRecordHeaderFromKey(long key){
        return index.get(key);
    }

    public boolean hasDeleteIndex() throws IOException {
        return readLastDeleteRecordKey() != -1L;
    }

    public long getIndexPosition(long position){
        return INDEX_FILE_HEADER_SIZE + (INDEX_NODE_LENGTH * position);
    }

    public long getHeaderFromIndex(long index){
        return getIndexPosition(index) + INDEX_KEY_LENGTH;
    }

    public long readKeyFromPosition(long position) throws IOException {
        file.seek(getIndexPosition(position));
        return file.readLong();
    }

    public static IndexFile initIndexFile(String databasePath, Mode mode, long recordCount){
        File f = new File(INDEX_FILE_NAME_PREFIX + databasePath);

        try{
            if(f.exists()){
                return new IndexFile(f, mode, recordCount);
            }else{
                return new IndexFile(f);
            }
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public long readLastDeleteRecordKey() throws IOException {
        file.seek(LAST_DELETE_RECORD_INDEX);
        return file.readLong();
    }

    public void writeHeaderFromIndex(long index, RecordHeader recordHeader) throws IOException {
        file.seek(getHeaderFromIndex(index));
        writeHeader(recordHeader);
    }
    public void removeIndexInMemoryMap(long key){
        index.remove(key);
    }

    public void updateInMemoryIndex(long key, RecordHeader recordHeader){
        index.replace(key, recordHeader);
    }

    private RecordHeader readRecordHeaderFromPosition(long position) throws IOException {
        file.seek(getIndexPosition(position)+INDEX_KEY_LENGTH);

        long dataIndex = file.readLong();
        int dataSize = file.readInt();
        long nextDeleteIndex = file.readLong();

        return new RecordHeader(dataIndex, dataSize, nextDeleteIndex);
    }

    //첫 로드

    private IndexFile(File f) throws IOException {
        file = new RandomAccessFile(f, Mode.WRITE.getProperty());
        writeLastDeleteRecordKey(-1L);
        writeKeySeq(0);
        initMemoryIndexMap(0);
    }

    //이후 로드

    private IndexFile(File f, Mode mode, long recordCount) throws IOException {
        file = new RandomAccessFile(f, mode.getProperty());
        initMemoryIndexMap(recordCount);
    }

    private void initMemoryIndexMap(long recordCount) throws IOException {
        index = new ConcurrentHashMap<>();
        for(long i = 0; i < recordCount; i++){
            long key = readKeyFromPosition(i);
            RecordHeader recordHeader = readRecordHeaderFromPosition(i);
            recordHeader.setSelfIndex(i);
            index.put(key, recordHeader);
        }
    }

    private void writeHeader(RecordHeader recordHeader) throws IOException {
        file.writeLong(recordHeader.getDataIndex());
        file.writeInt(recordHeader.getDataSize());
        file.writeLong(recordHeader.getNextDeleteKey());
    }

    public void writeLastDeleteRecordKey(long index) throws IOException {
        file.seek(LAST_DELETE_RECORD_INDEX);
        file.writeLong(index);
    }

    private long readKeySeq() throws IOException {
        file.seek(KEY_SEQUENCE_INDEX);
        return file.readLong();
    }

    private void writeKeySeq(long num) throws IOException {
        file.seek(KEY_SEQUENCE_INDEX);
        file.writeLong(num);
    }
}
