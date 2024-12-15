package com.fast.hands.test_task.repository;

import com.fast.hands.test_task.model.Currency;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

}
