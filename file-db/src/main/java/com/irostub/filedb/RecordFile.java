package com.irostub.filedb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RecordFile {
    private RandomAccessFile file;

    //파일의 헤더 총 크기
    private static final int FILE_HEADER_SIZE = 8;

    //현재 총 레코드 개수를 표시 주소 (값, 8byte)
    private static final byte RECORD_COUNT_HEADER_INDEX = 0;

    public static RecordFile initRecordFile(String databasePath, Mode mode){
        File f = new File(databasePath);
        try{
            if(f.exists()){
                return new RecordFile(f, mode);
            }else{
                return new RecordFile(f);
            }
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public void writeData(long position, String data) throws IOException {
        file.seek(position);
        file.writeUTF(data);
    }

    public long getRecordDataStartIndex(){
        return FILE_HEADER_SIZE;
    }

    public long readRecordCount() throws IOException {
        file.seek(RECORD_COUNT_HEADER_INDEX);
        return file.readLong();
    }

    public String readRecordDataFromIndex(long index) throws IOException {
        file.seek(index);
        return file.readUTF();
    }
    public void increaseRecordCount(long count) throws IOException {
        writeRecordCount(readRecordCount() + count);
    }

    public long getEofPointer() throws IOException {
        return file.length();
    }

    //첫 로드
    private RecordFile(File f) throws IOException {
        file = new RandomAccessFile(f, Mode.WRITE.getProperty());
        writeRecordCount(0L);
    }

    //이후 로드
    private RecordFile(File f, Mode mode) throws IOException {
        file = new RandomAccessFile(f, mode.getProperty());
    }

    private void writeRecordCount(long num) throws IOException {
        file.seek(RECORD_COUNT_HEADER_INDEX);
        file.writeLong(num);
    }
}
