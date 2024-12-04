package com.epam.cryptoservice.service.impl;

import com.epam.cryptoservice.exception.CoinISNotPresentInSystemException;
import com.epam.cryptoservice.repository.service.CoinRepositoryService;
import com.epam.cryptoservice.repository.service.PriceRepositoryService;
import com.epam.cryptoservice.schema.dto.CoinNormalizedRangeDto;
import com.epam.cryptoservice.schema.dto.CoinPeriodStatsDto;
import com.epam.cryptoservice.schema.dto.CoinPriceInfoDto;
import com.epam.cryptoservice.schema.dto.CoinPriceStatsDto;
import com.epam.cryptoservice.schema.entity.CoinEntity;
import com.epam.cryptoservice.schema.entity.PriceEntity;
import com.epam.cryptoservice.service.CalculationService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {
    private final static Object lock = new Object();
    private final PriceRepositoryService priceRepositoryService;
    private final CoinRepositoryService coinRepositoryService;

    @Setter
    private Clock clock = Clock.systemDefaultZone();

    public List<CoinNormalizedRangeDto> getAllCoinsNormalizedRange() {
        List<CoinPriceStatsDto> coinPriceStats;
        synchronized (lock) {
            coinPriceStats = priceRepositoryService.findMaxMinPriceForAllCoins();
        }
        List<CoinNormalizedRangeDto> coinRanges = new ArrayList<>();

        for (CoinPriceStatsDto stats : coinPriceStats) {
            Long coinId = stats.coinId();
            BigDecimal maxPrice = stats.maxPrice();
            BigDecimal minPrice = stats.minPrice();

            if (minPrice != null && minPrice.compareTo(BigDecimal.valueOf(0)) > 0) {
                BigDecimal normalizedRange = maxPrice
                        .subtract(minPrice)
                        .divide(minPrice, 10, RoundingMode.HALF_UP);
                coinRanges.add(new CoinNormalizedRangeDto(coinId, normalizedRange));
            }
        }
        Collections.sort(coinRanges);
        return coinRanges;
    }

    public CoinPriceInfoDto getCoinPriceInfo(String coin) throws CoinISNotPresentInSystemException {
        Long coinId = fetchCoinId(coin);
        PriceEntity oldestPrice;
        PriceEntity newestPrice;
        PriceEntity minPrice;
        PriceEntity maxPrice;
        synchronized (lock) {
            oldestPrice = priceRepositoryService.findOldestPriceByCoin(coinId);
            newestPrice = priceRepositoryService.findNewestPriceByCoin(coinId);
            minPrice = priceRepositoryService.findMinPriceByCoin(coinId);
            maxPrice = priceRepositoryService.findMaxPriceByCoin(coinId);
        }
        if (oldestPrice == null && newestPrice == null && minPrice == null && maxPrice == null)
            return null;


        return new CoinPriceInfoDto(coinId, oldestPrice, newestPrice, minPrice, maxPrice);
    }

    public CoinPriceInfoDto getCoinPriceInfoByPeriod(String coin,
                                                     LocalDate startDate,
                                                     LocalDate endDate)
            throws CoinISNotPresentInSystemException {
        Long coinId = fetchCoinId(coin);
        Long startTimestamp =
                startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
        Long endTimestamp =
                endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
        PriceEntity oldestPrice;
        PriceEntity newestPrice;
        PriceEntity minPrice;
        PriceEntity maxPrice;
        synchronized (lock) {
            oldestPrice = priceRepositoryService.findOldestPriceByCoin(
                    coinId,
                    startTimestamp,
                    endTimestamp);
            newestPrice = priceRepositoryService.findNewestPriceByCoin(
                    coinId,
                    startTimestamp,
                    endTimestamp);
            minPrice = priceRepositoryService.findMinPriceByCoin(
                    coinId,
                    startTimestamp,
                    endTimestamp);
            maxPrice = priceRepositoryService.findMaxPriceByCoin(
                    coinId,
                    startTimestamp,
                    endTimestamp);
        }

        if (oldestPrice == null && newestPrice == null && minPrice == null && maxPrice == null)
            return null;


        return new CoinPriceInfoDto(coinId, oldestPrice, newestPrice, minPrice, maxPrice);
    }

    public CoinNormalizedRangeDto getCoinWithHighestNormalizedRangeForDay(LocalDate date) {
        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        Long startTimestamp = startOfDay.getEpochSecond();
        Long endTimestamp = endOfDay.getEpochSecond() - 1;

        List<CoinPriceStatsDto> coinPriceStats;
        synchronized (lock) {
            coinPriceStats =
                    priceRepositoryService
                            .findMaxMinPriceForAllCoinsOnDay(startTimestamp, endTimestamp);
        }
        return getCoinNormalizedRange(coinPriceStats);
    }

    private CoinNormalizedRangeDto getCoinNormalizedRange(List<CoinPriceStatsDto> coinPriceStats) {
        if (coinPriceStats.isEmpty()) return null;
        CoinNormalizedRangeDto highestNormalizedRangeCoin = null;

        for (CoinPriceStatsDto stats : coinPriceStats) {
            Long coinId = stats.coinId();
            BigDecimal maxPrice = stats.maxPrice();
            BigDecimal minPrice = stats.minPrice();

            if (minPrice != null && minPrice.compareTo(BigDecimal.valueOf(0)) > 0) {
                BigDecimal normalizedRange = maxPrice
                        .subtract(minPrice)
                        .divide(minPrice, 10, RoundingMode.HALF_UP);
                if (highestNormalizedRangeCoin == null
                        || normalizedRange.compareTo(highestNormalizedRangeCoin.normalizedRange()) > 0) {
                    highestNormalizedRangeCoin = new CoinNormalizedRangeDto(coinId, normalizedRange);
                }
            }
        }
        return highestNormalizedRangeCoin;
    }

    public CoinPeriodStatsDto getCoinPeriodStats(String coin, int months) throws CoinISNotPresentInSystemException {
        Long coinId = fetchCoinId(coin);
        if (months <= 0) {
            months = 1;
        }
        Instant endInstant = Instant.now(clock);
        Instant startInstant = endInstant.minus(Duration.ofDays(months * 30L));

        Long startTimestamp = startInstant.getEpochSecond();
        Long endTimestamp = endInstant.getEpochSecond();

        List<PriceEntity> pricesInPeriod;
        synchronized (lock) {
            pricesInPeriod =
                    priceRepositoryService
                            .findAllPricesInPeriod(coinId, startTimestamp, endTimestamp);
        }
        List<PriceEntity> mutablePricesInPeriod = new ArrayList<>(pricesInPeriod);
        mutablePricesInPeriod.sort(Comparator.comparingLong(PriceEntity::getTimestamp));

        return getCoinPeriodStatsDto(coinId, mutablePricesInPeriod);
    }

    private CoinPeriodStatsDto getCoinPeriodStatsDto(Long coinId, List<PriceEntity> prices) {
        if (prices.isEmpty()) return null;

        PriceEntity oldestPrice = prices.get(0);
        PriceEntity newestPrice = prices.get(prices.size() - 1);

        PriceEntity minPriceEntity =
                Collections.min(prices, Comparator.comparing(PriceEntity::getPrice));
        PriceEntity maxPriceEntity =
                Collections.max(prices, Comparator.comparing(PriceEntity::getPrice));

        return new CoinPeriodStatsDto(
                coinId,
                oldestPrice,
                newestPrice,
                minPriceEntity,
                maxPriceEntity);
    }

    private Long fetchCoinId(String coin) throws CoinISNotPresentInSystemException {
        CoinEntity coinEntity;
        synchronized (lock) {
            coinEntity = coinRepositoryService.findByName(coin);
        }
        if (coinEntity == null) throw new CoinISNotPresentInSystemException();
        return coinEntity.getId();
    }
}
