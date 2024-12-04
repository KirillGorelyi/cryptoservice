package com.epam.cryptoservice.integration.repository;

import com.epam.cryptoservice.repository.PriceRepository;
import com.epam.cryptoservice.schema.dto.CoinPriceStatsDto;
import com.epam.cryptoservice.schema.entity.PriceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BaseRepositoryConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PriceRepositoryTest {

    @Autowired
    private PriceRepository priceRepository;

    @Test
    void testFindByCoinId() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        List<PriceEntity> result = priceRepository.findByCoinId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCoinId()).isEqualTo(1L);
    }

    @Test
    void testFindMaxMinPriceForAllCoins() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        PriceEntity price3 = new PriceEntity(null, 1622505900L, 2L, BigDecimal.valueOf(2000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);
        priceRepository.save(price3);

        List<CoinPriceStatsDto> stats = priceRepository.findMaxMinPriceForAllCoins();

        assertThat(stats).hasSize(2);
        assertThat(stats).anySatisfy(stat -> {
            if (stat.coinId().equals(1L)) {
                assertThat(stat.maxPrice()).isEqualTo(BigDecimal.valueOf(31000.0));
                assertThat(stat.minPrice()).isEqualTo(BigDecimal.valueOf(30000.0));
            }
        });
    }

    @Test
    void testFindAllPricesInPeriod() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1625097600L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        Long startTimestamp = 1622505600L;
        Long endTimestamp = 1625097600L;

        List<PriceEntity> result = priceRepository.findAllPricesInPeriod(startTimestamp, endTimestamp);

        assertThat(result).hasSize(2);
    }

    @Test
    void testFindDistinctCoins() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 2L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        List<String> distinctCoins = priceRepository.findDistinctCoins();

        assertThat(distinctCoins).hasSize(2);
        assertThat(distinctCoins).containsExactlyInAnyOrder("1", "2");
    }


    @Test
    void testFindByCoinAndTimestampList() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        List<PriceEntity> result = priceRepository.findByCoinAndTimestampList(1L, Arrays.asList(1622505600L, 1622505800L));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PriceEntity::getTimestamp).containsExactlyInAnyOrder(1622505600L, 1622505800L);
    }

    @Test
    void testFindByCoinAndBeforeTimestamp() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        List<PriceEntity> result = priceRepository.findByCoinAndBeforeTimestamp(1L, 1622505700L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTimestamp()).isEqualTo(1622505600L);
    }

    @Test
    void testFindByCoinAndAfterTimestamp() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        List<PriceEntity> result = priceRepository.findByCoinAndAfterTimestamp(1L, 1622505700L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTimestamp()).isEqualTo(1622505800L);
    }

    @Test
    void testFindByCoinAndPeriod() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        List<PriceEntity> result = priceRepository.findByCoinAndPeriod(1L, 1622505500L, 1622505900L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PriceEntity::getTimestamp).containsExactlyInAnyOrder(1622505600L, 1622505800L);
    }

    @Test
    void testFindMinPriceByCoin() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        PriceEntity result = priceRepository.findMinPriceByCoin(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(30000.0));
    }

    @Test
    void testFindMaxPriceByCoin() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        PriceEntity result = priceRepository.findMaxPriceByCoin(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(31000.0));
    }

    @Test
    void testFindOldestPriceByCoin() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        PriceEntity result = priceRepository.findOldestPriceByCoin(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTimestamp()).isEqualTo(1622505600L);
    }

    @Test
    void testFindNewestPriceByCoin() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622505800L, 1L, BigDecimal.valueOf(31000.0));
        priceRepository.save(price1);
        priceRepository.save(price2);

        PriceEntity result = priceRepository.findNewestPriceByCoin(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTimestamp()).isEqualTo(1622505800L);
    }

    @Test
    void testFindMaxMinPriceForAllCoinsOnDay() {
        PriceEntity price1 = new PriceEntity(null, 1622505600L, 1L, BigDecimal.valueOf(30000.0));
        PriceEntity price2 = new PriceEntity(null, 1622592000L, 1L, BigDecimal.valueOf(31000.0));
        BigDecimal expected = BigDecimal.valueOf(30000.0).setScale(2, RoundingMode.HALF_UP);
        priceRepository.save(price1);
        priceRepository.save(price2);

        Long startTimestamp = 1622505600L;
        Long endTimestamp = 1622591999L;
        List<CoinPriceStatsDto> result = priceRepository.findMaxMinPriceForAllCoinsOnDay(startTimestamp, endTimestamp);


        assertThat(result).hasSize(1);
        assertThat(result.get(0).coinId()).isEqualTo(1L);
        assertThat(result.get(0).maxPrice()).isEqualTo(expected);
    }
}
