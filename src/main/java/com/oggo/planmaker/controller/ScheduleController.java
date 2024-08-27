package com.oggo.planmaker.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oggo.planmaker.model.Schedule;
import com.oggo.planmaker.service.ScheduleService;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateSchedule(@RequestBody Map<String, String> preferences) {
        try {
            scheduleService.generateSchedule(preferences, null);
            return ResponseEntity.ok("Schedule generated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to create schedule: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Schedule>> getAllSchedules(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(scheduleService.getAllSchedules(userId));
    }

    @GetMapping("/travel")
    public ResponseEntity<List<Schedule>> getTravelSchedules(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByBusinessFlag(userId, "N"));
    }

    @GetMapping("/business")
    public ResponseEntity<List<Schedule>> getBusinessSchedules(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByBusinessFlag(userId, "Y"));
    }

    @GetMapping("/important")
    public ResponseEntity<List<Schedule>> getImportantSchedules(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(scheduleService.getImportantSchedules(userId));
    }

    @PutMapping("/toggleImportance/{scheNum}")
    public ResponseEntity<Void> toggleImportance(@PathVariable int scheNum) {
        scheduleService.toggleImportance(scheNum);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{scheNum}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable int scheNum) {
        scheduleService.deleteSchedule(scheNum);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateSchedule(@RequestParam int scheNum, @RequestParam String scheTitle, @RequestParam String scheDesc) {
        scheduleService.updateSchedule(scheNum, scheTitle, scheDesc);
        return ResponseEntity.ok().build();
    }
}
