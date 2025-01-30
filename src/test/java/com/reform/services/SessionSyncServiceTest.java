package com.reform.services;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.reform.entities.*;
import com.reform.repositories.ClientRepository;
import com.reform.repositories.InstructorRepository;
import com.reform.repositories.ProductRepository;
import com.reform.repositories.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import com.google.api.client.util.DateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SessionSyncServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private SubscriptionParserService subscriptionParserService;

    @Mock
    private TokenService tokenService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SessionSyncService sessionSyncService;

    private Event mockEvent;
    private Client mockClient;
    private Instructor mockInstructor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockClient = Client.builder().name("John Doe").build();
        mockInstructor = Instructor.builder().name("Carmen").build();

        mockEvent = new Event();
        mockEvent.setSummary("John Doe");
        mockEvent.setDescription("Start abonament 5 sedinte 1:1");
        mockEvent.setStart(new EventDateTime().setDate(new DateTime(Instant.now().toEpochMilli())));
    }

    // ✅ Test 1: Ensures a new client is created if they don't exist
    @Test
    void shouldCreateClientIfNotExists() {
        when(clientRepository.findByName("John Doe")).thenReturn(Optional.empty());
        when(clientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        sessionSyncService.processEvent(mockEvent, "Carmen");

        verify(clientRepository, times(1)).save(argThat(client ->
                client.getName().equals("John Doe")
        ));
    }

    // ✅ Test 2: Ensures no duplicate clients are created if they already exist
    @Test
    void shouldFindExistingClient() {
        when(clientRepository.findByName("John Doe")).thenReturn(Optional.of(mockClient));

        sessionSyncService.processEvent(mockEvent, "Carmen");

        verify(clientRepository, never()).save(any());
    }

    // ✅ Test 3: Ensures tokens are generated for a new subscription
    @Test
    void shouldGenerateTokensForNewSubscription() {
        // Given: A client is starting a new subscription
        when(clientRepository.findByName("John Doe"))
                .thenReturn(Optional.of(new Client("John Doe")));

        when(productRepository.findByName("abonament 5 sedinte 1:1"))
                .thenReturn(Optional.of(new Product("abonament 5 sedinte 1:1", 450.0, 5, TokenType.ONE_ON_ONE, TokenExpirePolicy.ONE_MONTH)));

        when(tokenService.hasAvailableTokens(any(Client.class)))
                .thenReturn(false); // Ensure no tokens are available to force generation

        // When: A session is processed with a "Start abonament" event
//        sessionSyncService.processEvent(mockEventWithSubscription, "Carmen");

        // Then: Ensure tokens are generated correctly
        verify(tokenService).generateTokensForClient(any(Client.class), any(Product.class));
    }

    // ✅ Test 4: Ensures a token is deducted for a regular session
    @Test
    void shouldDeductTokenForRegularSessionAndUpdateInstructor() {
        when(clientRepository.findByName("John Doe")).thenReturn(Optional.of(mockClient));
        when(instructorRepository.findByName("Carmen")).thenReturn(Optional.of(mockInstructor));
        when(tokenService.deductToken(eq(mockClient), any())).thenReturn(true);

        sessionSyncService.processEvent(mockEvent, "Carmen");

        verify(tokenService, times(1)).deductToken(eq(mockClient), any());
        verify(sessionRepository, times(1)).save(argThat(session ->
                session.getClient().equals(mockClient) &&
                        session.getInstructor().equals(mockInstructor)
        ));
        verify(instructorRepository, times(1)).save(mockInstructor);
    }

    // ✅ Test 5: Ensures NO token is deducted if none are available
    @Test
    void shouldNotDeductTokenIfNoneAvailable() {
        when(clientRepository.findByName("John Doe")).thenReturn(Optional.of(mockClient));
        when(tokenService.deductToken(eq(mockClient), any())).thenReturn(false);

        sessionSyncService.processEvent(mockEvent, "Carmen");

        verify(tokenService, times(1)).deductToken(eq(mockClient), any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void shouldSetCorrectExpiryDateForTokens() {
        // Given: A new subscription is created
        Product product = new Product("abonament 10 sedinte 1:1", 900.0, 10, TokenType.ONE_ON_ONE, TokenExpirePolicy.TWO_MONTHS);
        LocalDate today = LocalDate.now();
        LocalDate expectedExpiry = today.plusMonths(2);

        // When: Tokens are generated
        List<Token> tokens = IntStream.range(0, product.getTokenQty())
                .mapToObj(i -> Token.builder()
                        .client(new Client("John Doe"))
                        .product(product)
                        .tokenValue(product.getPrice() / product.getTokenQty())
                        .expiresAt(expectedExpiry)
                        .build())
                .toList();

        // Then: Check expiry date
        assertEquals(expectedExpiry, tokens.get(0).getProduct().getAvailableUntil());
    }

}

