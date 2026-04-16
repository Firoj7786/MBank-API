package com.mbank.exception;

public class UserInvalidException extends RuntimeException {

    public UserInvalidException(String message) {
        super(message);
    }
}
