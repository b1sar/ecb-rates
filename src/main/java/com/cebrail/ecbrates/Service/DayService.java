package com.cebrail.ecbrates.Service;

import com.cebrail.ecbrates.Model.Day;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public interface DayService {
    List<Day> findAll();

    Optional<Day> findById(LocalDate date);

    List<Day> findByDateBetween(LocalDate from, LocalDate to);
}
