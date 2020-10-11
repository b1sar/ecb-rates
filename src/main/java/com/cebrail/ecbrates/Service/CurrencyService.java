package com.cebrail.ecbrates.Service;

import com.cebrail.ecbrates.Model.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface CurrencyService {
    Page<Currency> findAll(Pageable pageable);
}
