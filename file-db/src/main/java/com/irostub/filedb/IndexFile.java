package com.irostub.filedb;

import com.irostub.filedb.exception.IndexFileException;
import com.irostub.filedb.exception.IndexFileInitializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class IndexFile {
    private RandomAccessFile file;
    private Map<Long, RecordHeader> indexEntry;


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

    public static IndexFile initIndexFile(String databasePath, Mode mode, long recordCount) {
        File f = new File(INDEX_FILE_NAME_PREFIX + databasePath);
        if (f.exists()) {
            return new IndexFile(f, mode, recordCount);
        } else {
            return new IndexFile(f);
        }
    }

    private IndexFile(File f){
        initializeIndexDatabase(f, Mode.WRITE);
        writeLastDeletedKey(-99L);
        writeSequence(0);
        initMemoryIndexMap(0);
    }

    private IndexFile(File f, Mode mode, long recordCount){
        initializeIndexDatabase(f, mode);
        initMemoryIndexMap(recordCount);
    }

    private void initializeIndexDatabase(File f, Mode mode){
        try{
            this.file = new RandomAccessFile(f, mode.getProperty());
        }catch (FileNotFoundException e) {
            log.error("인덱스 데이터베이스 파일을 찾을 수 없습니다.");
            throw new IndexFileInitializeException(e);
        }
        log.info("index database is ready");
    }

    private void initMemoryIndexMap(long recordCount){
        indexEntry = new ConcurrentHashMap<>();
        for(long i = 0; i < recordCount; i++){
            long key = readKey(getKeyFp(i));
            RecordHeader recordHeader = readRecordHeader(getRecordHeaderFp(i));
            recordHeader.setSelfPosition(i);

            this.indexEntry.put(key, recordHeader);
        }
    }

    public long getEofFp(){
        try {
            return file.length();
        }catch(IOException e) {
            log.error("cannot read file length");
            throw new IndexFileException(e);
        }
    }

    public void updateInMemoryIndex(long key, RecordHeader recordHeader){
        indexEntry.replace(key, recordHeader);
    }

    //TODO : 해당 메서드를 삭제하고 writeRecordHeader() 로 통합
    public void writeHeaderFromIndex(long position, RecordHeader recordHeader){
        long filePointer = getRecordHeaderFp(position);
        writeRecordHeader(filePointer, recordHeader);
    }

    public long generateKey(){
        long key = readSequence();
        writeSequence(readSequence() + 1);
        return key;
    }

    public void addIndexNode(long index, long key, RecordHeader recordHeader){

        long recordHeaderIndex = writeKey(index, key);
        writeRecordHeader(recordHeaderIndex, recordHeader);

        indexEntry.put(key, recordHeader);
    }

    /*
    인덱스가 실제로 지워지는 때
    1. 기존에 삭제된 인덱스 노드가 있다.
    2. 새로 추가하고자 하는 인덱스 노드가 들어온다.
    3. 기존 삭제된 인덱스 노드 중 worst case 를 찾고
    3. 새로 추가하고자 하는 인덱스 노드의 레코드 데이터 사이즈와 기존에 삭제된 인덱스 노드의 레코드 데이터 사이즈가 같다면
    4. 삭제 처리되었던 인덱스 노드를 실제로 파일에서 삭제하고 새로 삽입하고자 하는 인덱스 노드로 교체한다.
    4과정에서
     */
    public void deleteIndexNode(long key){
        //삭제하려는 인덱스 노드
        RecordHeader targetRecord = indexEntry.get(key);

        //기존에 삭제했던 인덱스 노드의 링크드리스트 시작 키
        long lastDeleteRecordKey = readLastDeletedKey();

        //링크드리스트 삭제 과정 시작

        //삭제하려는 인덱스가 처음 인덱스인 경우
        if(key == lastDeleteRecordKey){
            writeLastDeletedKey(targetRecord.getNextDeleteKey());
            return;
        }

        //삭제되었던 노드 링크드리스트를 삭제하고자하는 노드의 이전 노드를 찾을 때까지 순회
        RecordHeader prevRecord = getRecordHeaderFromKey(lastDeleteRecordKey);
        while(prevRecord.getNextDeleteKey() != key){
            prevRecord = getRecordHeaderFromKey(prevRecord.getNextDeleteKey());
        }

        //삭제하려는 인덱스가 처음이 아닌 경우
        //삭제하고자 하는 노드의 이전 노드가 삭제하려는 노드의 다음 노드 포인터를 가르키도록 설정
        prevRecord.setNextDeleteKey(targetRecord.getNextDeleteKey());

        //이전 노드에 대해 변경된 헤더를 실제로 파일에 쓰기
        long recordHeaderFp = getRecordHeaderFp(prevRecord.getSelfPosition());
        writeRecordHeader(recordHeaderFp, prevRecord);

        //인메모리맵을 동기화
        long prevKey = readKey(prevRecord.getSelfPosition());
        indexEntry.replace(prevKey, prevRecord);
        indexEntry.remove(key);
    }

    public long searchWorstFitKey(){
        long lastDeleteKey = readLastDeletedKey();
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

    public List<RecordHeader> getRecordHeaders(){
        return new ArrayList<>(indexEntry.values());
    }

    public RecordHeader getRecordHeaderFromKey(Long key){
        return indexEntry.get(key);
    }

    public List<RecordHeader> getRecordHeadersFromKeys(Long ... keys){
        return Arrays.stream(keys)
                .sorted(Comparator.naturalOrder())
                .map(this::getRecordHeaderFromKey)
                .collect(Collectors.toList());
    }

    public long getRecordHeaderFp(long position){
        return getKeyFp(position) + INDEX_KEY_LENGTH;
    }

    public long getKeyFp(long position){
        return INDEX_FILE_HEADER_SIZE + (INDEX_NODE_LENGTH * position);
    }

    public long readKey(long index){
        try {
            file.seek(index);
            return file.readLong();
        }catch (IOException e){
            log.error("cannot read key");
            throw new IndexFileException(e);
        }
    }

    public long writeKey(long index, long key){
        try{
            file.seek(index);
            file.writeLong(key);
            return file.getFilePointer();
        }catch(IOException e){
            log.error("cannot write key");
            throw new IndexFileException(e);
        }
    }

    public boolean hasLastDeletedKey(){
        return readLastDeletedKey() != -99L;
    }

    public long readLastDeletedKey(){
        try {
            file.seek(LAST_DELETE_RECORD_INDEX);
            return file.readLong();
        } catch (IOException e) {
            log.error("cannot read last deleted key");
            throw new IndexFileException(e);
        }
    }

    public void writeLastDeletedKey(long index){
        try {
            file.seek(LAST_DELETE_RECORD_INDEX);
            file.writeLong(index);
        }catch (IOException e){
            log.error("cannot write last deleted key");
            throw new IndexFileException(e);
        }
    }

    public void removeIndexInMemoryMap(long key){
        indexEntry.remove(key);
    }

    private RecordHeader readRecordHeader(long index){
        try {
            file.seek(index);
            long dataIndex = file.readLong();
            int dataSize = file.readInt();
            long nextDeleteIndex = file.readLong();
            return new RecordHeader(dataIndex, dataSize, nextDeleteIndex);
        }catch(IOException e){
            log.error("cannot read record header");
            throw new IndexFileException(e);
        }
    }

    public void writeRecordHeader(long index, RecordHeader recordHeader){
        try {
            file.seek(index);
            file.writeLong(recordHeader.getDataIndex());
            file.writeInt(recordHeader.getDataSize());
            file.writeLong(recordHeader.getNextDeleteKey());
        } catch (IOException e) {
            log.error("cannot write record header");
            throw new IndexFileException(e);
        }
    }

    private long readSequence(){
        try {
            file.seek(KEY_SEQUENCE_INDEX);
            return file.readLong();
        }catch(IOException e){
            log.error("cannot read sequence");
            throw new IndexFileException(e);
        }
    }

    private void writeSequence(long num){
        try {
            file.seek(KEY_SEQUENCE_INDEX);
            file.writeLong(num);
        }catch (IOException e){
            log.error("cannot write sequence");
            throw new IndexFileException(e);
        }
    }
}
