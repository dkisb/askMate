package com.codecool.askmateoop.errorhandler.custom_exceptions;

public class NotAllowedOperationException extends RuntimeException {
    public NotAllowedOperationException(String message) {
        super(message);
    }
}
