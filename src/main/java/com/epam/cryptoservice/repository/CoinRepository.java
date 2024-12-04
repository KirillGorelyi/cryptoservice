package com.epam.cryptoservice.repository;

import com.epam.cryptoservice.schema.entity.CoinEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoinRepository extends CrudRepository<CoinEntity, Long> {
    @Query("select ce from CoinEntity ce where ce.name = :name ")
    Optional<CoinEntity> findByName(String name);
}
