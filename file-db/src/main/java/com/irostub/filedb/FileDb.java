package com.irostub.filedb;

import com.irostub.filedb.annotation.Id;
import com.irostub.filedb.exception.IndexFileException;
import com.irostub.filedb.exception.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileDb<T> {
    private final RecordFile recordFile;
    private final IndexFile indexFile;
    private final Class<T> genericClazz;

    /*명명 규칙
     * 레코드 파일 : 레코드 데이터베이스
     * 인덱스 파일 : 인덱스 데이터베이스
     * 실제 파일 안에서의 위치 정보 : fp(file pointer), index
     * 논리적 위치 정보(몇번째에 있는가?) : pos(position)
     * 레코드 파일 : rf(RecordFile)
     * 레코드 수 : recordCount
     * 인덱스 파일 : if(IndexFile)
     * 인덱스의 키 : key
     * 인덱스의 값 : recordHeader
     * 인덱스를 가르킬 때 : indexNode
     * 인메모리 인덱스 : indexEntry
     *
     * 파일에 직접 접근하는 메서드 : read, write
     * 참조로 접근하는 메서드 : get, set
     * */

    private static final String RECORD_DATA_SEPARATOR = "|";

    public FileDb(String databasePath, Mode mode, Class<T> genericClazz) {
        this.recordFile = RecordFile.createRecordFile(databasePath, mode);
        long recordCount = recordFile.readRecordCount();
        this.indexFile = IndexFile.initIndexFile(databasePath, mode, recordCount);
        this.genericClazz = genericClazz;
    }

    synchronized public long insertRecord(T data) {
        //키값 생성
        long key = indexFile.generateKey();

        //직렬화 데이터
        String serializeData = getSerializeData(data, key);
        int realSize = serializeData.length() + 2;

        //인덱스 파일의 마지막 자리
        long recordCount = recordFile.readRecordCount();
        long indexPosition = indexFile.getKeyFp(recordCount);

        if (indexFile.hasLastDeletedKey()) {
            //남은 공간이 가장 큰 삭제 인덱스 찾기
            //마지막 삭제 인덱스(순번)
            long worstFitHeaderKey = indexFile.searchWorstFitKey();
            RecordHeader worstFitHeader = indexFile.getRecordHeaderFromKey(worstFitHeaderKey);

            if (worstFitHeader.getDataSize() > realSize) {

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
                recordHeader.setSelfPosition(recordCount);

                worstFitHeader.setDataSize(gap);
                worstFitHeader.setDataIndex(
                        worstFitHeader.getDataIndex() + realSize);

                indexFile.addIndexNode(indexPosition, key, recordHeader);
                indexFile.updateInMemoryIndex(worstFitHeaderKey, worstFitHeader);
                indexFile.writeHeaderFromIndex(worstFitHeader.getSelfPosition(), worstFitHeader);

                recordFile.increaseRecordCount(1);

                recordFile.writeData(recordHeader.getDataIndex(), serializeData);

            } else if (worstFitHeader.getDataSize() == realSize) {
                //공간이 일치할 경우
                //인덱스를 갈아끼우기
                long selfIndex = worstFitHeader.getSelfPosition();
                RecordHeader recordHeader = new RecordHeader(
                        worstFitHeader.getDataIndex(),
                        realSize
                );
                recordHeader.setSelfPosition(selfIndex);

                indexFile.deleteIndexNode(worstFitHeaderKey);

                long filePointer = indexFile.getKeyFp(selfIndex);
                indexFile.addIndexNode(filePointer, key, recordHeader);


                //레코드 데이터 카운트 유지
                //레코드 데이터를 해당 인덱스의 데이터 부분에 교체
                long recordFp = worstFitHeader.getDataIndex();
                recordFile.writeData(recordFp, serializeData);

                //인메모리에서 기존 인덱스 제거
                indexFile.removeIndexInMemoryMap(worstFitHeaderKey);
            }else{
                //파일 데이터의 마지막 주소
                long eofPointer = recordFile.getEofFp();

                //레코드 데이터 카운트 증가
                recordFile.increaseRecordCount(1);

                //새 레코드
                RecordHeader recordHeader = new RecordHeader(
                        eofPointer,
                        realSize
                );

                //인덱스 자신의 순서상 위치 설정
                recordHeader.setSelfPosition(recordCount);

                //가장 뒤에 인덱스 삽입
                indexFile.addIndexNode(indexPosition, key, recordHeader);

                //가장 뒤에 레코드 삽입
                recordFile.writeData(recordHeader.getDataIndex(), serializeData);
            }
        } else {
            //파일 데이터의 마지막 주소
            long eofPointer = recordFile.getEofFp();

            //레코드 데이터 카운트 증가
            recordFile.increaseRecordCount(1);

            //새 레코드
            RecordHeader recordHeader = new RecordHeader(
                    eofPointer,
                    realSize
            );

            //인덱스 자신의 순서상 위치 설정
            recordHeader.setSelfPosition(recordCount);

            //가장 뒤에 인덱스 삽입
            indexFile.addIndexNode(indexPosition, key, recordHeader);

            //가장 뒤에 레코드 삽입
            recordFile.writeData(recordHeader.getDataIndex(), serializeData);
        }
        return key;
    }

    synchronized public void deleteRecord(long key) {
        RecordHeader recordHeader = indexFile.getRecordHeaderFromKey(key);

        //기존에 삭제한 레코드가 있을 때
        if (indexFile.hasLastDeletedKey()) {
            //인덱스 파일의 마지막 삭제 레코드 주소에 해당 인덱스의 주소 삽입
            long lastDeleteKey = indexFile.readLastDeletedKey();
            indexFile.writeLastDeletedKey(key);

            //삭제할 레코드의 다음 삭제 레코드 키에 기존 삭제 키 삽입
            recordHeader.setNextDeleteKey(lastDeleteKey);
            indexFile.writeHeaderFromIndex(recordHeader.getSelfPosition(), recordHeader);
        } else {
            //기존에 삭제한 레코드가 없을 때
            //인덱스 파일의 마지막 삭제 레코드 주소에 해당 인덱스의 주소 삽입
            indexFile.writeLastDeletedKey(key);
            recordHeader.setNextDeleteKey(-1L);

            long index = indexFile.getRecordHeaderFp(recordHeader.getSelfPosition());
            indexFile.writeRecordHeader(index, recordHeader);
        }
    }

    synchronized public long updateRecord(long key, T data) {
        //업데이트 할 파일의 인덱스 가져오기
        RecordHeader originRecordHeader = indexFile.getRecordHeaderFromKey(key);
        long overrideIndex = originRecordHeader.getDataIndex();
        long overrideSelfPosition = originRecordHeader.getSelfPosition();
        String targetData = getSerializeData(data, key);
        int realSize = targetData.length() + 2;

        if (originRecordHeader.getDataSize() > realSize) {
            //인덱스의 경우는 기존 헤더를 갈이치우는 것으로 해결 가능
            //삭제된 공간을 가르키는 인덱스는 어떻게 존재해야하는가?
            //삭제된 공간을 가르키는 인덱스를 새로 만들어서 추가(이는 키를 새로 발급하는 것과도 같다) 이부분은 문제임.
            //만약 이걸 회피하고 싶다면,

            int gap = originRecordHeader.getDataSize() - realSize;

            //분할 공간 인덱스 추가 & 키 갱신
            originRecordHeader.setDataIndex(originRecordHeader.getDataIndex() + realSize);
            originRecordHeader.setDataSize(gap);
            originRecordHeader.setSelfPosition(recordFile.readRecordCount());
            long newKey = indexFile.generateKey();

            indexFile.addIndexNode(indexFile.getEofFp(),
                    newKey,
                    originRecordHeader);
            deleteRecord(newKey);
            indexFile.writeLastDeletedKey(newKey);

            //새로운 인덱스 수정
            RecordHeader newRecordHeader = new RecordHeader(
                    overrideIndex,
                    realSize
            );
            newRecordHeader.setSelfPosition(overrideSelfPosition);
            indexFile.writeRecordHeader(
                    indexFile.getRecordHeaderFp(newRecordHeader.getSelfPosition()),
                    newRecordHeader);

            indexFile.updateInMemoryIndex(key, newRecordHeader);

            recordFile.writeData(newRecordHeader.getDataIndex(), targetData);

            recordFile.increaseRecordCount(1);
        } else if (originRecordHeader.getDataSize() == realSize) {
            recordFile.writeData(
                    originRecordHeader.getDataIndex(),
                    targetData);
        } else {
            long newKey = indexFile.generateKey();

            indexFile.addIndexNode(
                    originRecordHeader.getDataIndex(),
                    newKey,
                    originRecordHeader);
            deleteRecord(newKey);
            indexFile.writeLastDeletedKey(newKey);

            RecordHeader newRecordHeader = new RecordHeader(
                    recordFile.getEofFp(),
                    realSize
            );
            newRecordHeader.setSelfPosition(originRecordHeader.getSelfPosition());
            indexFile.writeRecordHeader(
                    indexFile.getRecordHeaderFp(newRecordHeader.getSelfPosition()),
                    newRecordHeader);

            indexFile.updateInMemoryIndex(key, newRecordHeader);

            recordFile.writeData(newRecordHeader.getDataIndex(), targetData);

            recordFile.increaseRecordCount(1);
        }

        //업데이트 할 인덱스의 현재 크기를 가져오기

        //업데이트 하고싶은 크기가 더 큰 경우

        //업데이트 하고싶은 크기가 더 작은 경우

        //업데이트 하고싶은 크기가 같은 경우
        return key;
    }

    synchronized public List<T> findAllRecords() {
        List<RecordHeader> recordHeaders = indexFile.getRecordHeaders();
        return mappingRecord(recordHeaders);
    }

    synchronized public long count() {
        return findAllRecords().size();
    }

    synchronized public List<T> findAllRecords(long offset, int pageSize) {
        List<T> allRecords = findAllRecords();
        long count = allRecords.size();
        if (count < offset + pageSize) {
            int to = (int) (count % pageSize) + (int) offset;
            return allRecords.subList((int) offset, to);
        } else {
            return allRecords.subList((int) offset, pageSize);
        }
    }

    synchronized public List<T> findRecords(Long... keys) {
        List<RecordHeader> recordHeaders = indexFile.getRecordHeadersFromKeys(keys);
        return mappingRecord(recordHeaders);
    }

    synchronized public T findRecord(Long key) {
        RecordHeader recordHeaderFromKey = indexFile.getRecordHeaderFromKey(key);
        if (recordHeaderFromKey == null || recordHeaderFromKey.getNextDeleteKey() != -99L) {
            return null;
        }
        long dataIndex = recordHeaderFromKey.getDataIndex();
        String[] split = recordFile.readData(dataIndex).split("[" + RECORD_DATA_SEPARATOR + "]");

        return parseToGenericTypeObject(split);
    }

    private T parseToGenericTypeObject(String[] split) {

        T returnObject = instantiateGenericObject();

        List<Field> fields = getFieldsInClazz(returnObject);
        try {
            for (int i = 0; i < split.length; i++) {
                fields.get(i).setAccessible(true);
                if (split[i] == null || split[i].isEmpty() || split[i].equals("null")) {
                    fields.get(i).set(returnObject, null);
                }

                Object value = split[i];

                if (!fields.get(i).getType().equals(String.class)) {
                    value = fields.get(i)
                            .getType()
                            .getMethod("valueOf", String.class)
                            .invoke(null, split[i]);
                }
                fields.get(i).set(returnObject, value);
            }
        } catch (ReflectiveOperationException e) {
            log.error("cannot parse generic type object");
            throw new ParseException(e);
        }
        return returnObject;
    }

    private T instantiateGenericObject() {
        try {
            Constructor<T> declaredConstructor = genericClazz.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            return declaredConstructor.newInstance();
        } catch (ReflectiveOperationException e) {
            log.error("cannot instantiate {} type object", genericClazz.getTypeName());
            throw new IndexFileException(e);
        }
    }

    //declaredMethods() 순서를 보장하지 않으므로 오름차순 확정 순서로 정렬

    private List<Method> getDeclaredMethodsSortByName(Class<?> clazz) {
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
        for (Field field : declaredFields) {
            if (field.getDeclaredAnnotation(Id.class) != null) {
                field.setAccessible(true);
                try {
                    field.set(data, key);
                } catch (IllegalAccessException e) {
                    log.error("cannot set key to generic type key object");
                    throw new IndexFileException(e);
                }
            }
        }

        //@Field 가 달린 필드이며 프로퍼티명과 일치하는 필드를 직렬화
        for (Method declaredMethod : declaredMethods) {
            if (isGetProperty(declaredMethod)
                    && isFieldProperty(fieldsNamesInClazz, declaredMethod)) {
                try {
                    Class<?> returnType = declaredMethod.getReturnType();
                    Object invoke = declaredMethod.invoke(data);

                    if (invoke == null) {
                        datas.add(null);
                    } else {
                        datas.add(returnType.cast(invoke).toString());
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    log.error("cannot invoke generic type method");
                    throw new IndexFileException(e);
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
        String methodName = declaredMethod.getName().replace("get", "");
        return fieldsNamesInClazz.contains(methodName.substring(0, 1).toLowerCase() + methodName.substring(1));
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

    private List<T> mappingRecord(List<RecordHeader> recordHeaders) {
        return recordHeaders.stream()
                .filter(recordHeader -> recordHeader.getNextDeleteKey() == -99L)
                .map(RecordHeader::getDataIndex)
                .map(recordFile::readData)
                .map(recordData -> recordData.split("[" + RECORD_DATA_SEPARATOR + "]"))
                .map(this::parseToGenericTypeObject)
                .collect(Collectors.toList());
    }
}
