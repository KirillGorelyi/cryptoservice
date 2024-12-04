package com.epam.cryptoservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CoinIsNotPresentInSystemControllerAdvice {
    @ExceptionHandler(CoinISNotPresentInSystemException.class)
    public ResponseEntity<String> handleResourceNotFoundException(
            CoinISNotPresentInSystemException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
