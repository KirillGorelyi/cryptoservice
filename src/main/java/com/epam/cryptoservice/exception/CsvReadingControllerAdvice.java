package com.epam.cryptoservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class CsvReadingControllerAdvice {
    @ExceptionHandler(CsvReadingException.class)
    public ResponseEntity<String> handleResourceNotFoundException(
            CsvReadingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
