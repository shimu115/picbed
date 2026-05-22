package com.picbed.exception;

public class OssOperationException extends RuntimeException {

    public OssOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
