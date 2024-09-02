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
import com.oggo.planmaker.model.Event;
import com.oggo.planmaker.config.GoogleCalendarConfig;

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

		Calendar service = googleCalendarConfig.googleCalendarService();

		Events events = service.events().list("primary").setOauthToken(accessToken).setMaxResults(10)
				.setTimeMin(new DateTime(System.currentTimeMillis())).setOrderBy("startTime").setSingleEvents(true)
				.execute();

		List<Event> eventList = new ArrayList<>();
		for (com.google.api.services.calendar.model.Event googleEvent : events.getItems()) {
			Event event = new Event();
			event.setId(googleEvent.getId());
			event.setSummary(googleEvent.getSummary());
			event.setDescription(googleEvent.getDescription());
			event.setLocation(googleEvent.getLocation());
			if (googleEvent.getStart().getDateTime() != null) {
				event.setStartTime(LocalDateTime.ofInstant(
						Instant.ofEpochMilli(googleEvent.getStart().getDateTime().getValue()), ZoneId.systemDefault()));
			}
			if (googleEvent.getEnd().getDateTime() != null) {
				event.setEndTime(LocalDateTime.ofInstant(
						Instant.ofEpochMilli(googleEvent.getEnd().getDateTime().getValue()), ZoneId.systemDefault()));
			}
			eventList.add(event);
		}
		return eventList;
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
					Instant.ofEpochMilli(googleEvent.getStart().getDateTime().getValue()), ZoneId.systemDefault()));
		}

		if (googleEvent.getEnd() != null && googleEvent.getEnd().getDateTime() != null) {
			event.setEndTime(LocalDateTime.ofInstant(
					Instant.ofEpochMilli(googleEvent.getEnd().getDateTime().getValue()), ZoneId.systemDefault()));
		}

		return event;
	}
}
