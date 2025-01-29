package com.reform.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.reform.entities.Client;
import com.reform.entities.Instructor;
import com.reform.entities.Session;
import com.reform.repositories.ClientRepository;
import com.reform.repositories.InstructorRepository;
import com.reform.repositories.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionSyncService {

    private final GoogleCalendarService googleCalendarService;
    private final ClientRepository clientRepository;
    private final InstructorRepository instructorRepository;
    private final SessionRepository sessionRepository;

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
                processEvent(event, instructor);
            }

        } catch (Exception e) {
            log.error("Error syncing sessions for instructor {}: {}", instructorName, e.getMessage(), e);
        }
    }

    private void processEvent(Event event, Instructor instructor) {
        String clientName = event.getSummary(); // Assuming client name is in the event title
        LocalDate sessionDate = Instant.ofEpochMilli(event.getStart().getDateTime().getValue())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        Client client = clientRepository.findByFirstName(clientName)
                .orElseGet(() -> {
                    Client newClient = new Client();
                    newClient.setFirstName(clientName);
                    return clientRepository.save(newClient);
                });

        // Check if session already exists
        if (!sessionRepository.existsByClientAndSessionDate(client, sessionDate)) {
            Session session = new Session();
            session.setClient(client);
            session.setInstructor(instructor);
            session.setSessionDate(sessionDate);
            sessionRepository.save(session);

            log.info("Added new session: Client {} with Instructor {} on {}", clientName, instructor.getName(), sessionDate);
        }
    }
}
