package com.fast.hands.test_task.dto.apilayer;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ApiLayer {
    private String source;
    private OffsetDateTime timestamp;
    private Map<String, BigDecimal> quotes;
}
