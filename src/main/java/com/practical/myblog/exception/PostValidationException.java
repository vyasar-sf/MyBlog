package com.practical.myblog.exception;

public class PostValidationException extends RuntimeException {

    public PostValidationException(String message) {
        super(message);
    }
}

