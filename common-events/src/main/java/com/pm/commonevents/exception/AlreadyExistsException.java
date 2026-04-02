package com.pm.commonevents.exception;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message+" ALREADY EXISTS !!");
    }
}
