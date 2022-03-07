package com.irostub.filedb.exception;

public class IndexFileInitializeException extends RuntimeException{
    public IndexFileInitializeException() {
        super();
    }

    public IndexFileInitializeException(String message) {
        super(message);
    }

    public IndexFileInitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexFileInitializeException(Throwable cause) {
        super(cause);
    }

    protected IndexFileInitializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
