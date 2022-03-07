package com.irostub.filedb.exception;

public class RecordFileInitializeException extends RuntimeException{
    public RecordFileInitializeException() {
        super();
    }

    public RecordFileInitializeException(String message) {
        super(message);
    }

    public RecordFileInitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecordFileInitializeException(Throwable cause) {
        super(cause);
    }

    protected RecordFileInitializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
