package com.epam.cryptoservice;

import com.epam.cryptoservice.integration.repository.BaseRepositoryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EntityScan(basePackages = {"com.epam.cryptoservice.schema.entity"})
@Import(BaseRepositoryConfig.class)
class CryptoserviceApplicationTests {

    @Test
    void contextLoads() {
    }

}
