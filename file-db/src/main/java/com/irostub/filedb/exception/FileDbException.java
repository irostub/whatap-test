package com.irostub.filedb.exception;

public class FileDbException extends RuntimeException{
    public FileDbException() {
        super();
    }

    public FileDbException(String message) {
        super(message);
    }

    public FileDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileDbException(Throwable cause) {
        super(cause);
    }

    protected FileDbException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
