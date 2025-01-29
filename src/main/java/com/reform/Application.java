package com.reform;

import com.reform.services.SessionSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.reform")
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

    private final SessionSyncService sessionSyncService;

    public static void main(String[] args) {
        // Initialize Spring application context
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("Starting Google Calendar Sync...");
        sessionSyncService.syncSessionsFromGoogleCalendar();
        System.out.println("Google Calendar Sync Completed.");
    }
}
