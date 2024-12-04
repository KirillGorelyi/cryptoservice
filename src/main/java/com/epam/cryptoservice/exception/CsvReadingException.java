package com.epam.cryptoservice.exception;

public class CsvReadingException extends Exception {
    public CsvReadingException(Exception e) {
        super(String.format("Uploaded file is empty or contains only header row, " +
                "was failed to read " +
                "or does not satisfy the structure: " +
                "timestamp::Long, coin::String, price::Double. PLease, fix the file. " +
                "Detailed message:%s", e.getMessage()));
    }
}
