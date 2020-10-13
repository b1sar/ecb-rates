package com.cebrail.ecbrates;

public class UnsupportedCurrencyException extends RuntimeException {
    public UnsupportedCurrencyException(String message) {
        super(message);
    }

    public UnsupportedCurrencyException(String message, Throwable rootException) {
        super(message, rootException);
    }
}
