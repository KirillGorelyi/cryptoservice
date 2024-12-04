package com.epam.cryptoservice.schema.dto;

import com.epam.cryptoservice.schema.entity.PriceEntity;

public record CoinPeriodStatsDto(Long coinId,
                                 PriceEntity oldestPrice,
                                 PriceEntity newestPrice,
                                 PriceEntity minPrice,
                                 PriceEntity maxPrice) {
}
