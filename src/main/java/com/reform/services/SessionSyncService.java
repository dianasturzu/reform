package com.reform.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.reform.entities.Client;
import com.reform.entities.Instructor;
import com.reform.entities.Product;
import com.reform.entities.Session;
import com.reform.entities.Token;
import com.reform.entities.TokenStatus;
import com.reform.repositories.ClientRepository;
import com.reform.repositories.InstructorRepository;
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
    private final TokenService tokenService;
    private final SubscriptionParserService subscriptionParserService;

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

        Optional<Product> productOpt = subscriptionParserService.extractProductFromEventDescription(description);

        if (productOpt.isPresent()) {
            // Client purchased a new subscription
            Product product = productOpt.get();
            tokenService.generateTokensForClient(client, product);

            // Fetch the newly generated first token (Assumes tokens are ordered)
            Token firstToken = client.getTokens().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("Tokens should have been generated but were not found"));

            firstToken.setUsedAt(sessionDate);
            firstToken.setStatus(TokenStatus.USED);

            // Create a session and link to the first available token
            Session session = new Session(sessionDate, client, instructor, firstToken);
            client.addSession(session);
            instructor.addSession(session);

            log.info("✅ New subscription started for client {}. First session recorded.", client.getName());

        } else {
            // Client is using an existing token
            Token consumedToken = tokenService.consumeToken(client, sessionDate);
            Session session = new Session(sessionDate, client, instructor, consumedToken);

            client.addSession(session);
            instructor.addSession(session);

            log.info("✅ Session recorded for client {} on {}. Token deducted.", clientName, sessionDate);
        }
    }

}
