package com.reform.services;

import com.reform.entities.Product;
import com.reform.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService
{
    private ProductRepository productRepository;

    public Product getProductPriceAtTheStartOfSubscription(String productName, LocalDate subscriptionStartDate) {
        return productRepository.findValidProductForDate(productName, subscriptionStartDate).orElseThrow(() -> new IllegalArgumentException("No price found for " + productName + " at " + subscriptionStartDate));

    }
}
