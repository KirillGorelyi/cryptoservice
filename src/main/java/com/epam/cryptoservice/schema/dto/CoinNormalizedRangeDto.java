package com.epam.cryptoservice.schema.dto;


import java.math.BigDecimal;

public record CoinNormalizedRangeDto(Long coinId, BigDecimal normalizedRange)
        implements Comparable<CoinNormalizedRangeDto> {
    @Override
    public int compareTo(CoinNormalizedRangeDto other) {
        return other.normalizedRange.compareTo(this.normalizedRange);
    }
}
