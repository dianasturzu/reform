package com.reform.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;
import java.util.stream.Stream;

import com.reform.entities.Product;
import com.reform.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionParserService {

    private final ProductRepository productRepository;

    // ✅ Regex for "Start abonament X sedinte Y:Z" and "Sedinta Y:Z"
    private static final Pattern SUBSCRIPTION_PATTERN = Pattern.compile(".*\\bStart abonament (\\d+) sedinte (\\d+):(\\d+)\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern SINGLE_SESSION_PATTERN = Pattern.compile(".*\\bSedinta (\\d+):(\\d+)\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern BARTER_SESSION_PATTERN = Pattern.compile(".*\\bSedinta barter\\b.*", Pattern.CASE_INSENSITIVE);


    public Optional<Product> extractProductFromEventDescription(String description) {
        if (description == null || description.isEmpty()) {
            return Optional.empty();
        }

        return Stream.of(SUBSCRIPTION_PATTERN, SINGLE_SESSION_PATTERN, BARTER_SESSION_PATTERN)
                .map(pattern -> pattern.matcher(description))
                .filter(Matcher::find)
                .map(matcher -> productRepository.findByName(matcher.group(0)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
