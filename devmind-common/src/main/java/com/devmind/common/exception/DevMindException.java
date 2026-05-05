package com.devmind.common.exception;

public class DevMindException extends RuntimeException {

    private final String errorCode;

    public DevMindException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public DevMindException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DevMindException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INTERNAL_ERROR";
    }

    public String getErrorCode() { return errorCode; }
}
