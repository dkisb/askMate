package com.codecool.askmateoop.errorhandler;

import com.codecool.askmateoop.errorhandler.custom_exceptions.EmailAlreadyInUseException;
import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.errorhandler.custom_exceptions.UsernameAlreadyExistsException;
import com.codecool.askmateoop.model.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
        logger.error("Error: ", e);
        return new ErrorMessage(HttpStatus.CONFLICT.value(), e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleEmailAlreadyInUseException(EmailAlreadyInUseException e) {
        logger.error("Error: ", e);
        return new ErrorMessage(HttpStatus.CONFLICT.value(), e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleBadCredentials(BadCredentialsException e) {
        logger.error("Error: ", e);
        return new ErrorMessage(HttpStatus.UNAUTHORIZED.value(), "Invalid username or password");
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNoSuchFieldException(NoSuchElementException e) {
        logger.error("Error: ", e);
        return new ErrorMessage(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(NotAllowedOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleNotAllowedOperationException(NotAllowedOperationException e) {
        logger.error("Error: ", e);
        return new ErrorMessage(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleOtherException(Exception e) {
        logger.error("Error: ", e);
        return new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred");
    }
}
