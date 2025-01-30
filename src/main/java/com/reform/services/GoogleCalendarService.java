package com.reform.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "Fitness Studio Calendar Sync";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Calendar service;

    public GoogleCalendarService() throws Exception {
        service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials() throws Exception {
        // Load client secrets from credentials.json
        InputStream in = GoogleCalendarService.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new RuntimeException("Resource not found: credentials.json");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                Collections.singletonList(CalendarScopes.CALENDAR_READONLY))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder()
                .setPort(8888)  // Ensure this port is open
                .build())
                .authorize("user");    }

    public List<Event> getSessions(String calendarId, DateTime start, DateTime end) throws Exception {
        Events events = service.events()
                .list(calendarId)
                .setTimeMin(start)
                .setTimeMax(end)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();

        return events.getItems();
    }
}
