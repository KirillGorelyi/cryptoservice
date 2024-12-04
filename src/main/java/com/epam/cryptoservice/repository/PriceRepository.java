package com.epam.cryptoservice.repository;

import com.epam.cryptoservice.schema.dto.CoinPriceStatsDto;
import com.epam.cryptoservice.schema.entity.PriceEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends CrudRepository<PriceEntity, Long> {

    List<PriceEntity> findByCoinId(Long coinId);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId= :coinId and pe.timestamp in (:timestampList)")
    List<PriceEntity> findByCoinAndTimestampList(@Param("coinId") Long coinId,
                                                 List<Long> timestampList);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId= :coinId and pe.timestamp < :timestampBefore")
    List<PriceEntity> findByCoinAndBeforeTimestamp(@Param("coinId") Long coinId,
                                                   @Param("timestampBefore") Long timestampBefore);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId= :coinId and pe.timestamp > :timestampAfter")
    List<PriceEntity> findByCoinAndAfterTimestamp(@Param("coinId") Long coinId,
                                                  @Param("timestampAfter") Long timestampAfter);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId= :coinId " +
            "and pe.timestamp > :startTimestamp " +
            "and pe.timestamp < :endTimestamp")
    List<PriceEntity> findByCoinAndPeriod(
            @Param("coinId") Long coinId,
            @Param("startTimestamp") Long timestampBefore,
            @Param("endTimestamp") Long timestampAfter);

    @Query("select DISTINCT pe.coinId from PriceEntity pe")
    List<String> findDistinctCoins();


    @Query("select pe from PriceEntity pe " +
            "where pe.coinId = :coinId " +
            "and pe.price = " +
            "(select min(pe2.price) from PriceEntity pe2 where pe2.coinId = :coinId)")
    PriceEntity findMinPriceByCoin(@Param("coinId") Long coinId);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId = :coinId " +
            "and pe.timestamp > :startTimestamp " +
            "and pe.timestamp < :endTimestamp " +
            "and pe.price = " +
            "(select min(pe2.price) from PriceEntity pe2 where pe2.coinId = :coinId)")
    PriceEntity findMinPriceByCoin(@Param("coinId") Long coinId,
                                   @Param("startTimestamp") Long startTimestamp,
                                   @Param("endTimestamp") Long endTimestamp);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId = :coinId " +
            "and pe.price = (select max(pe2.price) " +
            "from PriceEntity pe2 where pe2.coinId = :coinId)")
    PriceEntity findMaxPriceByCoin(@Param("coinId") Long coinId);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId = :coinId " +
            "and pe.timestamp > :startTimestamp " +
            "and pe.timestamp < :endTimestamp " +
            "and pe.price = (select max(pe2.price) " +
            "from PriceEntity pe2 where pe2.coinId = :coinId)")
    PriceEntity findMaxPriceByCoin(@Param("coinId") Long coinId,
                                   @Param("startTimestamp") Long startTimestamp,
                                   @Param("endTimestamp") Long endTimestamp);


    @Query("select pe from PriceEntity pe " +
            "where pe.coinId = :coinId order by pe.timestamp ASC LIMIT 1")
    PriceEntity findOldestPriceByCoin(@Param("coinId") Long coinId);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId = :coinId " +
            "and pe.timestamp > :startTimestamp " +
            "and pe.timestamp < :endTimestamp " +
            "order by pe.timestamp ASC LIMIT 1")
    PriceEntity findOldestPriceByCoin(@Param("coinId") Long coinId,
                                      @Param("startTimestamp") Long startTimestamp,
                                      @Param("endTimestamp") Long endTimestamp);


    @Query("select pe from PriceEntity pe " +
            "where pe.coinId = :coinId order by pe.timestamp DESC LIMIT 1")
    PriceEntity findNewestPriceByCoin(@Param("coinId") Long coinId);

    @Query("select pe from PriceEntity pe " +
            "where pe.coinId = :coinId " +
            "and pe.timestamp > :startTimestamp " +
            "and pe.timestamp < :endTimestamp " +
            "order by pe.timestamp DESC LIMIT 1")
    PriceEntity findNewestPriceByCoin(@Param("coinId") Long coinId,
                                      @Param("startTimestamp") Long startTimestamp,
                                      @Param("endTimestamp") Long endTimestamp);


    @Query("select new com.epam.cryptoservice.schema.dto.CoinPriceStatsDto(" +
            "pe.coinId, " +
            "max(pe.price), " +
            "min(pe.price)) " +
            "from PriceEntity pe group by pe.coinId")
    List<CoinPriceStatsDto> findMaxMinPriceForAllCoins();

    @Query("select new com.epam.cryptoservice.schema.dto.CoinPriceStatsDto(" +
            "pe.coinId, " +
            "max(pe.price)," +
            " min(pe.price)) " +
            "from PriceEntity pe " +
            "where pe.timestamp >= :startTimestamp and pe.timestamp <= :endTimestamp " +
            "group by pe.coinId")
    List<CoinPriceStatsDto> findMaxMinPriceForAllCoinsOnDay(
            @Param("startTimestamp") Long startTimestamp,
            @Param("endTimestamp") Long endTimestamp);

    @Query("select pe from PriceEntity pe " +
            "where pe.timestamp >= :startTimestamp " +
            "and pe.timestamp <= :endTimestamp")
    List<PriceEntity> findAllPricesInPeriod(@Param("startTimestamp") Long startTimestamp,
                                            @Param("endTimestamp") Long endTimestamp);
}
