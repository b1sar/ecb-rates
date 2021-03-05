package com.cebrail.ecbrates.Controller;

import com.cebrail.ecbrates.Model.Day;
import com.cebrail.ecbrates.Service.DayService;
import com.cebrail.ecbrates.util.ExchangeRatesUtils;
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

@RestController("/api/v1")
public class RatesController {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private DayService dayService;
    final ExchangeRatesUtils exchangeRatesUtils;

    @Autowired
    public RatesController(DayService dayService, ExchangeRatesUtils exchangeRatesUtils) {
        this.dayService = dayService;
        this.exchangeRatesUtils = exchangeRatesUtils;
    }

    @GetMapping("/latest")
    public Day getLatest(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> at,
                         @RequestParam(required = false) Optional<String> base,
                         @RequestParam(required = false) Optional<List<String>> symbols) {
        Optional<Day> day = Optional.empty();
        if (at.isPresent()) {
            day = dayService.findById(at.get());
        } else {
            day = dayService.findById(LocalDate.now());
        }

        if (day.isPresent()) {
            if (base.isPresent()) {
                exchangeRatesUtils.rebase(day.get(), base.get());
            }
            if (symbols.isPresent()) {
                exchangeRatesUtils.pickAllSelected(day.get(), symbols.get());
            }
            return day.get();
        } else {
            //execute updateDaily function of a scheduler, then try to get the latest day if there is no date
            //then decide what to return
            return new Day();
        }
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
            if (base.isPresent()) {
                exchangeRatesUtils.rebase(d, base.get());
            }
            if (symbols.isPresent()) {
                exchangeRatesUtils.pickAllSelected(d, symbols.get());
            }
        }
        return days;
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
