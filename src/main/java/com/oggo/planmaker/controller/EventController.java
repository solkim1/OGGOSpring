package com.oggo.planmaker.controller;


import java.io.IOException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oggo.planmaker.model.CustomEvent;
import com.oggo.planmaker.service.EventService;

// RESTful API 엔드포인트를 정의하는 컨트롤러

@RequestMapping("/api/events")
public class EventController {

    @Autowired

    private EventService eventService; // EventService 의존성 주입

    // 모든 이벤트를 조회하는 엔드포인트
    @GetMapping
    public List<CustomEvent> getAllEvents() {
        System.out.println("모든 이벤트를 조회합니다.");
        return eventService.getAllEvents(); // 서비스 메소드를 호출하여 모든 이벤트를 가져옴
    }

	/*
	 * // Google Calendar와 동기화하는 엔드포인트
	 * 
	 * @PostMapping("/sync") public void syncEvents(@RequestBody SyncRequest
	 * request) throws IOException {
	 * System.out.println("syncEvents method called.");
	 * System.out.println("Attempting to sync Google Calendar events.");
	 * 
	 * try { // Google Calendar와 동기화
	 * eventService.syncDbEventsToGoogleCalendar(request.getAccessToken());
	 * System.out.println("Calendar sync successful!"); } catch (Exception e) {
	 * System.out.println("Failed to sync calendar: " + e.getMessage()); throw e; }
	 * }
	 */

    // 데이터베이스의 이벤트를 Google Calendar로 전송하는 엔드포인트
    @PostMapping("/sync-db")
    public void syncDbEventsToGoogleCalendar(@RequestBody SyncRequest request) throws IOException {
        System.out.println("syncDbEventsToGoogleCalendar method called.");
        System.out.println("Attempting to sync database events to Google Calendar.");

        try {
            // 데이터베이스의 이벤트를 Google Calendar로 동기화
            eventService.syncDbEventsToGoogleCalendar(request.getAccessToken());
            System.out.println("데이터베이스 이벤트를 Google Calendar로 동기화 성공!");
        } catch (Exception e) {
            System.out.println("데이터베이스 이벤트를 Google Calendar로 동기화 실패: {}" + e.getMessage());
            throw e;
        }
    }
}

