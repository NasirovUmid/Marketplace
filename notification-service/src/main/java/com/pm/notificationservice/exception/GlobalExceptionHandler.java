package com.pm.notificationservice.exception;

import com.pm.commonevents.exception.ApiProblem;
import com.pm.commonevents.exception.InternalProblemException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InternalProblemException.class)
    public ResponseEntity<ProblemDetail> handleNotificationInternalProblemException(InternalProblemException internalProblemException, HttpServletRequest httpServletRequest) {

        logger.error("Problem with {}", internalProblemException.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiProblem.of(HttpStatus.INTERNAL_SERVER_ERROR, internalProblemException.toString(), internalProblemException.getMessage(), httpServletRequest, internalProblemException)
        );
    }
}
