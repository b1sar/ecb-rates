package com.cebrail.ecbrates.util;

import com.cebrail.ecbrates.Model.Currency;
import com.cebrail.ecbrates.Model.Day;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class ExchangeRatesUtils {
    //utils
    public void rebase(Day day, Optional<String> b) {
        List<Currency> currencies = day.getCurrencies();
        if (b.isPresent() && !b.get().equals("EUR")) {
            Optional<Currency> base = currencies.stream()
                    .filter(currency -> b.get().toLowerCase().equals(currency.getName().toLowerCase()))
                    .findFirst();
            Double baseRate = base.get().getRate();//TODO: base.get() is used withoed base.isPresent() check. Refactor.
            for (Currency d : currencies) {

                d.setRate(d.getRate() / baseRate);
            }
            day.setCurrencies(currencies);
        }
    }

    public void pickAllSelected(Day day, Optional<List<String>> symbols) {
        if (symbols.isPresent()) {
            List<Currency> newCurrencies = day.getCurrencies().stream()
                    .filter(currency -> symbols.get().contains(currency.getName()))
                    .collect(Collectors.toList());
            day.setCurrencies(newCurrencies);
        }
    }
}
