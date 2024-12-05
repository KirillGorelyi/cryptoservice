package com.epam.cryptoservice.controller;

import com.epam.cryptoservice.exception.CoinISNotPresentInSystemException;
import com.epam.cryptoservice.schema.dto.CoinNormalizedRangeDto;
import com.epam.cryptoservice.schema.dto.CoinPeriodStatsDto;
import com.epam.cryptoservice.schema.dto.CoinPriceInfoDto;
import com.epam.cryptoservice.service.CalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class CalculationController {
    private final CalculationService calculationService;

    @GetMapping("/all/normalized-range")
    @Cacheable("requests")
    public ResponseEntity<List<CoinNormalizedRangeDto>> getAllCoinsNormalizedRange() {
        List<CoinNormalizedRangeDto> coinRanges = calculationService.getAllCoinsNormalizedRange();
        return ResponseEntity.ok(coinRanges);
    }

    @GetMapping("/{coin}/info")
    @Cacheable("requests")
    public ResponseEntity<CoinPriceInfoDto> getCoinPriceInfo(@PathVariable String coin)
            throws CoinISNotPresentInSystemException {
        CoinPriceInfoDto coinPriceInfo = calculationService.getCoinPriceInfo(coin);
        if (coinPriceInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coinPriceInfo);
    }

    @GetMapping("/{coin}/info/in-period")
    @Cacheable("requests")
    public ResponseEntity<CoinPriceInfoDto> getCoinPriceInfoForThePeriod(
            @PathVariable String coin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
            throws CoinISNotPresentInSystemException {
        CoinPriceInfoDto coinPriceInfo =
                calculationService.getCoinPriceInfoByPeriod(coin, startDate, endDate);
        if (coinPriceInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coinPriceInfo);
    }

    @GetMapping("/highest-normalized-range")
    @Cacheable("requests")
    public ResponseEntity<CoinNormalizedRangeDto> getCoinWithHighestNormalizedRangeForDay(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CoinNormalizedRangeDto coinNormalizedRangeDto =
                calculationService.getCoinWithHighestNormalizedRangeForDay(date);
        if (coinNormalizedRangeDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coinNormalizedRangeDto);
    }

    @GetMapping("/period-stats")
    @Cacheable("requests")
    public ResponseEntity<CoinPeriodStatsDto> getCoinPeriodStats(
            @RequestParam(value = "months", required = false, defaultValue = "1") String coin,
            @RequestParam(value = "months", required = false, defaultValue = "1") int months)
            throws CoinISNotPresentInSystemException {
        CoinPeriodStatsDto stats = calculationService.getCoinPeriodStats(coin, months);
        return ResponseEntity.ok(stats);
    }
}
