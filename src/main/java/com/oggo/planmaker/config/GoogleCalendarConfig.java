package com.oggo.planmaker.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Configuration
public class GoogleCalendarConfig {

    @Value("${google.api.credentials.file}")
    private String credentialsFilePath;

    private final ResourceLoader resourceLoader;

    public GoogleCalendarConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public Calendar googleCalendarService() {
        try {
            Resource resource = resourceLoader.getResource(credentialsFilePath);
            GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream())
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR_READONLY));
            System.out.println("Google Credentials File Path: " + credentialsFilePath);
            return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Spring Boot Google Calendar API").build();
        } catch (IOException e) {
            System.err.println("Failed to load Google credentials from: " + credentialsFilePath);
            throw new RuntimeException("Failed to create Google Calendar service", e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to create Google Calendar service due to security exception", e);
            
        }
    }
}
