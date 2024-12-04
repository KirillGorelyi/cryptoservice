package com.epam.cryptoservice.service;

import com.epam.cryptoservice.exception.CoinISNotPresentInSystemException;
import com.epam.cryptoservice.schema.dto.CoinNormalizedRangeDto;
import com.epam.cryptoservice.schema.dto.CoinPeriodStatsDto;
import com.epam.cryptoservice.schema.dto.CoinPriceInfoDto;

import java.time.LocalDate;
import java.util.List;

public interface CalculationService {
    List<CoinNormalizedRangeDto> getAllCoinsNormalizedRange();

    CoinPriceInfoDto getCoinPriceInfo(String coin) throws CoinISNotPresentInSystemException;

    CoinNormalizedRangeDto getCoinWithHighestNormalizedRangeForDay(LocalDate date);

    CoinPeriodStatsDto getCoinPeriodStats(String coin, int months) throws CoinISNotPresentInSystemException;

    CoinPriceInfoDto getCoinPriceInfoByPeriod(String coin,
                                              LocalDate startDate,
                                              LocalDate endDate)
            throws CoinISNotPresentInSystemException;
}
