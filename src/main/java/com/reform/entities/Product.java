package com.reform.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Token> tokens = new ArrayList<>();

    public Product(String name, Double price, int tokenQty, TokenType tokenType, TokenExpirePolicy tokenExpirePolicy) {
        this.name = name;
        this.price = price;
        this.tokenQty = tokenQty;
        this.tokenType = tokenType;
        this.tokenExpirePolicy = tokenExpirePolicy;
    }
}
