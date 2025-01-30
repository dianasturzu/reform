package com.reform.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;

import com.reform.entities.Product;
import com.reform.entities.TokenType;
import com.reform.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionParserService {

    private final ProductRepository productRepository;

    // ✅ Regex for "Start abonament X sedinte Y:Z" and "Sedinta Y:Z"
    private static final Pattern SUBSCRIPTION_PATTERN = Pattern.compile("Start abonament (\\d+) sedinte (\\d+):(\\d+)");
    private static final Pattern SINGLE_SESSION_PATTERN = Pattern.compile("Sedinta (\\d+):(\\d+)");
    private static final Pattern BARTER_SESSION_PATTERN = Pattern.compile("Sedinta barter", Pattern.CASE_INSENSITIVE);

    public Optional<Product> extractSubscription(String description) {
        if (description == null || description.isEmpty()) {
            return Optional.empty();
        }

        // ✅ Handle "Start abonament X sedinte Y:Z" (Subscription)
        Matcher subscriptionMatcher = SUBSCRIPTION_PATTERN.matcher(description);
        if (subscriptionMatcher.find()) {
            int sessionCount = Integer.parseInt(subscriptionMatcher.group(1));
            TokenType tokenType = mapToTokenType(subscriptionMatcher.group(2), subscriptionMatcher.group(3));

            return tokenType != null ? productRepository.findByTokenQtyAndTokenType(sessionCount, tokenType) : Optional.empty();
        }

        // ✅ Handle "Sedinta Y:Z" (Single Session)
        Matcher singleSessionMatcher = SINGLE_SESSION_PATTERN.matcher(description);
        if (singleSessionMatcher.find()) {
            TokenType tokenType = mapToTokenType(singleSessionMatcher.group(1), singleSessionMatcher.group(2));

            return tokenType != null ? productRepository.findByTokenQtyAndTokenType(1, tokenType) : Optional.empty();
        }

        // ✅ Handle "Sedinta barter" (Barter Session)
        Matcher barterMatcher = BARTER_SESSION_PATTERN.matcher(description);
        if (barterMatcher.find()) {
            return productRepository.findByTokenQtyAndTokenType(1, TokenType.BARTER);
        }

        return Optional.empty();
    }

    private TokenType mapToTokenType(String type, String subType) {
        String formattedType = type + ":" + subType;
        return switch (formattedType) {
            case "1:1" -> TokenType.ONE_ON_ONE;
            case "1:2" -> TokenType.ONE_ON_TWO;
            default -> null;
        };
    }
}
