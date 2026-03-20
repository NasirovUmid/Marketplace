package com.pm.userservice.exception;

import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.ApiProblem;
import com.pm.commonevents.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(NotFoundException notFoundException, HttpServletRequest httpServletRequest) {

        logger.error("USER WITH = [ {} ] NOT FOUND!!", notFoundException.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiProblem.of(HttpStatus.NOT_FOUND, notFoundException.getMessage(), httpServletRequest, notFoundException)
        );
    }


    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAlreadyExistsException(AlreadyExistsException alreadyExistsException, HttpServletRequest httpServletRequest) {

        logger.error("USER WITH THIS EMAIL ALREADY EXISTS = {}", alreadyExistsException.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiProblem.of(HttpStatus.CONFLICT, alreadyExistsException.getMessage(), httpServletRequest, alreadyExistsException)
        );
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
