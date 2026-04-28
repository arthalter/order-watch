package com.orderwatch.backend.api;

public enum ErrorCode {
    OK(0),
    BAD_REQUEST(40000),
    INTERNAL_ERROR(50000);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}

