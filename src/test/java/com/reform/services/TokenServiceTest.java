package com.reform.services;

import com.reform.entities.*;
import com.reform.repositories.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private Client testClient;
    private Product testProduct;
    private Token testToken;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setName("John Doe");

        testProduct = new Product();
        testProduct.setName("Abonament 10 sedinte 1:1");
        testProduct.setPrice(500.0);
        testProduct.setTokenQty(10);
        testProduct.setAvailableUntil(LocalDate.now().plusMonths(2));

        testToken = Token.TokenBuilder.aToken()
                .withClient(testClient)
                .withProduct(testProduct)
                .withTokenValue(testProduct.getPrice() / testProduct.getTokenQty())
                .withExpiresAt(testProduct.getAvailableUntil())
                .withStatus(TokenStatus.AVAILABLE)
                .build();
    }

    @Test
    void shouldGenerateTokensForClient() {
        tokenService.generateTokensForClient(testClient, testProduct);

        assertEquals(10, testClient.getTokens().size());
        verify(tokenRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldConsumeTokenSuccessfully() {
        when(tokenRepository.findTokensByClientAndStatus(testClient, TokenStatus.AVAILABLE))
                .thenReturn(List.of(testToken));

        Token consumedToken = tokenService.consumeToken(testClient, LocalDate.now());

        assertNotNull(consumedToken);
        assertEquals(TokenStatus.USED, consumedToken.getStatus());
        verify(tokenRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldExtendTokensWhenNoneAvailable() {
        when(tokenRepository.findTokensByClientAndStatus(testClient, TokenStatus.AVAILABLE))
                .thenReturn(List.of());

        when(tokenRepository.findByClient(testClient))
                .thenReturn(List.of(testToken));

        Token result = tokenService.consumeToken(testClient, LocalDate.now());

        assertNull(result);
        verify(tokenRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldWarnWhenTokensAreExpiring() {
        Token expiringToken = testToken;
        expiringToken.setExpiresAt(LocalDate.now().plusDays(7));

        when(tokenRepository.findExpiringTokens(LocalDate.now().plusDays(7)))
                .thenReturn(List.of(expiringToken));

        tokenService.warnExpiringTokens();

        verify(tokenRepository, times(1)).findExpiringTokens(LocalDate.now().plusDays(7));
    }
}
