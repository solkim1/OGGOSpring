package com.oggo.planmaker.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    @GetMapping("/generate")
    public CompletableFuture<ResponseEntity<Map<String, List<Map<String, Object>>>>> generateSchedule(
            @RequestParam String userId,
            @RequestParam int days,
            @RequestParam String ageGroup,
            @RequestParam String gender,
            @RequestParam String groupSize,
            @RequestParam String theme,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        return scheduleService.generateItinerary(userId, days, ageGroup, gender, groupSize, theme, startDate, endDate)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    Map<String, List<Map<String, Object>>> errorResponse = new HashMap<>();
                    errorResponse.put("error", List.of(Map.of("message", "일정 생성 중 오류가 발생했습니다: " + ex.getMessage())));
                    return ResponseEntity.status(500).body(errorResponse);
                });
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveSchedule(
            @RequestParam String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            scheduleService.saveItinerary(userId, startDate, endDate);
            return ResponseEntity.ok("일정이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("일정 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Schedule>> getUserSchedules(@PathVariable String userId) {
        try {
            List<Schedule> schedules = scheduleService.getUserSchedules(userId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateSchedule(@RequestBody Schedule schedule) {
        try {
            scheduleService.updateSchedule(schedule);
            return ResponseEntity.ok("일정이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("일정 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<String> deleteSchedule(@PathVariable int scheduleId) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.ok("일정이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("일정 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}