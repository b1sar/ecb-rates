package com.cebrail.ecbrates.Model;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


@Entity
public class Day {
    @ElementCollection
    List<Currency> currencies;
    @Id
    private LocalDate date;

    public Day() {
    }

    public Day(LocalDate time, List<Currency> currencies) {
        this.date = time;
        this.currencies = currencies;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Day)) return false;
        Day day = (Day) o;
        return Objects.equals(getDate(), day.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate());
    }

    @Override
    public String toString() {
        return "Day{" +
                "date=" + date +
                ", currencies=" + currencies +
                '}';
    }
}
