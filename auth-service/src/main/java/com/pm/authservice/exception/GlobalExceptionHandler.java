package com.pm.authservice.exception;

import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.ApiProblem;
import com.pm.commonevents.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyExistsException(AlreadyExistsException emailAlreadyExistsException, HttpServletRequest httpServletRequest) {

        logger.error("EMAIL ALREADY EXISTS EXCEPTION   = {} ", emailAlreadyExistsException.getMessage().toUpperCase());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiProblem.of(HttpStatus.CONFLICT, emailAlreadyExistsException.getMessage(), httpServletRequest, emailAlreadyExistsException));

    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException badCredentialsException, HttpServletRequest httpServletRequest) {

        logger.error("WRONG CREDENTIALS = {}", badCredentialsException.getMessage().toUpperCase());

        return ResponseEntity.status(401).body(
                ApiProblem.of(HttpStatus.UNAUTHORIZED, badCredentialsException.getMessage(), httpServletRequest, badCredentialsException));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(NotFoundException userNotFoundException, HttpServletRequest httpServletRequest) {

        logger.error("USER DOES NOT EXISTS EXCEPTION = {} ", userNotFoundException.getMessage().toUpperCase());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiProblem.of(HttpStatus.NOT_FOUND, userNotFoundException.getMessage(), httpServletRequest, userNotFoundException));

    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTokenException(InvalidTokenException invalidTokenException, HttpServletRequest httpServletRequest) {

        logger.error("INVALID TOKEN EXCEPTION = {}", invalidTokenException.getMessage().toUpperCase());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiProblem.of(HttpStatus.UNAUTHORIZED, invalidTokenException.getMessage(), httpServletRequest, invalidTokenException));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest httpServletRequest) {

        logger.error("UNEXPECTED ERROR", exception);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String detail = exception.getMessage();

        if (exception instanceof ErrorResponse errorResponse) {

            status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            detail = errorResponse.getBody().getDetail();
        } else if (exception instanceof ResponseStatusException rsException) {
            status = HttpStatus.valueOf(rsException.getStatusCode().value());
            detail = rsException.getReason();
        }

        return ResponseEntity.status(status).body(
                ApiProblem.of(status, detail, httpServletRequest, exception));


    }

}
