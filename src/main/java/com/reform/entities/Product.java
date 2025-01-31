package com.reform.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price;
    private int tokenQty; // Number of tokens included

    @Enumerated(EnumType.STRING)
    private TokenType tokenType; // 1:1, 1:2, BARTER

    @Enumerated(EnumType.STRING)
    private TokenExpirePolicy tokenExpirePolicy; // Expiration rule for tokens

    private LocalDate availableFrom;

    private LocalDate availableUntil;

}
