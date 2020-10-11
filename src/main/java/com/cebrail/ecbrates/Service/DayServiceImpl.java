package com.cebrail.ecbrates.Service;

import com.cebrail.ecbrates.Model.Day;
import com.cebrail.ecbrates.Repository.DayRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DayServiceImpl implements DayService {
    private DayRepository dayRepository;

    public DayServiceImpl(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }


    @Override
    public Optional<Day> findById(LocalDate date) {
        return dayRepository.findById(date);
    }

    @Override
    public List<Day> findByDateBetween(LocalDate from, LocalDate to) {
        return dayRepository.findByTimeBetween(from, to);
    }
}
