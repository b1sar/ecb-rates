package com.cebrail.ecbrates.Controller;

import com.cebrail.ecbrates.Model.Currency;
import com.cebrail.ecbrates.Model.Day;
import com.cebrail.ecbrates.Service.DayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController()
public class RatesController {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private DayService dayService;

    @Autowired
    public RatesController(DayService dayService) {
        this.dayService = dayService;
    }

    @GetMapping("/latest")
    public Day getLatest(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> at,
                         @RequestParam(required = false) Optional<String> base,
                         @RequestParam(required = false) Optional<List<String>> symbols) {
        Optional<Day> day = Optional.empty();
        if (at.isPresent()) {
            day = dayService.findById(at.get());
        } else {
            day = dayService.findById(LocalDate.of(2020, 10, 9));
        }

        if (day.isPresent()) {
            rebase(day.get(), base);
            pickAllSelected(day.get(), symbols);
            return day.get();
        }
        return new Day();
    }

    @GetMapping("/historical")
    public List<Day> getHistorical(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> to,
                                   @RequestParam(required = false) Optional<String> base,
                                   @RequestParam(required = false) Optional<List<String>> symbols) {
        List<Day> days = new ArrayList<>();

        if (from.isPresent() && to.isPresent()) {
            days = dayService.findByDateBetween(from.get(), to.get());
        } else if (from.isPresent()) {
            days = dayService.findByDateBetween(from.get(), LocalDate.now(ZoneId.systemDefault()));
        } else if (to.isPresent()) {
            days = dayService.findByDateBetween(LocalDate.of(1999, 1, 4), to.get());
        } else {
            days = dayService.findAll();
        }

        for (Day d : days) {
            rebase(d, base);
            pickAllSelected(d, symbols);
        }
        return days;
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    //utils
    private void rebase(Day day, Optional<String> b) {
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

    private void pickAllSelected(Day day, Optional<List<String>> symbols) {
        if (symbols.isPresent()) {
            List<Currency> newCurrencies = day.getCurrencies().stream()
                    .filter(currency -> symbols.get().contains(currency.getName()))
                    .collect(Collectors.toList());
            day.setCurrencies(newCurrencies);
        }
    }
}
