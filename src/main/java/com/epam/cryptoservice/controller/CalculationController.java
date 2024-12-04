package com.epam.cryptoservice.controller;

import com.epam.cryptoservice.exception.CoinISNotPresentInSystemException;
import com.epam.cryptoservice.schema.dto.CoinNormalizedRangeDto;
import com.epam.cryptoservice.schema.dto.CoinPeriodStatsDto;
import com.epam.cryptoservice.schema.dto.CoinPriceInfoDto;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface CalculationController {
    ResponseEntity<List<CoinNormalizedRangeDto>> getAllCoinsNormalizedRange();

    ResponseEntity<CoinPriceInfoDto> getCoinPriceInfo(String coin)
            throws CoinISNotPresentInSystemException;

    ResponseEntity<CoinNormalizedRangeDto> getCoinWithHighestNormalizedRangeForDay(LocalDate date);

    ResponseEntity<CoinPeriodStatsDto> getCoinPeriodStats(String coin, int months)
            throws CoinISNotPresentInSystemException;

    ResponseEntity<CoinPriceInfoDto> getCoinPriceInfoForThePeriod(String coin,
                                                                  LocalDate startDate,
                                                                  LocalDate endDate)
            throws CoinISNotPresentInSystemException;
}
