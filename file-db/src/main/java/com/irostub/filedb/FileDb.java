package com.irostub.filedb;

import com.irostub.filedb.annotation.Id;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FileDb<T> {
    private final RecordFile recordFile;
    private final IndexFile indexFile;
    private final Class<T> genericClazz;

    private static final String RECORD_DATA_SEPARATOR = "|";

    public FileDb(String databasePath, Mode mode, Class<T> genericClazz) throws IOException {
        this.recordFile = RecordFile.initRecordFile(databasePath, mode);
        long recordCount = recordFile.readRecordCount();
        this.indexFile = IndexFile.initIndexFile(databasePath, mode, recordCount);
        this.genericClazz = genericClazz;
    }

    public long insertRecord(T data) throws IOException {
        //키값 생성
        long key = indexFile.generateKey();

        //직렬화 데이터
        String serializeData = getSerializeData(data, key);
        int realSize = serializeData.length() + 2;

        //인덱스 파일의 마지막 자리
        long recordCount = recordFile.readRecordCount();
        long indexPosition = indexFile.getIndexPosition(recordCount);

        if (indexFile.hasDeleteIndex()) {
            //남은 공간이 가장 큰 삭제 인덱스 찾기
            //마지막 삭제 인덱스(순번)
            long worstFitHeaderKey = indexFile.getWorstFitKey();
            RecordHeader worstFitHeader = indexFile.getRecordHeaderFromKey(worstFitHeaderKey);

            if(worstFitHeader.getDataSize() > realSize){

                //공간이 클 경우
                //공간을 분할
                //가장 뒤에 인덱스 삽입
                //레코드 카운트 증가
                //레코드를 분할된 공간 위치에 삽입
                //삭제 처리된 인덱스의 인메모리 업데이트와 파일에서의 write
                int gap = worstFitHeader.getDataSize() - realSize;
                RecordHeader recordHeader = new RecordHeader(
                        worstFitHeader.getDataIndex(),
                        realSize
                );
                recordHeader.setSelfIndex(recordCount);

                worstFitHeader.setDataSize(gap);
                worstFitHeader.setDataIndex(
                        worstFitHeader.getDataIndex() + realSize);

                indexFile.addIndex(indexPosition, key, recordHeader);
                indexFile.updateInMemoryIndex(worstFitHeaderKey, worstFitHeader);
                indexFile.writeHeaderFromIndex(worstFitHeader.getSelfIndex(), worstFitHeader);

                recordFile.increaseRecordCount(1);

                recordFile.writeData(recordHeader.getDataIndex(), serializeData);

            }else if(worstFitHeader.getDataSize() == realSize){
                //공간이 일치할 경우
                //인덱스를 갈아끼우기
                long selfIndex = worstFitHeader.getSelfIndex();
                RecordHeader recordHeader = new RecordHeader(
                        worstFitHeader.getDataIndex(),
                        realSize
                );
                recordHeader.setSelfIndex(selfIndex);

                indexFile.deleteEntryIndex(worstFitHeaderKey);

                long filePointer = indexFile.getIndexPosition(selfIndex);
                indexFile.addIndex(filePointer, key, recordHeader);


                //레코드 데이터 카운트 유지
                //레코드 데이터를 해당 인덱스의 데이터 부분에 교체
                long recordFp = worstFitHeader.getDataIndex();
                recordFile.writeData(recordFp, serializeData);

                //인메모리에서 기존 인덱스 제거
                indexFile.removeIndexInMemoryMap(worstFitHeaderKey);
            }
        }else {
            //파일 데이터의 마지막 주소
            long eofPointer = recordFile.getEofPointer();

            //레코드 데이터 카운트 증가
            recordFile.increaseRecordCount(1);

            //새 레코드
            RecordHeader recordHeader = new RecordHeader(
                    eofPointer,
                    realSize
            );

            //인덱스 자신의 순서상 위치 설정
            recordHeader.setSelfIndex(recordCount);

            //가장 뒤에 인덱스 삽입
            indexFile.addIndex(indexPosition, key, recordHeader);

            //가장 뒤에 레코드 삽입
            recordFile.writeData(recordHeader.getDataIndex(), serializeData);
        }
        return key;
    }

    public void deleteRecord(long key) throws IOException {
        RecordHeader recordHeader = indexFile.getRecordHeaderFromKey(key);

        //기존에 삭제한 레코드가 있을 때
        if(indexFile.hasDeleteIndex()){
            //인덱스 파일의 마지막 삭제 레코드 주소에 해당 인덱스의 주소 삽입
            long lastDeleteKey = indexFile.readLastDeleteRecordKey();
            indexFile.writeLastDeleteRecordKey(key);

            //삭제할 레코드의 다음 삭제 레코드 키에 기존 삭제 키 삽입
            recordHeader.setNextDeleteKey(lastDeleteKey);
            indexFile.writeHeaderFromIndex(recordHeader.getSelfIndex(), recordHeader);
        }else{
            //기존에 삭제한 레코드가 없을 때
            //인덱스 파일의 마지막 삭제 레코드 주소에 해당 인덱스의 주소 삽입
            indexFile.writeLastDeleteRecordKey(key);
        }
    }

    public List<T> findRecords(long ... key){
        return null;
    }

    public void updateRecord(long key, T t){
        //업데이트 할 파일의 인덱스 가져오기
        //업데이트 할 인덱스의 현재 크기를 가져오기

        //업데이트 하고싶은 크기가 더 큰 경우

        //업데이트 하고싶은 크기가 더 작은 경우

        //업데이트 하고싶은 크기가 같은 경우
    }

    public T findRecord(long key) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        RecordHeader recordHeaderFromKey = indexFile.getRecordHeaderFromKey(key);
        long dataIndex = recordHeaderFromKey.getDataIndex();
        String[] split = recordFile.readRecordDataFromIndex(dataIndex).split("["+RECORD_DATA_SEPARATOR+"]");

        return parseToGenericTypeObject(split);
    }

    private T parseToGenericTypeObject(String[] split) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        T returnObject = genericClazz.newInstance();
        List<Field> fields = getFieldsInClazz(returnObject);
        for (int i = 0; i < split.length; i++) {
            fields.get(i).setAccessible(true);
            if(split[i] == null || split[i].isEmpty() || split[i].equals("null")){
                fields.get(i).set(returnObject, null);
            }

            Object value = split[i];

            if(!fields.get(i).getType().equals(String.class)){
             value = fields.get(i)
                    .getType()
                    .getMethod("valueOf", String.class)
                    .invoke(null, split[i]);
            }
            fields.get(i).set(returnObject, value);
        }
        return returnObject;
    }

    //declaredMethods() 순서를 보장하지 않으므로 오름차순 확정 순서로 정렬
    private List<Method> getDeclaredMethodsSortByName(Class<?> clazz){
        List<Method> methods = Arrays.asList(clazz.getDeclaredMethods());
        methods.sort(Comparator.comparing(Method::getName));
        return methods;
    }

    //T 클래스 직렬화, 구분자 사용
    private String getSerializeData(T data, long key) {
        List<Method> declaredMethods = getDeclaredMethodsSortByName(data.getClass());

        Field[] declaredFields = data.getClass().getDeclaredFields();
        List<String> fieldsNamesInClazz = getFieldsNamesInClazz(declaredFields);

        List<String> datas = new ArrayList<>();

        //키 삽입
        for(Field field : declaredFields){
            if(field.getDeclaredAnnotation(Id.class) != null){
                field.setAccessible(true);
                try{
                    field.set(data, key);
                }catch(IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }

        //@Field 가 달린 필드이며 프로퍼티명과 일치하는 필드를 직렬화
        for (Method declaredMethod : declaredMethods) {
            if(isGetProperty(declaredMethod)
                    && isFieldProperty(fieldsNamesInClazz, declaredMethod)) {
                try {
                    Class<?> returnType = declaredMethod.getReturnType();
                    Object invoke = declaredMethod.invoke(data);

                    if(invoke == null){
                        datas.add(null);
                    }else{
                        datas.add(returnType.cast(invoke).toString());
                    }
                }catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return String.join(RECORD_DATA_SEPARATOR, datas);
    }

    private boolean isGetProperty(Method declaredMethod) {
        final String propertyRegex = "get[0-9A-Z].*";
        return declaredMethod.getName().matches(propertyRegex);
    }

    private boolean isFieldProperty(List<String> fieldsNamesInClazz, Method declaredMethod) {
        return fieldsNamesInClazz.contains(declaredMethod
                .getName()
                .replace("get", "")
                .toLowerCase());
    }

    private List<Field> getFieldsInClazz(T t) {
        return Arrays.stream(t.getClass().getDeclaredFields())
                .filter(field -> field.getDeclaredAnnotation(com.irostub.filedb.annotation.Field.class) != null)
                .sorted(Comparator.comparing(Field::getName))
                .collect(Collectors.toList());
    }

    private List<String> getFieldsNamesInClazz(Field[] fields) {
        return Arrays.stream(fields)
                .filter(field -> field.getDeclaredAnnotation(com.irostub.filedb.annotation.Field.class) != null)
                .map(Field::getName)
                .collect(Collectors.toList());
    }
}
