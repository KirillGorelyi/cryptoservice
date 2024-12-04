package com.epam.cryptoservice.schema.dto;

import java.math.BigDecimal;

public record CoinPriceStatsDto(Long coinId, BigDecimal maxPrice, BigDecimal minPrice) {
    public CoinPriceStatsDto(Long coinId, double maxPrice, double minPrice) {
        this(coinId, BigDecimal.valueOf(maxPrice), BigDecimal.valueOf(minPrice));
    }
}
