package com.cebrail.ecbrates.Repository;

import com.cebrail.ecbrates.Model.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DayRepository extends JpaRepository<Day, LocalDate> {
    @Query("select d from Day d where d.date between ?1 and ?2")
    List<Day> findByTimeBetween(LocalDate start, LocalDate end);
}
