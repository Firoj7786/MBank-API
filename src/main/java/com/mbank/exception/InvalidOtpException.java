package com.mbank.exception;

public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException(String msg) {
        super(msg);
    }
}
