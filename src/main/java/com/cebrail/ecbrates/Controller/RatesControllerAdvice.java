package com.cebrail.ecbrates.Controller;

import com.cebrail.ecbrates.CannotRebaseCurrenciesException;
import com.cebrail.ecbrates.UnsupportedCurrencyException;
import com.cebrail.ecbrates.util.ExchangeRatesUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RatesControllerAdvice {

    @ExceptionHandler(UnsupportedCurrencyException.class)
    ResponseEntity<Object> handleUnsupportedCurrencyException(UnsupportedCurrencyException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "The currency symbols you requested are unvalid or not supported.");
        response.put("time", LocalDateTime.now());
        response.put("supported_symbols", ExchangeRatesUtils.supportedCurrencies);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CannotRebaseCurrenciesException.class)
    ResponseEntity<Object> handleCannotRebaseCurrencyException(CannotRebaseCurrenciesException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Cannot rebase by '" + ex.getBaseCurrencyName() +
                "' at the given day: " + ex.getDay().getDate().toString() +
                "\n Possible reasons: There is no data for the requested currency" +
                "\n Please try again with a different interval that does not contain the '" + ex.getDay().getDate().toString() + "'.");
        response.put("time", LocalDateTime.now());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }


}
