package com.epam.cryptoservice.integration.repository;

import com.epam.cryptoservice.repository.CoinRepository;
import com.epam.cryptoservice.schema.entity.CoinEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BaseRepositoryConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CoinRepositoryTest {

    @Autowired
    private CoinRepository coinRepository;

    @Test
    void testFindByName_ExistingCoin() {
        CoinEntity coin = new CoinEntity(null, "BTC");
        coinRepository.save(coin);

        Optional<CoinEntity> result = coinRepository.findByName("BTC");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("BTC");
    }

    @Test
    void testFindByName_NonExistingCoin() {
        CoinEntity coin = new CoinEntity(null, "ETH");
        coinRepository.save(coin);


        Optional<CoinEntity> result = coinRepository.findByName("BTC");
        assertThat(result).isNotPresent();
    }

    @Test
    void testFindByName_MultipleCoins() {
        coinRepository.save(new CoinEntity(null, "BTC"));
        coinRepository.save(new CoinEntity(null, "ETH"));

        Optional<CoinEntity> result = coinRepository.findByName("ETH");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ETH");
    }
}
