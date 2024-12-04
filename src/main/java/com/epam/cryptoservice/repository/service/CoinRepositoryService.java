package com.epam.cryptoservice.repository.service;

import com.epam.cryptoservice.repository.CoinRepository;
import com.epam.cryptoservice.schema.entity.CoinEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoinRepositoryService {
    private final CoinRepository coinRepository;

    public CoinEntity findByName(String name) {
        return coinRepository.findByName(name).orElse(null);
    }

    public CoinEntity findById(Long coinId) {
        return coinRepository.findById(coinId).orElse(null);
    }

    public CoinEntity saveCoin(String coinName) {
        return coinRepository.save(new CoinEntity(null, coinName));
    }
}
