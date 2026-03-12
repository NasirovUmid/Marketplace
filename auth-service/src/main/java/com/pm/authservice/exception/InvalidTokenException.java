package com.pm.authservice.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String token) {
        super("The token = [ "+token+" ] IS NOT VALID!");
    }
}
