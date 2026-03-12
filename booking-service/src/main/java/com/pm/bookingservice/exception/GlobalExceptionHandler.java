package com.pm.bookingservice.exception;

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

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleBookingAlreadyExistsException(AlreadyExistsException alreadyExistsException, HttpServletRequest httpServletRequest){

        logger.error("Booking AlreadyExistsException = {} ",alreadyExistsException.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiProblem.of(HttpStatus.CONFLICT,alreadyExistsException.toString(), alreadyExistsException.getMessage(), httpServletRequest,alreadyExistsException));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleBookingNotFoundException(NotFoundException notFoundException,HttpServletRequest httpServletRequest){

        logger.error("Booking NotFoundException = {}",notFoundException.getMessage());


        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiProblem.of(HttpStatus.NOT_FOUND,notFoundException.toString(),notFoundException.getMessage(),httpServletRequest,notFoundException));

    }

}
