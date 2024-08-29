package com.oggo.planmaker.service;

import java.io.IOException;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.oggo.planmaker.mapper.EventMapper;
import com.oggo.planmaker.model.CustomEvent;

@Service
public class EventService {

	@Autowired
	private EventMapper eventMapper;

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final ZoneId LOCAL_ZONE_ID = ZoneId.of("Asia/Seoul");
	private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

	// 데이터베이스에서 모든 이벤트를 조회합니다.
	public List<CustomEvent> getAllEvents() {
		return eventMapper.selectAllEvents();
	}

	// 구글 캘린더 서비스 객체를 생성합니다.
	private Calendar getCalendarService(String accessToken) throws IOException, GeneralSecurityException {
		GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
		HttpCredentialsAdapter credentialsAdapter = new HttpCredentialsAdapter(credentials);

		return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
				credentialsAdapter).setApplicationName("Plan Maker").build();
	}

	// 데이터베이스의 이벤트를 구글 캘린더와 동기화합니다.
	@Transactional
	public void syncDbEventsToGoogleCalendar(String accessToken) {
		try {
			List<CustomEvent> dbEvents = getAllEvents();
			Calendar service = getCalendarService(accessToken);

			// 구글 캘린더의 모든 기존 이벤트를 조회합니다.
			List<Event> existingGoogleEvents = getAllGoogleEvents(service);

			// 데이터베이스의 이벤트를 구글 캘린더와 동기화합니다.
			for (CustomEvent customEvent : dbEvents) {
				Event googleEvent = mapDTOToGoogleEvent(customEvent);
				// 데이터베이스의 이벤트를 구글 캘린더에서 찾습니다.
				Event existingEvent = findGoogleEventBySummary(existingGoogleEvents, googleEvent.getSummary());

				if (existingEvent == null) {
					// 구글 캘린더에 기존 이벤트가 없으면 새로운 이벤트를 추가합니다.
					service.events().insert("primary", googleEvent).execute();
					System.out.println("Event added to Google Calendar: " + googleEvent.getSummary());
				} else {
					System.out.println("Event already exists on Google Calendar: " + googleEvent.getSummary());
				}
			}

			// 데이터베이스에 없는 구글 캘린더의 이벤트를 삭제합니다.
			deleteGoogleCalendarEventsNotInDb(service, dbEvents);
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			System.err.println("Failed to sync events to Google Calendar: " + e.getMessage());
		}
	}

	// 구글 캘린더에서 모든 이벤트를 조회하여 리스트로 반환합니다.
	private List<Event> getAllGoogleEvents(Calendar service) throws IOException {
		List<Event> allEvents = new java.util.ArrayList<>();
		String pageToken = null;
		do {
			com.google.api.services.calendar.model.Events events = service.events().list("primary")
					.setPageToken(pageToken).execute();
			List<Event> googleEvents = events.getItems();
			allEvents.addAll(googleEvents);
			pageToken = events.getNextPageToken();
		} while (pageToken != null);
		return allEvents;
	}

	// 데이터베이스에 없는 구글 캘린더의 이벤트를 삭제합니다.
	private void deleteGoogleCalendarEventsNotInDb(Calendar service, List<CustomEvent> dbEvents) throws IOException {
		Set<String> dbEventIdentifiers = dbEvents.stream().map(event -> event.getPoiName() + "-" + event.getSchenum()) // 이벤트를
																														// 식별하는
																														// 유니크
																														// 키
																														// 생성
				.collect(Collectors.toSet());

		List<Event> existingGoogleEvents = getAllGoogleEvents(service);

		for (Event googleEvent : existingGoogleEvents) {
			String googleEventIdentifier = googleEvent.getSummary(); // 유니크 키를 사용하여 비교

			if (!dbEventIdentifiers.contains(googleEventIdentifier)) {
				// 데이터베이스에 없는 구글 캘린더의 이벤트를 삭제합니다.
				service.events().delete("primary", googleEvent.getId()).execute();
				System.out.println("Event removed from Google Calendar: " + googleEvent.getId());
			}
		}
	}

	// 특정 이벤트를 데이터베이스와 구글 캘린더에서 삭제합니다.
	public void deleteEvent(int schenum, String GoogleToken) {
		try {
			// 데이터베이스에서 이벤트를 삭제합니다.
			eventMapper.deleteEvent(schenum);

			// 구글 캘린더에서 이벤트를 삭제합니다.
			Calendar service = getCalendarService(GoogleToken);
			String googleEventId = getGoogleEventId(schenum, service);
			if (googleEventId != null) {
				service.events().delete("primary", googleEventId).execute();
				System.out.println("Event removed from Google Calendar.");
			} else {
				System.out.println("Event not found in Google Calendar.");
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			System.err.println("Failed to delete event from Google Calendar: " + e.getMessage());
		}
	}

	// 구글 캘린더에서 특정 이벤트의 ID를 조회합니다.
	private String getGoogleEventId(int schenum, Calendar service) throws IOException {
		List<Event> existingGoogleEvents = getAllGoogleEvents(service);

		for (Event event : existingGoogleEvents) {
			String identifier = event.getSummary(); // 이벤트 식별자

			if (identifier.equals(schenum)) { // 이벤트 식별자 키 사용
				return event.getId();
			}
		}

		return null;
	}

	// CustomEvent 객체를 Google Calendar Event 객체로 변환합니다.
	private Event mapDTOToGoogleEvent(CustomEvent eventDTO) {
		Event event = new Event();
		event.setSummary(eventDTO.getPoiName() + "-" + eventDTO.getSchenum()); // schenum을 포함하여 유일한 식별자를 설정합니다.
		event.setDescription(eventDTO.getScheDesc());

		// 날짜와 시간을 결합하여 ISO 8601 형식으로 변환합니다.
		String startDateTimeStr = eventDTO.getScheStDt().toString() + "T" + eventDTO.getScheStTm().toString();
		String endDateTimeStr = eventDTO.getScheEdDt().toString() + "T" + eventDTO.getScheEdTm().toString();

		// 문자열을 LocalDateTime으로 변환합니다.
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		LocalDateTime startLocalDateTime = LocalDateTime.parse(startDateTimeStr, formatter);
		LocalDateTime endLocalDateTime = LocalDateTime.parse(endDateTimeStr, formatter);

		// LocalDateTime을 UTC로 변환합니다.
		EventDateTime start = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(
				startLocalDateTime.atZone(LOCAL_ZONE_ID).withZoneSameInstant(UTC_ZONE_ID).toInstant().toEpochMilli()));
		EventDateTime end = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(
				endLocalDateTime.atZone(LOCAL_ZONE_ID).withZoneSameInstant(UTC_ZONE_ID).toInstant().toEpochMilli()));

		event.setStart(start);
		event.setEnd(end);

		return event;
	}

	// 구글 캘린더에서 이벤트를 조회하여 Summary로 중복을 확인합니다.
	private Event findGoogleEventBySummary(List<Event> googleEvents, String summary) {
		for (Event event : googleEvents) {
			if (event.getSummary().equals(summary)) {
				return event;
			}
		}
		return null;
	}

}
