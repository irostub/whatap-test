package com.irostub.filedb.exception;

public class IndexFileException extends RuntimeException{
    public IndexFileException() {
        super();
    }

    public IndexFileException(String message) {
        super(message);
    }

    public IndexFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexFileException(Throwable cause) {
        super(cause);
    }

    protected IndexFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
