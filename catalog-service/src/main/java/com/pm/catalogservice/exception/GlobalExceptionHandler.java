package com.pm.catalogservice.exception;

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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCatalogNotFoundException(NotFoundException notFoundException, HttpServletRequest httpServletRequest) {

        logger.error("CATALO NOT FOUND EXCEPTION = {} ", notFoundException.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiProblem.of(HttpStatus.NOT_FOUND, notFoundException.toString(), notFoundException.getMessage(), httpServletRequest, notFoundException)
        );
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleCatalogAlreadyExistsException(AlreadyExistsException alreadyExistsException, HttpServletRequest httpServletRequest) {

        logger.error("CATALO ALREADY EXISTS EXCEPTION = {}", alreadyExistsException.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiProblem.of(HttpStatus.CONFLICT, alreadyExistsException.toString(), alreadyExistsException.getMessage(), httpServletRequest, alreadyExistsException)
        );
    }
}
