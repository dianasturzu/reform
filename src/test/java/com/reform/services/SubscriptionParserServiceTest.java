package com.reform.services;

import com.reform.entities.Product;
import com.reform.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionParserServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SubscriptionParserService subscriptionParserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldExtractSubscriptionProduct() {
        String description = "Some random text Start abonament 10 sedinte 1:1 with more text";

        Product mockProduct = new Product();
        mockProduct.setName("abonament 10 sedinte 1:1");

        when(productRepository.findByName("abonament 10 sedinte 1:1")).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = subscriptionParserService.extractProductFromEventDescription(description);

        assertTrue(result.isPresent(), "Expected product to be found");
        assertEquals(mockProduct, result.get());
    }

    @Test
    void shouldExtractSingleSessionProduct() {
        String description = "Some text before Sedinta 1:2 and some text after";

        Product mockProduct = new Product();
        mockProduct.setName("Sedinta 1:2");

        when(productRepository.findByName("sedinta 1:2")).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = subscriptionParserService.extractProductFromEventDescription(description);

        assertTrue(result.isPresent(), "Expected single session product to be found");
        assertEquals(mockProduct, result.get());
    }

    @Test
    void shouldExtractBarterSessionProduct() {
        String description = "Some other text Sedinta barter more text after";

        Product mockProduct = new Product();
        mockProduct.setName("Sedinta barter");

        when(productRepository.findByName("sedinta barter")).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = subscriptionParserService.extractProductFromEventDescription(description);

        assertTrue(result.isPresent(), "Expected barter session product to be found");
        assertEquals(mockProduct, result.get());
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        String description = "Random text with no subscription or session info";

        Optional<Product> result = subscriptionParserService.extractProductFromEventDescription(description);

        assertTrue(result.isEmpty(), "Expected no product to be found");
    }

    @Test
    void shouldExtractSubscriptionProduct_WithDiacritics() {
        String descriptionWithDiacritics = "Start abonament 10 ședințe 1:1";

        Product mockProduct = new Product();
        mockProduct.setName("abonament 10 sedinte 1:1");

        when(productRepository.findByName("abonament 10 sedinte 1:1")).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = subscriptionParserService.extractProductFromEventDescription(descriptionWithDiacritics);

        assertTrue(result.isPresent(), "Expected product to be found despite diacritics");
        assertEquals(mockProduct, result.get());
    }
}
