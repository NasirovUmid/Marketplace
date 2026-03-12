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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyExistsException(AlreadyExistsException emailAlreadyExistsException, HttpServletRequest httpServletRequest) {

        logger.error("EMAIL ALREADY EXISTS EXCEPTION   = {} ", emailAlreadyExistsException.getMessage().toUpperCase());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiProblem.of(HttpStatus.CONFLICT, emailAlreadyExistsException.getMessage(), emailAlreadyExistsException.getMessage(), httpServletRequest, emailAlreadyExistsException));

    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(NotFoundException userNotFoundException, HttpServletRequest httpServletRequest) {

        logger.error("USER DOES NOT EXISTS EXCEPTION = {} ", userNotFoundException.getMessage().toUpperCase());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiProblem.of(HttpStatus.NOT_FOUND, userNotFoundException.toString(), userNotFoundException.getMessage(), httpServletRequest, userNotFoundException));

    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTokenException(InvalidTokenException invalidTokenException,HttpServletRequest httpServletRequest) {

        logger.error("INVALID TOKEN EXCEPTION = {}", invalidTokenException.getMessage().toUpperCase());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiProblem.of(HttpStatus.UNAUTHORIZED,invalidTokenException.toString(),invalidTokenException.getMessage(),httpServletRequest,invalidTokenException));
    }


}
