package com.irostub.filedb;

import com.irostub.filedb.exception.RecordFileException;
import com.irostub.filedb.exception.RecordFileInitializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

@Slf4j
public class RecordFile {
    private RandomAccessFile file;

    //파일의 헤더 총 크기
    private static final int FILE_HEADER_SIZE = 8;

    //현재 총 레코드 개수를 표시 주소 (값, 8byte)
    private static final byte RECORD_COUNT_HEADER_INDEX = 0;

    public static RecordFile createRecordFile(String databasePath, Mode mode) {
        File f = new File(databasePath);
        if (f.exists()) {
            return new RecordFile(f, mode);
        } else {
            return new RecordFile(f);
        }
    }

    private RecordFile(File f) {
        initializeRecordDatabase(f, Mode.WRITE);
        writeRecordCount(0L);
    }

    private RecordFile(File f, Mode mode) {
        initializeRecordDatabase(f, mode);
    }

    private void initializeRecordDatabase(File f, Mode mode) {
        try{
            this.file = new RandomAccessFile(f, mode.getProperty());
        } catch (FileNotFoundException e) {
            log.error("레코드 데이터베이스 파일을 찾을 수 없습니다.");
            throw new RecordFileInitializeException(e);
        }
        log.info("record database is ready");
    }

    public long getEofFp() {
        try {
            return file.length();
        } catch (IOException e) {
            log.error("cannot read file length");
            throw new RecordFileException(e);
        }
    }

    public void writeData(long index, String data){
        try {
            file.seek(index);
            file.writeUTF(data);
        } catch (IOException e) {
            log.error("cannot write record data");
            throw new RecordFileException(e);
        }
    }

    public String readData(long index){
        try {
            file.seek(index);
            return file.readUTF();
        } catch (IOException e) {
            log.error("cannot read record data");
            throw new RecordFileException(e);
        }
    }

    public long readRecordCount() {
        try {
            file.seek(RECORD_COUNT_HEADER_INDEX);
            return file.readLong();
        } catch (IOException e) {
            log.error("cannot read record count");
            throw new RecordFileException(e);
        }
    }

    private void writeRecordCount(long num) {
        try {
            file.seek(RECORD_COUNT_HEADER_INDEX);
            file.writeLong(num);
        } catch (IOException e) {
            log.error("cannot write record count");
            throw new RecordFileException(e);
        }
    }

    public void increaseRecordCount(long count) {
        writeRecordCount(readRecordCount() + count);
    }
}
