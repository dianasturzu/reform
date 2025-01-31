package com.reform.services;

import com.reform.entities.Product;
import com.reform.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ProductService
{
    private ProductRepository productRepository;

    public Optional<Product> getProductPriceAtTheStartOfSubscription(String productName, LocalDate subscriptionStartDate) {
        return productRepository.findValidProductForDate(productName, subscriptionStartDate);
    }
}
