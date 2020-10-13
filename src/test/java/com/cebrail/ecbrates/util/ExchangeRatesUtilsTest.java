package com.cebrail.ecbrates.util;

import com.cebrail.ecbrates.Model.Currency;
import com.cebrail.ecbrates.Model.Day;
import com.cebrail.ecbrates.UnsupportedCurrencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRatesUtilsTest {

    private ExchangeRatesUtils exchangeRatesUtils;

    @BeforeEach
    private void initializeBean() {
        exchangeRatesUtils = new ExchangeRatesUtils();
    }

    @Test
    void rebase() {
        List<Currency> currencyList = new ArrayList<>();
        var usd = new Currency("USD", 10d);
        var try_ = new Currency("TRY", 30d);
        var gdp = new Currency("GDP", 40d);
        var smb = new Currency("SmB", 32d);
        currencyList.add(usd);
        currencyList.add(try_);
        currencyList.add(gdp);
        currencyList.add(smb);

        Day day = new Day(LocalDate.now(), currencyList);

        exchangeRatesUtils.rebase(day, "USD");

        assertEquals(3d, try_.getRate());
        assertEquals(1.0d, usd.getRate());
        assertEquals(4.0d, gdp.getRate());
        assertEquals(3.2d, smb.getRate());
        assertEquals(LocalDate.now(), day.getDate());
    }

    @Test
    void rebase_EUR() {
        List<Currency> currencyList = new ArrayList<>();
        var usd = new Currency("USD", 10d);
        var try_ = new Currency("TRY", 30d);
        var gdp = new Currency("GDP", 40d);
        var smb = new Currency("SmB", 32d);
        currencyList.add(usd);
        currencyList.add(try_);
        currencyList.add(gdp);
        currencyList.add(smb);

        Day day = new Day(LocalDate.now(), currencyList);

        exchangeRatesUtils.rebase(day, "EUR");

        assertEquals(10d, usd.getRate());
        assertEquals(30d, try_.getRate());
        assertEquals(40d, gdp.getRate());
        assertEquals(4, day.getCurrencies().size());
    }

    @Test
    void rebaseBy_NonExistentCurrencySymbol() {
        List<Currency> currencyList = new ArrayList<>();
        var usd = new Currency("USD", 10d);
        var try_ = new Currency("TRY", 30d);
        var gdp = new Currency("GDP", 40d);
        var smb = new Currency("SmB", 32d);
        currencyList.add(usd);
        currencyList.add(try_);
        currencyList.add(gdp);
        currencyList.add(smb);

        Day day = new Day(LocalDate.now(), currencyList);

        exchangeRatesUtils.rebase(day, "TEST-NA");

        assertEquals(10d, usd.getRate());
        assertEquals(30d, try_.getRate());
        assertEquals(40d, gdp.getRate());
        assertEquals(4, day.getCurrencies().size());
    }

    @Test
    void pickAllSelected() {
        List<Currency> currencyList = new ArrayList<>();
        var usd = new Currency("USD", 10d);
        var try_ = new Currency("TRY", 30d);
        var gdp = new Currency("GDP", 40d);
        var pln = new Currency("pln", 32d);
        currencyList.add(usd);
        currencyList.add(try_);
        currencyList.add(gdp);
        currencyList.add(pln);

        Day day = new Day(LocalDate.now(), currencyList);

        List<String> symbols = List.of("TRY", "PLn");

        exchangeRatesUtils.pickAllSelected(day, symbols);

        assertEquals(symbols.size(), day.getCurrencies().size());
    }


    @Test
    void pick_NonExistentSymbol_ShouldThrowException() {
        List<Currency> currencyList = new ArrayList<>();
        var usd = new Currency("USD", 10d);
        var try_ = new Currency("TRY", 30d);
        var gdp = new Currency("GDP", 40d);
        var pln = new Currency("PLN", 32d);
        currencyList.add(usd);
        currencyList.add(try_);
        currencyList.add(gdp);
        currencyList.add(pln);

        Day day = new Day(LocalDate.now(), currencyList);

        List<String> symbols = List.of("TRY", "TEST-NA");

        assertThrows(UnsupportedCurrencyException.class, () -> exchangeRatesUtils.pickAllSelected(day, symbols));
    }
}