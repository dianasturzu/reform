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

    public enum TokenType {
        ONE_ON_ONE, // 1:1
        ONE_ON_TWO, // 1:2
        BARTER      // Special barter sessions
    }

    public enum TokenExpirePolicy {
        NONE,       // No expiration (e.g., single sessions)
        ONE_MONTH,  // Expires 1 month after first use
        TWO_MONTHS  // Expires 2 months after first use
    }
}
