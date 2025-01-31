package com.reform.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.reform.entities.*;
import com.reform.repositories.ClientRepository;
import com.reform.repositories.InstructorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionSyncServiceTest {

    @Mock
    private GoogleCalendarService googleCalendarService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private SubscriptionParserService subscriptionParserService;

    @InjectMocks
    private SessionSyncService sessionSyncService;

    private Client testClient;
    private Instructor testInstructor;
    private Product testProduct;
    private Token testToken;
    private LocalDate sessionDate;
    private Event testEvent;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new Client();
        testClient.setName("John Doe");

        testInstructor = new Instructor();
        testInstructor.setName("Carmen");

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

        sessionDate = LocalDate.now();

        // Set Google Calendar IDs using Reflection
        setField(sessionSyncService, "carmenCalendarId", "mock-carmen-calendar-id");
        setField(sessionSyncService, "dianaCalendarId", "mock-diana-calendar-id");

        // Create a mock event
        testEvent = new Event();
        testEvent.setSummary(testClient.getName()); // Client name in title
        testEvent.setDescription("Start abonament 10 sedinte 1:1"); // Subscription info

        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDateTime(new DateTime(sessionDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        testEvent.setStart(eventDateTime);
    }

    /**
     * Utility method to inject values into private fields.
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void shouldSyncSessionsFromGoogleCalendar() throws Exception {
        when(googleCalendarService.getSessions(any(), any(), any())).thenReturn(List.of(testEvent));
        when(instructorRepository.findByName(anyString())).thenReturn(Optional.of(testInstructor));
        when(clientRepository.findByName(anyString())).thenReturn(Optional.of(testClient));

        sessionSyncService.syncSessionsFromGoogleCalendar();

        verify(googleCalendarService, times(2)).getSessions(any(), any(), any());
        verify(clientRepository, atLeastOnce()).findByName(anyString());
        verify(instructorRepository, atLeastOnce()).findByName(anyString());
    }

    @Test
    void shouldProcessEventAndGenerateTokens() {
        when(clientRepository.findByName(testClient.getName())).thenReturn(Optional.of(testClient));
        when(instructorRepository.findByName(testInstructor.getName())).thenReturn(Optional.of(testInstructor));
        when(subscriptionParserService.extractProductFromEventDescription(anyString()))
                .thenReturn(Optional.of(testProduct));

        doAnswer(invocation -> {
            testClient.setTokens(List.of(testToken)); // Simulate token generation
            return null;
        }).when(tokenService).generateTokensForClient(any(Client.class), any(Product.class));

        sessionSyncService.processEvent(testEvent, testInstructor.getName());

        verify(tokenService, times(1)).generateTokensForClient(testClient, testProduct);
        verify(clientRepository, atLeastOnce()).findByName(testClient.getName());
        verify(instructorRepository, atLeastOnce()).findByName(testInstructor.getName());
    }

    @Test
    void shouldProcessEventAndDeductToken() {
        when(clientRepository.findByName(testClient.getName())).thenReturn(Optional.of(testClient));
        when(instructorRepository.findByName(testInstructor.getName())).thenReturn(Optional.of(testInstructor));
        when(subscriptionParserService.extractProductFromEventDescription(anyString())).thenReturn(Optional.empty());
        when(tokenService.consumeToken(testClient, sessionDate)).thenReturn(testToken);

        sessionSyncService.processEvent(testEvent, testInstructor.getName());

        verify(tokenService, times(1)).consumeToken(testClient, sessionDate);
        verify(clientRepository, atLeastOnce()).findByName(testClient.getName());
        verify(instructorRepository, atLeastOnce()).findByName(testInstructor.getName());
    }
}
