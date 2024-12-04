package com.epam.cryptoservice.exception;

public class CoinISNotPresentInSystemException extends Exception {
    public CoinISNotPresentInSystemException() {
        super("The coin is not present in the system. " +
                "Please, upload its data or " +
                "use the system api for fetching the live data from the market.");
    }
}
