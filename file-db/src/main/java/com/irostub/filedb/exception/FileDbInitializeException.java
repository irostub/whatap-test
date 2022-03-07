package com.irostub.filedb.exception;

public class FileDbInitializeException extends RuntimeException{
    public FileDbInitializeException() {
        super();
    }

    public FileDbInitializeException(String message) {
        super(message);
    }

    public FileDbInitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileDbInitializeException(Throwable cause) {
        super(cause);
    }

    protected FileDbInitializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
