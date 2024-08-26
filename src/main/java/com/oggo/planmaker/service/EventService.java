package com.oggo.planmaker.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.oggo.planmaker.mapper.EventMapper;
import com.oggo.planmaker.config.GoogleCalendarConfig;
import com.oggo.planmaker.model.Event;

@Service
public class EventService {
    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private GoogleCalendarConfig googleCalendarConfig;

    public List<Event> getAllEvents() {
        return eventMapper.selectAllEvents();
    }

    public void saveEvents(List<Event> events) {
        eventMapper.insertEvents(events);
    }

    public List<Event> getGoogleCalendarEvents(String accessToken) throws IOException {
        try {
            Calendar service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
                    .setApplicationName("Plan Maker")
                    .build();

            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = service.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<com.google.api.services.calendar.model.Event> items = events.getItems();
            List<Event> convertedEvents = new ArrayList<>();

            for (com.google.api.services.calendar.model.Event googleEvent : items) {
                Event event = convertGoogleEventToEvent(googleEvent);
                convertedEvents.add(event);
            }

            return convertedEvents;
        } catch (Exception e) {
            throw new IOException("Error fetching Google Calendar events", e);
        }
    }

    public void syncGoogleCalendarEvents(String accessToken) throws IOException {
        List<Event> googleEvents = getGoogleCalendarEvents(accessToken);
        saveEvents(googleEvents);
    }

    private Event convertGoogleEventToEvent(com.google.api.services.calendar.model.Event googleEvent) {
        Event event = new Event();
        event.setId(googleEvent.getId());
        event.setSummary(googleEvent.getSummary());
        event.setDescription(googleEvent.getDescription());
        event.setLocation(googleEvent.getLocation());
        
        if (googleEvent.getStart() != null && googleEvent.getStart().getDateTime() != null) {
            event.setStartTime(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(googleEvent.getStart().getDateTime().getValue()),
                ZoneId.systemDefault()
            ));
        }
        
        if (googleEvent.getEnd() != null && googleEvent.getEnd().getDateTime() != null) {
            event.setEndTime(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(googleEvent.getEnd().getDateTime().getValue()),
                ZoneId.systemDefault()
            ));
        }

        return event;
    }
}