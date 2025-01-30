package com.reform.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.reform.entities.Client;
import com.reform.entities.Instructor;
import com.reform.entities.Product;
import com.reform.repositories.ClientRepository;
import com.reform.repositories.InstructorRepository;
import com.reform.repositories.ProductRepository;
import com.reform.repositories.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionSyncService {

    private final GoogleCalendarService googleCalendarService;
    private final ClientRepository clientRepository;
    private final InstructorRepository instructorRepository;
    private final SessionRepository sessionRepository;
    private final ProductRepository productRepository;
    private final TokenService tokenService;
    private final ProductService productService;

    @Value("${google.calendar.carmen.id}")
    private String carmenCalendarId;

    @Value("${google.calendar.diana.id}")
    private String dianaCalendarId;

    public void syncSessionsFromGoogleCalendar() {
        try {
            DateTime start = new DateTime(Instant.now().minusSeconds(2_592_000).toEpochMilli()); // Last 30 days
            DateTime end = new DateTime(Instant.now().toEpochMilli()); // Up to today

            log.info("Starting Google Calendar sync...");
            syncInstructorSessions("Carmen", carmenCalendarId, start, end);
            syncInstructorSessions("Diana", dianaCalendarId, start, end);
            log.info("Google Calendar sync completed successfully.");

        } catch (Exception e) {
            log.error("Error syncing sessions from Google Calendar: {}", e.getMessage(), e);
        }
    }

    private void syncInstructorSessions(String instructorName, String calendarId, DateTime start, DateTime end) {
        if (calendarId == null || calendarId.isEmpty()) {
            log.error("No Google Calendar ID found for instructor {}", instructorName);
            return;
        }

        try {
            List<Event> events = googleCalendarService.getSessions(calendarId, start, end);
            Instructor instructor = instructorRepository.findByName(instructorName)
                    .orElseGet(() -> instructorRepository.save(new Instructor(instructorName)));

            for (Event event : events) {
                processEvent(event, instructor.getName());
            }

        } catch (Exception e) {
            log.error("Error syncing sessions for instructor {}: {}", instructorName, e.getMessage(), e);
        }
    }


    public void processEvent(Event event, String instructorName) {
        String clientName = event.getSummary(); // Event title is the client's name
        String description = event.getDescription(); // Event description contains subscription info

        // Extract the session date
        EventDateTime eventDateTime = event.getStart();
        DateTime dateTime = eventDateTime.getDateTime() != null ? eventDateTime.getDateTime() : eventDateTime.getDate();
        LocalDate sessionDate = Instant.ofEpochMilli(dateTime.getValue()).atZone(ZoneId.systemDefault()).toLocalDate();

        log.info("📅 Processing session for client: {} on {}", clientName, sessionDate);

        // Fetch or create client
        Client client = clientRepository.findByName(clientName)
                .orElseGet(() -> clientRepository.save(new Client(clientName)));

        // Fetch or create instructor
        Instructor instructor = instructorRepository.findByName(instructorName)
                .orElseGet(() -> instructorRepository.save(new Instructor(instructorName)));

        // Detect if this is a new subscription
        if (description != null && description.toLowerCase().contains("start abonament")) {
            log.info("📜 New subscription detected for {}", clientName);
            handleNewSubscription(client, description, sessionDate);
        }

        // Ensure client has tokens before deducting
        if (!tokenService.hasAvailableTokens(client)) {
            log.warn("⚠️ Client {} has NO available tokens! Cannot deduct a token.", clientName);
            return;
        }

        // Deduct token for this session
        tokenService.deductToken(client, sessionDate);



        // Add session to instructor
        instructor.getSessions().add(sessionRepository.save(new com.reform.entities.Session(sessionDate, client, instructor)));

        log.info("✅ Session recorded for client {} on {}. Token deducted.", clientName, sessionDate);
    }

    private void handleNewSubscription(Client client, String description, LocalDate sessionDate) {
        String subscriptionName = extractSubscriptionName(description);

        Optional<Product> optionalProduct = productService.getProductPriceAtTheStartOfSubscription(subscriptionName, sessionDate);
        if (optionalProduct.isPresent()) {
            tokenService.generateTokensForClient(client, optionalProduct.get());
            log.info("🎟️ Generated tokens for client {} for subscription {}", client.getName(), subscriptionName);
        } else {
            log.error("❌ No matching product found for: {}", subscriptionName);
        }
    }

    private String extractSubscriptionName(String description) {
        return description.replace("Start abonament ", "").trim();
    }
}
