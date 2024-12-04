package com.epam.cryptoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CryptoserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoserviceApplication.class, args);
	}

}
