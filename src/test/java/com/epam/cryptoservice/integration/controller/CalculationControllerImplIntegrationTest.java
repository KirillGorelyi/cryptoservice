package com.epam.cryptoservice.integration.controller;

import com.epam.cryptoservice.integration.repository.BaseRepositoryConfig;
import com.epam.cryptoservice.repository.CoinRepository;
import com.epam.cryptoservice.repository.PriceRepository;
import com.epam.cryptoservice.schema.dto.CoinNormalizedRangeDto;
import com.epam.cryptoservice.schema.dto.CoinPriceInfoDto;
import com.epam.cryptoservice.schema.entity.CoinEntity;
import com.epam.cryptoservice.schema.entity.PriceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(BaseRepositoryConfig.class)
public class CalculationControllerImplIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private CoinRepository coinRepository;

    @BeforeEach
    public void setUp() {
        priceRepository.deleteAll();

        Optional<CoinEntity> btcEntity = coinRepository.findByName("BTC");
        Optional<CoinEntity> ethEntity = coinRepository.findByName("ETH");

        PriceEntity price1 = new PriceEntity(null,
                1622505600L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(30000.0));
        PriceEntity price1_1 = new PriceEntity(null,
                1622505700L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(38000.0));
        PriceEntity price2 = new PriceEntity(null,
                1622592000L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(35000.0));
        PriceEntity price3 = new PriceEntity(null,
                1622678400L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(32000.0));
        PriceEntity price4 = new PriceEntity(null,
                1622764800L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(29000.0));
        PriceEntity price5 = new PriceEntity(null,
                1622851200L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(35000.0));
        PriceEntity price6 = new PriceEntity(null,
                1622937600L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(34000.0));
        PriceEntity price7 = new PriceEntity(null,
                1623024000L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(33000.0));
        PriceEntity price8 = new PriceEntity(null,
                1623110400L,
                btcEntity.get().getId(),
                BigDecimal.valueOf(32000.0));

        PriceEntity ethPrice1 = new PriceEntity(null,
                1622505600L,
                ethEntity.get().getId(),
                BigDecimal.valueOf(2000.0));
        PriceEntity ethPrice2 = new PriceEntity(null,
                1622592000L,
                ethEntity.get().getId(),
                BigDecimal.valueOf(2100.0));

        priceRepository.saveAll(Arrays.asList(
                price1,
                price1_1,
                price2,
                price3,
                price4,
                price5,
                price6,
                price7,
                price8,
                ethPrice1,
                ethPrice2));
    }

    @Test
    public void testGetAllCoinsNormalizedRange() {
        String url = "http://localhost:" + port + "/api/stats/all/normalized-range";

        ResponseEntity<CoinNormalizedRangeDto[]> response = restTemplate.getForEntity(
                url,
                CoinNormalizedRangeDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CoinNormalizedRangeDto[] coinRanges = response.getBody();
        assertNotNull(coinRanges);
        assertEquals(2, coinRanges.length);

        assertEquals(4L, coinRanges[0].coinId());
        assertEquals(1L, coinRanges[1].coinId());
    }

    @Test
    public void testGetCoinPriceInfo() {
        String coin = "BTC";
        String url = "http://localhost:" + port + "/api/stats/" + coin + "/info";

        ResponseEntity<CoinPriceInfoDto> response =
                restTemplate.getForEntity(url, CoinPriceInfoDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CoinPriceInfoDto coinPriceInfo = response.getBody();
        assertNotNull(coinPriceInfo);
        assertEquals(4L, coinPriceInfo.coinId());
        assertEquals(0, BigDecimal.valueOf(30000.0)
                .compareTo(coinPriceInfo.oldestPrice().getPrice()));
        assertEquals(0, BigDecimal.valueOf(32000.0)
                .compareTo(coinPriceInfo.newestPrice().getPrice()));
    }

    @Test
    public void testGetCoinWithHighestNormalizedRangeForDay_Success() {
        LocalDate date = LocalDate.of(2021, 6, 1);
        String url = "http://localhost:" + port
                + "/api/stats/highest-normalized-range?date=" + date;

        ResponseEntity<CoinNormalizedRangeDto> response = restTemplate.getForEntity(
                url, CoinNormalizedRangeDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        CoinNormalizedRangeDto result = response.getBody();
        assertNotNull(result);
        assertEquals(4L, result.coinId());

        BigDecimal maxPrice = BigDecimal.valueOf(38000.0);
        BigDecimal minPrice = BigDecimal.valueOf(30000.0);
        BigDecimal expectedNormalizedRange = maxPrice.subtract(minPrice)
                .divide(minPrice, 10, RoundingMode.HALF_UP);
        assertEquals(expectedNormalizedRange, result.normalizedRange());
    }

    @Test
    public void testGetCoinWithHighestNormalizedRangeForDay_NotFound() {
        LocalDate date = LocalDate.of(2020, 1, 1);
        String url = "http://localhost:" + port +
                "/api/stats/highest-normalized-range?date=" + date;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetCoinPeriodStats_CoinNotFound() {
        String coin = "UnknownCoin";
        int months = 3;

        String url = "http://localhost:" + port + "/api/stats/period-stats?" +
                "coin=" + coin + "&months=" + months;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetCoinPriceInfoForThePeriod_Success() {
        String coin = "BTC";
        Long coinId = 4L;
        LocalDate startDate = LocalDate.of(2021, 6, 1);
        LocalDate endDate = LocalDate.of(2021, 6, 3);

        String url = "http://localhost:" + port + "/api/stats/"
                + coin + "/info/in-period?startDate=" + startDate
                + "&endDate=" + endDate;

        ResponseEntity<CoinPriceInfoDto> response =
                restTemplate.getForEntity(url, CoinPriceInfoDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        CoinPriceInfoDto coinPriceInfo = response.getBody();
        assertEquals(coinId, coinPriceInfo.coinId());
        assertEquals(0, BigDecimal.valueOf(30000.0)
                .compareTo(coinPriceInfo.oldestPrice().getPrice()));
        assertEquals(0, BigDecimal.valueOf(35000.0)
                .compareTo(coinPriceInfo.newestPrice().getPrice()));
    }
}
