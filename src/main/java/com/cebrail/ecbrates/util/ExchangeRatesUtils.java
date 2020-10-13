package com.cebrail.ecbrates.util;

import com.cebrail.ecbrates.CannotRebaseCurrenciesException;
import com.cebrail.ecbrates.Model.Currency;
import com.cebrail.ecbrates.Model.Day;
import com.cebrail.ecbrates.UnsupportedCurrencyException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class ExchangeRatesUtils {
    public static List<String> supportedCurrencies = List.of("USD", "JPY", "BGN", "CZK", "DKK",
            "GBP", "HUF", "PLN", "RON", "SEK", "CHF", "ISK", "NOK", "HRK", "RUB", "TRY", "AUD",
            "BRL", "CAD", "CNY", "HKD", "IDR", "ILS", "INR", "KRW", "MXN", "MYR", "NZD", "PHP",
            "SGD", "THB", "ZAR");

    public ExchangeRatesUtils() {
    }

    //utils
    public void rebase(Day day, Optional<String> offeredBase) throws CannotRebaseCurrenciesException {

        List<Currency> currencies = day.getCurrencies();
        if (offeredBase.isPresent() && !offeredBase.get().equals("EUR")) {

            if (isBaseSupported(offeredBase.get())) {
                Optional<Currency> base = currencies.stream()
                        .filter(currency -> offeredBase.get().toLowerCase().equals(currency.getName().toLowerCase()))
                        .findFirst();
                /*
                //TODO: base.get() is used withoed base.isPresent() check. Refactor.
                If the base is not present, it means that there is no data about the base, in the specific day.

                In case where a time interval is requested to be rebased and although most of the days in the interval
                have data for that base if a single day happened to have not data, the results would be misleading.

                Because the days that have the 'base' currency data would be rebased and the one's that don't have the 'base'
                currency data would'nt be rebased, howeser the returned result would be consisted of all of them.

                Possible Solution:
                throw an exception( for example DayDoesntHaveData(LocalDateTime, base, "there is no data for the day..")
                and catch that exception in the controller then handle thereafter
                 */
                if (base.isPresent()) {
                    Double baseRate = base.get().getRate();
                    for (Currency d : currencies) {

                        d.setRate(d.getRate() / baseRate);
                    }

                    day.setCurrencies(currencies);
                } else {
                    throw new CannotRebaseCurrenciesException("There is no base currency entry in this day", day, offeredBase.get());
                }
            }
        }
    }

    public void pickAllSelected(Day day, Optional<List<String>> symbols) throws UnsupportedCurrencyException {//filter symbols
        if (symbols.isPresent()) {
            if (areAllSymbolsValid(symbols.get())) {
                List<String> symbolList = symbols.get().stream().map(String::toLowerCase).collect(Collectors.toList());

                List<Currency> newCurrencies = day.getCurrencies().stream()
                        .filter(currency -> symbolList.contains(currency.getName().toLowerCase()))
                        .collect(Collectors.toList());
                day.setCurrencies(newCurrencies);
            } else {
                //throw an unsupportedCurrency exception
                throw new UnsupportedCurrencyException("The currency symbols provided are unvalid or not supported");
            }
        }
    }

    public boolean isBaseSupported(String base) {
        return supportedCurrencies.contains(base.toUpperCase());
    }

    public boolean areAllSymbolsValid(List<String> symbols) {
        List<String> temp = symbols.stream().map(String::toUpperCase).collect(Collectors.toList());
        return supportedCurrencies.containsAll(temp);
    }
}
