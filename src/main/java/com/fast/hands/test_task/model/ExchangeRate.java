package com.fast.hands.test_task.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
public class ExchangeRate {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "source", nullable = false)
    private Currency source;
    @ManyToOne
    @JoinColumn(name = "target", nullable = false)
    private Currency target;
    @Column
    private BigDecimal rate;
    @Column
    private OffsetDateTime timestamp;
}
