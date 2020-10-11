package com.cebrail.ecbrates.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@JsonIgnoreProperties(allowSetters = true, value = {"id", "hibernateLazyInitializer", "handler"})
public class Currency {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private Double rate;

    public Currency() {
    }

    public Currency(String name, Double rate) {
        this.name = name;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        Currency currency = (Currency) o;
        return Objects.equals(getName(), currency.getName()) &
                Objects.equals(getRate(), currency.getRate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getRate());
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", rate=" + rate +
                '}';
    }
}
