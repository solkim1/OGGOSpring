package com.oggo.planmaker.controller;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oggo.planmaker.mapper.ScheduleMapper;

import com.oggo.planmaker.model.Schedule;
import com.oggo.planmaker.model.ScheduleJson;
import com.oggo.planmaker.service.ScheduleService;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    private static final String DATA_DIR = "src/main/resources/data/";
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private ScheduleMapper scheduleMapper;


    @GetMapping("/travel/generate")
    public CompletableFuture<ResponseEntity<Map<String, List<Map<String, Object>>>>> generateTravelSchedule(

            @RequestParam String userId,
            @RequestParam int days,
            @RequestParam String ageGroup,
            @RequestParam String gender,
            @RequestParam String groupSize,
            @RequestParam String theme,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        return scheduleService.generateTravelItinerary(userId, days, ageGroup, gender, groupSize, theme, startDate, endDate)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/business/generate")
    public CompletableFuture<ResponseEntity<Map<String, List<Map<String, Object>>>>> generateBusinessSchedule(
            @RequestParam String userId,
            @RequestParam int days,
            @RequestParam String region,
            @RequestParam String includeOptions,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return scheduleService.generateBusinessItinerary(userId, days, region, includeOptions, startTime, endTime, startDate, endDate)
                .thenApply(ResponseEntity::ok);

    }
    @GetMapping("/patchschedule")
    public String patchschedule(@RequestParam("scheNum") String scheNum) {
        System.out.println(scheNum);

        List<ScheduleJson> schedule = scheduleMapper.patchschedule(scheNum);
        
        for (ScheduleJson sche : schedule) {
            System.out.println(sche.toString());
        }

        // Create an ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        Map<String, List<ScheduleJson>> groupedByDate = new TreeMap<>();
        for (ScheduleJson sche : schedule) {
            String date = sche.getStartDate();
            groupedByDate.putIfAbsent(date, new ArrayList<>());
            groupedByDate.get(date).add(sche);
        }

        // 같은날짜 day ~ 로 그룹화
        int dayCounter = 1;
        for (Map.Entry<String, List<ScheduleJson>> entry : groupedByDate.entrySet()) {
            ArrayNode dayArray = mapper.createArrayNode();

            for (ScheduleJson sche : entry.getValue()) {
                ObjectNode scheduleNode = mapper.createObjectNode();
                scheduleNode.put("name", sche.getTitle());
                scheduleNode.put("lat", sche.getLat());
                scheduleNode.put("lng", sche.getLng());
                scheduleNode.put("description", sche.getDescription());
                scheduleNode.put("departTime", sche.getDepartTime().substring(0, 5)); // DB에 초까지 저장되어있으므로 잘라내기
                scheduleNode.put("arriveTime", sche.getArriveTime().substring(0, 5)); // DB에 초까지 저장되어있으므로 잘라내기
                scheduleNode.put("type", sche.getType());

                dayArray.add(scheduleNode);
            }

            // 같은날짜 day ~ 로 그룹화
            rootNode.set("day" + dayCounter, dayArray);
            dayCounter++;
        }

        // JSON 데이터 String 으로 리턴
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Schedule>> getAllSchedules(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(scheduleService.getAllSchedules(userId));
    }

//    @GetMapping("/travel")
//    public ResponseEntity<List<Schedule>> getTravelSchedules(@RequestParam("userId") String userId) {
//        return ResponseEntity.ok(scheduleService.getSchedulesByBusinessFlag(userId, "N"));
//    }
//
//    @GetMapping("/business")
//    public ResponseEntity<List<Schedule>> getBusinessSchedules(@RequestParam("userId") String userId) {
//        return ResponseEntity.ok(scheduleService.getSchedulesByBusinessFlag(userId, "Y"));
//    }
//
//    @GetMapping("/important")
//    public ResponseEntity<List<Schedule>> getImportantSchedules(@RequestParam("userId") String userId) {
//        return ResponseEntity.ok(scheduleService.getImportantSchedules(userId));
//    }

    @PutMapping("/toggleImportance/{scheNum}")
    public ResponseEntity<Void> toggleImportance(@PathVariable String scheNum) {
        scheduleService.toggleImportance(scheNum);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{scheNum}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable String scheNum) {
        scheduleService.deleteSchedule(scheNum);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateSchedule(@RequestParam String scheNum, @RequestParam String scheTitle, @RequestParam String scheDesc) {
        scheduleService.updateSchedule(scheNum, scheTitle, scheDesc);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> insertTravelCourses(@RequestBody List<ScheduleJson> scheduleJsonList) throws SQLException {
    	System.out.println("좀돼라");
        logger.info("Received request to save schedules: {}", scheduleJsonList);
        Map<String, Object> response = new HashMap<>();

        try {
            scheduleService.saveSchedules(scheduleJsonList);
            
            response.put("status", "success");
            response.put("message", "성공적으로 저장되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error occurred while saving schedules: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "저장 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    

    @GetMapping("/themes/{themeName}")
    public ResponseEntity<?> getScheduleByTheme(@PathVariable String themeName) {
        try {
            String jsonContent = scheduleService.getScheduleByTheme(themeName);

            return ResponseEntity.ok(jsonContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("{\"error\": \"Schedule not found for theme: " + themeName + "\"}");
        }
    }

    
    @GetMapping("/exhibitions/{exhibitionName}")
    public ResponseEntity<String> getScheduleByExhibition(@PathVariable String exhibitionName) {
        try {
            String jsonContent = scheduleService.getScheduleByExhibition(exhibitionName);
            return ResponseEntity.ok(jsonContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("{\"error\": \"Schedule not found for exhibition: " + exhibitionName + "\"}");
        }
    }

}