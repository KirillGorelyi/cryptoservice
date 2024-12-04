package com.epam.cryptoservice.repository.service;

import com.epam.cryptoservice.repository.PriceRepository;
import com.epam.cryptoservice.schema.dto.CoinPriceStatsDto;
import com.epam.cryptoservice.schema.entity.PriceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PriceRepositoryService {
    private static final Object lock = new Object();
    private final PriceRepository priceRepository;

    public void saveAllAvoidExisting(List<PriceEntity> listToSave) {
        Map<Long, List<Long>> coinToTimestampsMap = listToSave.stream()
                .collect(Collectors.groupingBy(
                        PriceEntity::getCoinId,
                        Collectors.mapping(PriceEntity::getTimestamp,
                                Collectors.toList())));
        synchronized (lock) {
            List<PriceEntity> entities = new ArrayList<>();
            for (Map.Entry<Long, List<Long>> entry : coinToTimestampsMap.entrySet()) {
                Set<Long> existingTimestamps = priceRepository.findByCoinAndTimestampList(
                                entry.getKey(),
                                entry.getValue()
                        ).stream()
                        .map(PriceEntity::getTimestamp)
                        .collect(Collectors.toSet());
                if (existingTimestamps.isEmpty()) {
                    entities.addAll(listToSave);
                    continue;
                }

                Map<Boolean, List<PriceEntity>> partitioned = listToSave.stream()
                        .collect(Collectors.partitioningBy(
                                priceEntity ->
                                        existingTimestamps.contains(priceEntity.getTimestamp())
                        ));
                listToSave.addAll(partitioned.get(false));
            }
            saveAll(entities);
        }
    }


    public void saveAll(List<PriceEntity> listToSave) {
        try {
            StreamSupport
                    .stream(priceRepository.saveAll(listToSave).spliterator(), false);
        } catch (Exception ex) {
            throw new RuntimeException("Something went wrong, " + ex.getMessage());
        }

    }

    public List<CoinPriceStatsDto> findMaxMinPriceForAllCoins() {
        return priceRepository.findMaxMinPriceForAllCoins();
    }

    public PriceEntity findOldestPriceByCoin(Long coinId) {
        return priceRepository.findOldestPriceByCoin(coinId);
    }

    public PriceEntity findNewestPriceByCoin(Long coinId) {
        return priceRepository.findNewestPriceByCoin(coinId);
    }

    public PriceEntity findMinPriceByCoin(Long coinId) {
        return priceRepository.findMinPriceByCoin(coinId);
    }

    public PriceEntity findMaxPriceByCoin(Long coinId) {
        return priceRepository.findMaxPriceByCoin(coinId);
    }

    public PriceEntity findOldestPriceByCoin(Long coinId,
                                             Long startTimestamp,
                                             Long endTimestamp) {
        return priceRepository.findOldestPriceByCoin(coinId, startTimestamp, endTimestamp);
    }

    public PriceEntity findNewestPriceByCoin(Long coinId,
                                             Long startTimestamp,
                                             Long endTimestamp) {
        return priceRepository.findNewestPriceByCoin(coinId, startTimestamp, endTimestamp);
    }

    public PriceEntity findMinPriceByCoin(Long coinId,
                                          Long startTimestamp,
                                          Long endTimestamp) {
        return priceRepository.findMinPriceByCoin(coinId, startTimestamp, endTimestamp);
    }

    public PriceEntity findMaxPriceByCoin(Long coinId,
                                          Long startTimestamp,
                                          Long endTimestamp) {
        return priceRepository.findMaxPriceByCoin(coinId, startTimestamp, endTimestamp);
    }

    public List<CoinPriceStatsDto> findMaxMinPriceForAllCoinsOnDay(Long startTimestamp,
                                                                   Long endTimestamp) {
        return priceRepository.findMaxMinPriceForAllCoinsOnDay(startTimestamp, endTimestamp);
    }

    public List<PriceEntity> findAllPricesInPeriod(Long coinId,
                                                   Long startTimestamp,
                                                   Long endTimestamp) {
        return priceRepository.findByCoinAndPeriod(coinId, startTimestamp, endTimestamp);
    }
}
