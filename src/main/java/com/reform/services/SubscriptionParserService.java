package com.reform.services;

import java.text.Normalizer;
import java.util.Locale;
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

        // Normalize description (remove diacritics and lowercase)
        String normalizedDescription = normalizeText(description);

        Pattern[] patterns = {SUBSCRIPTION_PATTERN, SINGLE_SESSION_PATTERN, BARTER_SESSION_PATTERN};

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(normalizedDescription);
            if (matcher.find()) { // Ensure a match is found before extracting

                String extractedName;
                if (pattern == SUBSCRIPTION_PATTERN) {
                    extractedName = "abonament " + matcher.group(1) + " sedinte " + matcher.group(2) + ":" + matcher.group(3);
                } else if (pattern == SINGLE_SESSION_PATTERN) {
                    extractedName = "sedinta " + matcher.group(1) + ":" + matcher.group(2);
                } else { // BARTER_SESSION_PATTERN
                    extractedName = "sedinta barter";
                }

//                System.out.println("Extracted product name: " + extractedName);

                Optional<Product> product = productRepository.findByName(extractedName);
                if (product.isPresent()) {
                    return product; // Return the first found product
                }
            }
        }

        return Optional.empty(); // No match found
    }

    // Helper method to normalize text (remove diacritics and make lowercase)
    private static String normalizeText(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)  // Decomposes accents
                .replaceAll("\\p{M}", "") // Removes diacritical marks
                .toLowerCase(Locale.ROOT) // Convert to lowercase
                .trim();
    }
}
