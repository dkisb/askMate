package com.codecool.askmateoop.errorhandler.custom_exceptions;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String email) {
        super("Email already in use: " + email);
    }
}
