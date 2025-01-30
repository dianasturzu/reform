package com.reform.services;

import com.reform.entities.Client;
import com.reform.entities.Product;
import com.reform.entities.Token;
import com.reform.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;

    // 🔹 Generate Tokens When a New Subscription is Purchased
    public void generateTokensForClient(Client client, Product product) {
        List<Token> tokens = IntStream.range(0, product.getTokenQty())
                .mapToObj(i -> Token.builder()
                        .client(client)
                        .product(product)
                        .tokenValue(product.getPrice() / product.getTokenQty()) // ✅ Set token price
                        .expiresAt(calculateExpiryDate(product)) // ✅ Expiry based on policy
                        .build()
                )
                .toList();

        tokenRepository.saveAll(tokens);
    }

    // 🔹 Deduct a Token When a Session is Held
    public boolean deductToken(Client client, LocalDate sessionDate) {
        return tokenRepository.findUnexpiredTokens(client, sessionDate).stream()
                .filter(token -> isValidForUse(token, sessionDate)) // ✅ Ensure valid token
                .findFirst()
                .map(token -> {
                    token.setUsedAt(sessionDate);
                    log.info("Session recorded for client {} on {}. Token deducted. Price: {} RON",
                            client.getName(), sessionDate, token.getTokenValue());
                    tokenRepository.save(token);
                    return true;
                })
                .orElse(false);
    }

    // 🔹 Check if Token is Still Valid
    private boolean isValidForUse(Token token, LocalDate sessionDate) {
        return (token.getProduct().getAvailableFrom() == null || !sessionDate.isAfter(token.getProduct().getAvailableUntil()));
    }

    // 🔹 Calculate Expiry Date Based on Subscription Type
    private LocalDate calculateExpiryDate(Product product) {
        return switch (product.getTokenExpirePolicy()) {
            case ONE_MONTH -> LocalDate.now().plusMonths(1);
            case TWO_MONTHS -> LocalDate.now().plusMonths(2);
            default -> null;
        };
    }

    /**
     * Checks if a client has at least one available token.
     */
    public boolean hasAvailableTokens(Client client) {
        return tokenRepository.countAvailableTokens(client) > 0;
    }
}
