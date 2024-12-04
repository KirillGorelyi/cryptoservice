package com.epam.cryptoservice.schema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "price")
@AllArgsConstructor
@RequiredArgsConstructor
public class PriceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "timestamp", nullable = false)
    Long timestamp;
    @Column(name = "coin_id", nullable = false)
    Long coinId;
    @Column(name = "price", nullable = false)
    BigDecimal price;

}
