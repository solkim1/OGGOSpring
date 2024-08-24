package com.oggo.planmaker.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.oggo.planmaker.service.EventService;
import com.oggo.planmaker.model.Event;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
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