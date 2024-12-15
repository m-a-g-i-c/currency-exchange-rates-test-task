package com.fast.hands.test_task.repository;

import com.fast.hands.test_task.model.ExchangeRate;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

@RequiredArgsConstructor
@Slf4j
public class ExchangeRateRepositoryDelegate {
    private final ExchangeRateRepository exchangeRateRepository;

    public List<ExchangeRate> saveAll(List<ExchangeRate> list) {
        try {
            return exchangeRateRepository.saveAll(list);
        } catch (DataIntegrityViolationException e) {
            log.error("Unable to save exchange rates: {}", e.getMessage());
            throw new RuntimeException("Cannot save exchange rates", e);
        }
    }
}
