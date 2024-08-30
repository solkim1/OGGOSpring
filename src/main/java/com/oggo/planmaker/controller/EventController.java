package com.oggo.planmaker.controller;



import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oggo.planmaker.model.Event;
import com.oggo.planmaker.service.EventService;

@RestController

@RequestMapping("/api/events")
public class EventController {

    @Autowired

    private EventService eventService;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/google")
    public List<Event> getGoogleCalendarEvents(@RequestParam String accessToken) {
        try {
            return eventService.getGoogleCalendarEvents(accessToken);
        } catch (Exception e) {
            // 예외 처리
            return Collections.emptyList(); // 오류 발생 시 빈 리스트 반환
        }
    }

    @PostMapping("/sync")
    public void syncEvents(@RequestParam String accessToken) {
        try {
            eventService.syncGoogleCalendarEvents(accessToken);
        } catch (Exception e) {
            // 적절한 예외 처리
        }
    }


}

