package com.oggo.planmaker.controller;



import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oggo.planmaker.mapper.ScheduleMapper;
import com.oggo.planmaker.model.Schedule;
import com.oggo.planmaker.model.ScheduleJson;
import com.oggo.planmaker.service.OpenAIService;
import com.oggo.planmaker.service.ScheduleService;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    private static final String DATA_DIR = "src/main/resources/data/";
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private ScheduleMapper scheduleMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

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

    @PostMapping("/recall")
    public CompletableFuture<ResponseEntity<Map<String, List<Map<String, Object>>>>> regenerateSchedule(
            @RequestBody Map<String, List<Map<String, Object>>> excludedItems) {
		logger.info("Received request to regenerate schedule with excluded items: {}", excludedItems);

        // OpenAI 프롬프트 생성
        StringBuilder promptBuilder = new StringBuilder("현재 이 일정을 제거하고 다른 일정을 넣으려고 합니다. ");
        promptBuilder.append("현재의 일정 정보는 name, type, lat, lng 이며, departTime부터 arriveTime까지 있습니다. ")
                    .append("제시한 lat과 lng를 토대로 근처의 지역에서 동일한 departTime와 arriveTime을 지켜서 다른 지역을 추천해주세요:\n\n");

        excludedItems.forEach((day, items) -> {
            items.forEach(item -> {
                promptBuilder.append(String.format("날짜: %s, 이름: %s, 유형: %s, 위치: (%f, %f), 시간: %s - %s\n",
                        day, item.get("name"), item.get("type"), item.get("lat"), item.get("lng"), 
                        item.get("departTime"), item.get("arriveTime")));
            });
        });

        // JSON 응답 형식 안내 추가
        promptBuilder.append("\n응답은 반드시 완전한 JSON 형식이어야 하며, 불필요한 텍스트나 주석을 포함하지 마세요. 각 날짜(day1, day2 등)에 해당하는 기존 입력값과 다른 새로운 서울의 일정 항목들을 포함해야 합니다. 형식은 다음과 같습니다:\n");
        promptBuilder.append("{\"day1\": [{\"name\": \"장소명\", \"lat\": 위도, \"lng\": 경도, \"description\": \"설명\", \"departTime\": \"출발시간\", \"arriveTime\": \"도착시간\", \"type\": \"관광지/식당/카페/숙박\"}], \"day2\": [...]}");
        promptBuilder.append("\n제거된 항목을 대체할 새로운 항목을 동일한 시간대와 위치에 맞게 제시해주세요.");
        
        String prompt = promptBuilder.toString();
        logger.info("Generated prompt for OpenAI: {}", prompt);

        // OpenAI 서비스에 요청을 보내 새로운 일정을 생성
        return openAIService.generateItinerary(prompt)
                .thenApply(response -> {
                    // OpenAI로부터 받은 응답을 파싱하여 적절한 형식으로 반환
                    Map<String, List<Map<String, Object>>> result = parseOpenAIResponse(response);
                    return ResponseEntity.ok(result); // 클라이언트로 응답 반환
                })
                .exceptionally(ex -> {
                    logger.error("Error during itinerary regeneration: {}", ex.getMessage());
                    Map<String, List<Map<String, Object>>> errorResponse = new HashMap<>();
                    errorResponse.put("error", List.of(Map.of("message", "일정 재생성 중 오류가 발생했습니다: " + ex.getMessage())));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                });
    }

    private Map<String, List<Map<String, Object>>> parseOpenAIResponse(String response) {
        Map<String, List<Map<String, Object>>> parsedResponse = new HashMap<>();
        try {
            // JSON 응답이 올바른 형식인지 확인하고 파싱
            parsedResponse = objectMapper.readValue(response, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
        } catch (JsonParseException e) {
            logger.error("JSON 파싱 오류: 유효하지 않은 JSON 형식입니다. 가능한 경우, 유효한 JSON 부분만 처리합니다.", e);
            // 응답 문자열을 다시 확인하고, JSON 시작 지점 및 종료 지점을 파악해 수동으로 자르기
            String jsonPart = extractValidJson(response);
            if (jsonPart != null) {
                try {
                    parsedResponse = objectMapper.readValue(jsonPart, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
                } catch (Exception ex) {
                    logger.error("유효한 JSON 부분도 파싱에 실패했습니다.", ex);
                }
            }
        } catch (Exception e) {
            logger.error("JSON 파싱 중 일반적인 오류가 발생했습니다: {}", e.getMessage());
        }
        return parsedResponse;
    }

    // 유효한 JSON 부분을 추출하는 메서드 예시
    private String extractValidJson(String response) {
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}") + 1;
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return response.substring(startIndex, endIndex);
        }
        return null;
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
    public ResponseEntity<Map<String, Object>> insertTravelCourses(@RequestBody List<ScheduleJson> scheduleJsonList) {
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