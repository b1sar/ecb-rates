package com.cebrail.ecbrates;

import com.cebrail.ecbrates.Model.Day;

/**
 * Throwed when the base currency is not found in the day, thus cannot rebase it.
 */
public class CannotRebaseCurrenciesException extends RuntimeException {
    private Day day;
    private String baseCurrencyName;

    public CannotRebaseCurrenciesException(String message, Day day, String baseCurrencyName) {
        super(message);
        this.day = day;
        this.baseCurrencyName = baseCurrencyName;
    }

    public CannotRebaseCurrenciesException(String message, Day day, String baseCurrencyName, Throwable rootCause) {
        super(message, rootCause);
        this.day = day;
        this.baseCurrencyName = baseCurrencyName;
    }

    public Day getDay() {
        return day;
    }

    public String getBaseCurrencyName() {
        return baseCurrencyName;
    }
}
