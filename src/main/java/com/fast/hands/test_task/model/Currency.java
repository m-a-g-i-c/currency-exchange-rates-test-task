package com.fast.hands.test_task.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
public class Currency {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false, unique = true, length = 3)
    private String currencyCode;
    @Column
    private String name;
    @Column
    private String description;

    public Currency(){}
    public Currency(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
