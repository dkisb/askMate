package com.codecool.askmateoop.errorhandler.custom_exceptions;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Username " + username + " already exists");

    }
}
