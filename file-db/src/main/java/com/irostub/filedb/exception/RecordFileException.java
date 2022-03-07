package com.irostub.filedb.exception;

public class RecordFileException extends RuntimeException{
    public RecordFileException() {
        super();
    }

    public RecordFileException(String message) {
        super(message);
    }

    public RecordFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecordFileException(Throwable cause) {
        super(cause);
    }

    protected RecordFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
