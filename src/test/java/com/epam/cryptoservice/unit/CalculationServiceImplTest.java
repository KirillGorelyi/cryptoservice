package com.epam.cryptoservice.unit;

import com.epam.cryptoservice.exception.CoinISNotPresentInSystemException;
import com.epam.cryptoservice.repository.service.CoinRepositoryService;
import com.epam.cryptoservice.repository.service.PriceRepositoryService;
import com.epam.cryptoservice.schema.dto.CoinNormalizedRangeDto;
import com.epam.cryptoservice.schema.dto.CoinPeriodStatsDto;
import com.epam.cryptoservice.schema.dto.CoinPriceInfoDto;
import com.epam.cryptoservice.schema.dto.CoinPriceStatsDto;
import com.epam.cryptoservice.schema.entity.CoinEntity;
import com.epam.cryptoservice.schema.entity.PriceEntity;
import com.epam.cryptoservice.service.impl.CalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculationServiceImplTest {

    @Mock
    private CoinRepositoryService coinRepositoryService;

    @Mock
    private PriceRepositoryService priceRepositoryService;

    @InjectMocks
    private CalculationServiceImpl calculationService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.ofEpochSecond(1623110400L), ZoneId.systemDefault());
        calculationService.setClock(fixedClock);
    }

    @Test
    void testGetAllCoinsNormalizedRange_Success() {
        List<CoinPriceStatsDto> coinPriceStats = Arrays.asList(
                new CoinPriceStatsDto(1L, BigDecimal.valueOf(60000.0), BigDecimal.valueOf(30000.0)),
                new CoinPriceStatsDto(2L, BigDecimal.valueOf(4000.0), BigDecimal.valueOf(2000.0)),
                new CoinPriceStatsDto(3L, BigDecimal.valueOf(1.5), BigDecimal.valueOf(0.5))
        );

        when(priceRepositoryService.findMaxMinPriceForAllCoins()).thenReturn(coinPriceStats);

        List<CoinNormalizedRangeDto> result = calculationService.getAllCoinsNormalizedRange();

        assertNotNull(result);
        assertEquals(3, result.size());

        Map<String, BigDecimal> expectedRanges = new HashMap<>();
        expectedRanges.put("BTC", (BigDecimal.valueOf(60000.0)
                .subtract(BigDecimal.valueOf(30000.0)))
                .divide(BigDecimal.valueOf(30000.0), 10, RoundingMode.HALF_UP));
        expectedRanges.put("ETH", (BigDecimal.valueOf(4000.0)
                .subtract(BigDecimal.valueOf(2000.0)))
                .divide(BigDecimal.valueOf(2000.0), 10, RoundingMode.HALF_UP));
        expectedRanges.put("XRP", (BigDecimal.valueOf(1.5)
                .subtract(BigDecimal.valueOf(0.5)))
                .divide(BigDecimal.valueOf(0.5), 10, RoundingMode.HALF_UP));

        assertEquals(3L, result.get(0).coinId());
        assertEquals(expectedRanges.get("XRP"), result.get(0).normalizedRange());
        assertEquals(1L, result.get(1).coinId());
        assertEquals(expectedRanges.get("BTC"), result.get(1).normalizedRange());
        assertEquals(2L, result.get(2).coinId());
        assertEquals(expectedRanges.get("ETH"), result.get(2).normalizedRange());
    }

    @Test
    void testGetAllCoinsNormalizedRange_MinPriceZero() {
        List<CoinPriceStatsDto> coinPriceStats = Arrays.asList(
                new CoinPriceStatsDto(1L, BigDecimal.valueOf(60000.0), BigDecimal.valueOf(0.0)),
                new CoinPriceStatsDto(2L, BigDecimal.valueOf(4000.0), BigDecimal.valueOf(2000.0))
        );

        when(priceRepositoryService.findMaxMinPriceForAllCoins()).thenReturn(coinPriceStats);

        List<CoinNormalizedRangeDto> result = calculationService.getAllCoinsNormalizedRange();

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(2L, result.get(0).coinId());
        assertEquals((BigDecimal.valueOf(4000.0).subtract(BigDecimal.valueOf(2000.0))).divide(BigDecimal.valueOf(2000.0), 10, RoundingMode.HALF_UP), result.get(0).normalizedRange());
    }

    @Test
    void testGetCoinPriceInfo_Success() throws CoinISNotPresentInSystemException {
        String coin = "BTC";
        Long coinId = 1L;
        CoinEntity coinEntity = new CoinEntity(coinId, coin);
        PriceEntity oldestPrice =
                new PriceEntity(1L, 1622505600L, coinId, BigDecimal.valueOf(30000.0));
        PriceEntity newestPrice =
                new PriceEntity(2L, 1625097600L, coinId, BigDecimal.valueOf(60000.0));
        PriceEntity minPrice =
                new PriceEntity(3L, 1623801600L, coinId, BigDecimal.valueOf(25000.0));
        PriceEntity maxPrice =
                new PriceEntity(4L, 1624406400L, coinId, BigDecimal.valueOf(65000.0));

        when(coinRepositoryService.findByName(coin)).thenReturn(coinEntity);
        when(priceRepositoryService.findOldestPriceByCoin(coinId)).thenReturn(oldestPrice);
        when(priceRepositoryService.findNewestPriceByCoin(coinId)).thenReturn(newestPrice);
        when(priceRepositoryService.findMinPriceByCoin(coinId)).thenReturn(minPrice);
        when(priceRepositoryService.findMaxPriceByCoin(coinId)).thenReturn(maxPrice);

        CoinPriceInfoDto result = calculationService.getCoinPriceInfo(coin);

        assertNotNull(result);
        assertEquals(coinId, result.coinId());
        assertEquals(oldestPrice, result.oldestPrice());
        assertEquals(newestPrice, result.newestPrice());
        assertEquals(minPrice, result.minPrice());
        assertEquals(maxPrice, result.maxPrice());
    }

    @Test
    void testGetCoinPriceInfo_CoinNotFound() {
        String coin = "SuperUnknownCoin";

        CoinISNotPresentInSystemException exception = assertThrows(
                CoinISNotPresentInSystemException.class, () -> calculationService.getCoinPriceInfo(coin));

        assertEquals(exception.getCause(), new CoinISNotPresentInSystemException().getCause());
    }

    @Test
    void testGetCoinWithHighestNormalizedRangeForDay_Success() {
        LocalDate date = LocalDate.of(2021, 6, 1);
        List<CoinPriceStatsDto> coinPriceStats = Arrays.asList(
                new CoinPriceStatsDto(1L, BigDecimal.valueOf(60000.0), BigDecimal.valueOf(30000.0)),
                new CoinPriceStatsDto(2L, BigDecimal.valueOf(4000.0), BigDecimal.valueOf(2000.0)),
                new CoinPriceStatsDto(3L, BigDecimal.valueOf(1.5), BigDecimal.valueOf(0.5))
        );

        when(priceRepositoryService.findMaxMinPriceForAllCoinsOnDay(anyLong(), anyLong()))
                .thenReturn(coinPriceStats);

        CoinNormalizedRangeDto result = calculationService.getCoinWithHighestNormalizedRangeForDay(date);

        assertNotNull(result);
        assertEquals(3L, result.coinId());
        assertEquals(BigDecimal.valueOf(1.5)
                        .subtract(BigDecimal.valueOf(0.5))
                        .divide(BigDecimal.valueOf(0.5), 10, RoundingMode.HALF_UP),
                result.normalizedRange());
    }

    @Test
    void testGetCoinWithHighestNormalizedRangeForDay_NoData() {
        LocalDate date = LocalDate.of(2021, 6, 1);
        List<CoinPriceStatsDto> coinPriceStats = Collections.emptyList();

        when(priceRepositoryService.findMaxMinPriceForAllCoinsOnDay(anyLong(), anyLong()))
                .thenReturn(coinPriceStats);

        CoinNormalizedRangeDto result = calculationService.getCoinWithHighestNormalizedRangeForDay(date);

        assertNull(result);
    }

    @Test
    void testGetCoinPeriodStats_Success() throws CoinISNotPresentInSystemException {
        String coin = "BTC";
        Long coinId = 1L;
        CoinEntity coinEntity = new CoinEntity(coinId, coin);
        int months = 3;

        List<PriceEntity> pricesInPeriod = Arrays.asList(
                new PriceEntity(1L, 1622505600L, coinId, BigDecimal.valueOf(30000.0)),
                new PriceEntity(2L, 1622592000L, coinId, BigDecimal.valueOf(31000.0)),
                new PriceEntity(3L, 1622678400L, coinId, BigDecimal.valueOf(32000.0)),
                new PriceEntity(4L, 1622764800L, coinId, BigDecimal.valueOf(29000.0)),
                new PriceEntity(5L, 1622851200L, coinId, BigDecimal.valueOf(35000.0)),
                new PriceEntity(6L, 1622937600L, coinId, BigDecimal.valueOf(34000.0)),
                new PriceEntity(7L, 1623024000L, coinId, BigDecimal.valueOf(33000.0)),
                new PriceEntity(8L, 1623110400L, coinId, BigDecimal.valueOf(32000.0))
        );

        when(coinRepositoryService.findByName(coin)).thenReturn(coinEntity);
        when(priceRepositoryService.findAllPricesInPeriod(eq(coinId), any(Long.class), any(Long.class)))
                .thenReturn(pricesInPeriod);

        CoinPeriodStatsDto result = calculationService.getCoinPeriodStats(coin, months);

        assertNotNull(result);
        assertEquals(coinId, result.coinId());
        assertEquals(1622505600L, result.oldestPrice().getTimestamp());
        assertEquals(1623110400L, result.newestPrice().getTimestamp());
        assertEquals(BigDecimal.valueOf(29000.0), result.minPrice().getPrice());
        assertEquals(BigDecimal.valueOf(35000.0), result.maxPrice().getPrice());
    }


    @Test
    void testGetCoinPeriodStats_NoData() throws CoinISNotPresentInSystemException {
        String coin = "UnknownCoin";
        Long coinId = 98798798L;
        CoinEntity coinEntity = new CoinEntity(coinId, coin);

        int months = 3;

        when(coinRepositoryService.findByName(coin)).thenReturn(coinEntity);
        when(priceRepositoryService.findAllPricesInPeriod(eq(coinId), any(Long.class), any(Long.class)))
                .thenReturn(Collections.emptyList());

        CoinPeriodStatsDto result = calculationService.getCoinPeriodStats(coin, months);

        assertNull(result);
    }

    @Test
    void testGetCoinPeriodStats_InvalidMonths() throws CoinISNotPresentInSystemException {
        String coin = "BTC";
        Long coinId = 98797987L;
        int months = -1;

        CoinEntity coinEntity = new CoinEntity(coinId, coin);


        List<PriceEntity> pricesInPeriod = List.of(
                new PriceEntity(1L, 1622505600L, coinId, BigDecimal.valueOf(30000.0))
        );

        when(coinRepositoryService.findByName(coin)).thenReturn(coinEntity);
        when(priceRepositoryService.findAllPricesInPeriod(eq(coinId), anyLong(), anyLong()))
                .thenReturn(pricesInPeriod);

        CoinPeriodStatsDto result = calculationService.getCoinPeriodStats(coin, months);

        assertNotNull(result);
        assertEquals(coinId, result.coinId());
        assertEquals(1622505600L, result.oldestPrice().getTimestamp());
        assertEquals(1622505600L, result.newestPrice().getTimestamp());
        assertEquals(BigDecimal.valueOf(30000.0), result.minPrice().getPrice());
        assertEquals(BigDecimal.valueOf(30000.0), result.maxPrice().getPrice());
    }


    @Test
    void testGetCoinPriceInfoByPeriod_Success() throws CoinISNotPresentInSystemException {
        String coin = "BTC";
        Long coinId = 1L;
        CoinEntity coinEntity = new CoinEntity(coinId, coin);
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        Long startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
        Long endTimestamp = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();

        PriceEntity oldestPrice = new PriceEntity(1L, startTimestamp, coinId, BigDecimal.valueOf(10000.0));
        PriceEntity newestPrice = new PriceEntity(2L, endTimestamp, coinId, BigDecimal.valueOf(12000.0));
        PriceEntity minPrice = new PriceEntity(3L, startTimestamp + 1000, coinId, BigDecimal.valueOf(9000.0));
        PriceEntity maxPrice = new PriceEntity(4L, startTimestamp + 2000, coinId, BigDecimal.valueOf(13000.0));

        Mockito.when(coinRepositoryService.findByName(coin)).thenReturn(coinEntity);

        Mockito.when(priceRepositoryService.findOldestPriceByCoin(coinId, startTimestamp, endTimestamp))
                .thenReturn(oldestPrice);
        Mockito.when(priceRepositoryService.findNewestPriceByCoin(coinId, startTimestamp, endTimestamp))
                .thenReturn(newestPrice);
        Mockito.when(priceRepositoryService.findMinPriceByCoin(coinId, startTimestamp, endTimestamp))
                .thenReturn(minPrice);
        Mockito.when(priceRepositoryService.findMaxPriceByCoin(coinId, startTimestamp, endTimestamp))
                .thenReturn(maxPrice);

        CoinPriceInfoDto result = calculationService.getCoinPriceInfoByPeriod(coin, startDate, endDate);

        assertNotNull(result);
        assertEquals(coinId, result.coinId());
        assertEquals(oldestPrice, result.oldestPrice());
        assertEquals(newestPrice, result.newestPrice());
        assertEquals(minPrice, result.minPrice());
        assertEquals(maxPrice, result.maxPrice());
    }

    @Test
    void testGetCoinPriceInfoByPeriod_CoinNotFound() {
        String coin = "UnknownCoin";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        CoinISNotPresentInSystemException exception = assertThrows(
                CoinISNotPresentInSystemException.class,
                () -> calculationService.getCoinPriceInfoByPeriod(coin, startDate, endDate));


        assertEquals(exception.getCause(), new CoinISNotPresentInSystemException().getCause());
    }

    @Test
    void testGetCoinPriceInfoByPeriod_NoData() throws CoinISNotPresentInSystemException {
        String coin = "BTC";
        Long coinId = 1L;
        CoinEntity coinEntity = new CoinEntity(coinId, coin);
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        Long startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
        Long endTimestamp = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();

        Mockito.when(coinRepositoryService.findByName(coin)).thenReturn(coinEntity);
        Mockito.when(priceRepositoryService.findOldestPriceByCoin(coinId, startTimestamp, endTimestamp))
                .thenReturn(null);
        Mockito.when(priceRepositoryService.findNewestPriceByCoin(coinId, startTimestamp, endTimestamp))
                .thenReturn(null);
        Mockito.when(priceRepositoryService.findMinPriceByCoin(coinId, startTimestamp, endTimestamp))
                .thenReturn(null);
        Mockito.when(priceRepositoryService.findMaxPriceByCoin(coinId, startTimestamp, endTimestamp))
                .thenReturn(null);

        CoinPriceInfoDto result = calculationService.getCoinPriceInfoByPeriod(coin, startDate, endDate);
        assertNull(result);
    }
}
