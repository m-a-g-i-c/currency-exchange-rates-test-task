package com.fast.hands.test_task.repository;

import com.fast.hands.test_task.model.Currency;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

@RequiredArgsConstructor
@Slf4j
public class CurrencyRepositoryDelegate {

    private final CurrencyRepository currencyRepository;

    public Currency saveThrowable(Currency currency) {
        try {
            return currencyRepository.save(currency);
        } catch (DataIntegrityViolationException e) {
            log.error("Unable to save currency: {}", e.getMessage());
            throw new RuntimeException("Cannot save currency", e);
        }
    }

    public List<Currency> findAll() {
        return currencyRepository.findAll();
    }
}
