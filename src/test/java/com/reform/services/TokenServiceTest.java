package com.reform.services;

import com.reform.entities.Client;
import com.reform.entities.Product;
import com.reform.entities.Token;
import com.reform.entities.TokenExpirePolicy;
import com.reform.repositories.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Captor
    private ArgumentCaptor<List<Token>> tokenCaptor;


    private Client testClient;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testClient = Client.builder().name("John Doe").build();
        testProduct = Product.builder()
                .tokenQty(5)
                .tokenExpirePolicy(TokenExpirePolicy.ONE_MONTH)
                .build();
    }

    @Test
    void shouldGenerateCorrectNumberOfTokens() {
        tokenService.generateTokensForClient(testClient, testProduct);

        // ✅ Capture the saved tokens list
        verify(tokenRepository).saveAll(tokenCaptor.capture());

        // ✅ Assert the correct number of tokens
        List<Token> savedTokens = tokenCaptor.getValue();
        assertEquals(testProduct.getTokenQty(), savedTokens.size());

        // ✅ Ensure all tokens belong to the correct client
        assertTrue(savedTokens.stream().allMatch(t -> t.getClient().equals(testClient)));

        // ✅ Ensure all tokens have the correct expiration date
        LocalDate expectedExpiry = LocalDate.now().plusMonths(1);
        assertTrue(savedTokens.stream().allMatch(t -> expectedExpiry.equals(t.getExpiresAt())));
    }

    @Test
    void shouldDeductTokenIfAvailable() {
        // 🔹 Arrange: Mock an available token
        Token availableToken = Token.builder()
                .client(testClient)
                .expiresAt(LocalDate.now().plusDays(10)) // ✅ Valid token
                .build();

        when(tokenRepository.findUnexpiredTokens(eq(testClient), any()))
                .thenReturn(List.of(availableToken));

        // 🔹 Act: Deduct a token
        boolean success = tokenService.deductToken(testClient, LocalDate.now());

        // 🔹 Assert: Check token is marked as used
        assertTrue(success, "Expected token deduction to succeed");
        assertNotNull(availableToken.getUsedAt(), "Expected token usedAt to be updated");
        assertEquals(LocalDate.now(), availableToken.getUsedAt(), "Expected token usedAt to match session date");

        // 🔹 Verify: Token repository saves updated token
        verify(tokenRepository).save(availableToken);
    }

    @Test
    void shouldNotDeductTokenIfNoneAvailable() {
        when(tokenRepository.findUnexpiredTokens(eq(testClient), any()))
                .thenReturn(List.of());

        boolean success = tokenService.deductToken(testClient, LocalDate.now());

        assertFalse(success);
    }
}

