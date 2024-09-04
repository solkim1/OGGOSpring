package com.oggo.planmaker.service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oggo.planmaker.mapper.ScheduleMapper;
import com.oggo.planmaker.model.Schedule;
import com.oggo.planmaker.model.ScheduleJson;

@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);
    private static final String DATA_DIR = "src/main/resources/data/";
    private static final String EXHIBITIONS_DIR = DATA_DIR;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private OpenAIService openAIService;

    private final Map<String, Map<String, List<Map<String, Object>>>> temporaryItineraries = new ConcurrentHashMap<>();

    public CompletableFuture<Map<String, List<Map<String, Object>>>> generateTravelItinerary(String userId, int days, String ageGroup, String gender, String groupSize, String theme, String startDate, String endDate) {
        String prompt = generateTravelPrompt(days, ageGroup, gender, groupSize, theme, startDate, endDate);
        int maxRetries = 1;
        return generateItineraryWithRetry(prompt, userId, maxRetries);
    }

    public CompletableFuture<Map<String, List<Map<String, Object>>>> generateBusinessItinerary(String userId, int days, String region, String includeOptions, String startTime, String endTime, String startDate, String endDate) {
        String prompt = generateBusinessPrompt(days, region, includeOptions, startTime, endTime, startDate, endDate);
        int maxRetries = 1;
        return generateItineraryWithRetry(prompt, userId, maxRetries);
    }

    private CompletableFuture<Map<String, List<Map<String, Object>>>> generateItineraryWithRetry(String prompt, String userId, int retries) {
        return openAIService.generateItinerary(prompt).thenApply(aiGeneratedText -> {
            log.info("AI 생성된 텍스트: " + aiGeneratedText);
            Map<String, List<Map<String, Object>>> itinerary = parseAIResponse(aiGeneratedText);

            if (itinerary == null) {
                log.error("일정 파싱에 실패했습니다. AI 응답을 확인하세요.");
                throw new RuntimeException("일정 생성 중 오류가 발생했습니다. AI 응답을 확인하세요.");
            }

            temporaryItineraries.put(userId, itinerary);
            return itinerary;
        }).exceptionally(ex -> {
            log.error("일정 생성 중 예외가 발생했습니다: " + ex.getMessage());
            if (retries > 1) {
                log.info("재시도 중... 남은 시도 횟수: " + (retries - 1));
                return generateItineraryWithRetry(prompt, userId, retries - 1).join();
            } else {
                throw new RuntimeException("일정 생성 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
        });
    }

    @Transactional
    public void saveSchedules(List<ScheduleJson> scheduleJsonList) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonSchedules = objectMapper.writeValueAsString(scheduleJsonList);
            
            Map<String, Object> params = new HashMap<>();
            params.put("p_schedules", jsonSchedules);
            
            scheduleMapper.callSaveScheduleWithPOI(params);
            
            log.info("Schedules saved successfully");
        } catch (Exception e) {
            log.error("Error saving schedules", e);
            throw new RuntimeException("스케줄 저장 중 오류 발생", e);
        }
    }

    public List<Schedule> getAllSchedules(String userId) {
        return scheduleMapper.findAllSchedulesByUserId(userId);
    }

    public List<Schedule> getSchedulesByBusinessFlag(String userId, String isBusiness) {
        return scheduleMapper.findByBusinessFlag(userId, isBusiness);
    }

    public List<Schedule> getImportantSchedules(String userId) {
        return scheduleMapper.findImportantSchedules(userId);
    }

    public void toggleImportance(String scheNum) {
        scheduleMapper.updateImportanceByScheNum(scheNum);
    }

    public void deleteSchedule(String scheNum) {
        scheduleMapper.deleteByScheNum(scheNum);
    }

    public void updateSchedule(String scheNum, String scheTitle, String scheDesc) {
        scheduleMapper.updateSchedule(scheNum, scheTitle, scheDesc);
    }

    private String generateTravelPrompt(int days, String ageGroup, String gender, String groupSize, String theme, String startDate, String endDate) {
        return String.format("아래 조건에 맞춰 %d일간의 여행 일정을 JSON 형식으로 작성해 주세요:\n"
                + "- 연령대: %s, 성별: %s, 그룹 크기: %s명, 테마: %s\n"
                + "- 기간: %s부터 %s까지, 반드시 매일 아침 9시부터 저녁 9시까지 활동이 꽉 차도록 구성\n"
                + "- 사람이 보편적으로 활동하는 시간에 맞춰서 추천해주세요"
                + "- 다음 장소는 현재 위치로부터 반경 3km 내에 위치해야 함\n"
                + "- 각 장소의 설명은 40자 이하로 작성\n"
                + "- 일정에는 관광지, 식당, 카페, 숙박이 포함되며, 모두 실제 존재하는 장소여야 함\n"
                + "- 모든 장소 정보는 신뢰할 수 있는 출처(예: Google 지도, 위키피디아)에 기반해야 함\n"
                + "- 시간은 HH:MM 형식으로 작성\n"
                + "- 응답은 완전한 JSON 형식이어야 하며, JSON 외의 내용은 포함하지 마세요\n"
                + "- 응답은 한글로 작성해 주세요\n"
                + "JSON 형식의 예시는 다음과 같습니다:\n"
                + "```json\n"
                + "{\n"
                + "  \"day1\": [\n"
                + "    {\"name\": \"장소명\", \"lat\": 위도, \"lng\": 경도, \"description\": \"설명 (40자 이하)\", \"departTime\": \"출발시간\", \"arriveTime\": \"도착시간\", \"type\": \"관광지/식당/카페/숙박\"}\n"
                + "  ],\n"
                + "  \"day2\": [...],\n"
                + "  ...\n"
                + "  \"day%d\": [...]\n"
                + "}\n"
                + "```", days, ageGroup, gender, groupSize, theme, startDate, endDate, days);
    }
 
    private String generateBusinessPrompt(int days, String region, String includeOptions, String startTime, String endTime, String startDate, String endDate) {
        return String.format("아래 조건에 맞춰 %d일간의 출장 일정을 JSON 형식으로 작성해 주세요:\n"
                + "- 지역: %s\n"
                + "- 포함할 활동: %s\n"
                + "- 출장 업무 시간: %s부터 %s까지, 이 시간을 피한 일정으로 구성\n"
                + "- 사람이 보편적으로 활동하는 시간에 맞춰서 추천해주세요"
                + "- 시작일: %s, 종료일: %s\n, 반드시 매일 아침 9시부터 저녁 9시까지 활동이 꽉 차도록 구성"
                + "- 일정에는 각 포함할 활동이 반드시 존재해야하며, 전시회는 현재 열리고 있는 국내 전시회이어야 합니다\n"
                + "- 모든 장소는 실제로 존재하는 장소여야 하며, 허구의 장소는 포함하지 마세요\n"
                + "- 장소 정보는 신뢰할 수 있는 출처(예: Google 지도, 위키피디아)에 기반해 주세요\n"
                + "- 시간은 HH:MM 형식으로 작성\n"
                + "- 응답은 완전한 JSON 형식으로, JSON 외의 내용은 포함하지 말고, 한글로 작성해 주세요\n"
                + "JSON 형식의 예시는 다음과 같습니다:\n"
                + "```json\n"
                + "{\n"
                + "  \"day1\": [\n"
                + "    {\"name\": \"장소명\", \"lat\": 위도, \"lng\": 경도, \"description\": \"설명 (40자 이하)\", \"departTime\": \"출발시간\", \"arriveTime\": \"도착시간\", \"type\": \"활동 유형\"}\n"
                + "  ],\n"
                + "  \"day2\": [...],\n"
                + "  ...\n"
                + "  \"day%d\": [...]\n"
                + "}\n"
                + "```", days, region, includeOptions, startTime, endTime, startDate, endDate, days);
    }

    private Map<String, List<Map<String, Object>>> parseAIResponse(String aiGeneratedText) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String jsonText = aiGeneratedText.trim();

            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }

            jsonText = jsonText.trim();

            if (jsonText.startsWith("{") && jsonText.endsWith("}")) {
                return objectMapper.readValue(jsonText, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
            } else {
                log.error("유효하지 않은 JSON 형식이 감지되었습니다. 응답의 유효한 부분만 파싱을 시도합니다.");

                int lastIndex = jsonText.lastIndexOf("},");
                if (lastIndex > 0) {
                    String validPart = jsonText.substring(0, lastIndex + 2) + "]}";
                    try {
                        return objectMapper.readValue(validPart,
                                new TypeReference<Map<String, List<Map<String, Object>>>>() {});
                    } catch (JsonProcessingException ex) {
                        log.error("유효한 JSON 부분도 파싱하지 못했습니다: " + validPart);
                    }
                }
                throw new JsonProcessingException("유효하지 않은 JSON 형식입니다") {
                    private static final long serialVersionUID = 1L;
                };
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: " + e.getMessage(), e);
            return null;
        }
    }

    public String getScheduleByTheme(String themeName) throws IOException {
        String filePath = DATA_DIR + themeName + ".json";
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public String getScheduleByExhibition(String exhibitionName) throws IOException {
        String filePath = EXHIBITIONS_DIR + exhibitionName + ".json";
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}