package com.oggo.planmaker.service;

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
import com.oggo.planmaker.mapper.PoiMapper;
import com.oggo.planmaker.mapper.ScheduleMapper;
import com.oggo.planmaker.model.Schedule;
import com.oggo.planmaker.model.ScheduleJson;

@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private PoiMapper poiMapper;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private PoiService poiService;

    private final Map<String, Map<String, List<Map<String, Object>>>> temporaryItineraries = new ConcurrentHashMap<>();

    public CompletableFuture<Map<String, List<Map<String, Object>>>> generateItinerary(String userId, int days,
            String ageGroup, String gender, String groupSize, String theme, String startDate, String endDate) {
        String prompt = generatePrompt(days, ageGroup, gender, groupSize, theme, startDate, endDate);
        int maxRetries = 1;
        return generateItineraryWithRetry(prompt, userId, maxRetries);
    }

    private CompletableFuture<Map<String, List<Map<String, Object>>>> generateItineraryWithRetry(String prompt,
            String userId, int retries) {
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

    private String generatePrompt(int days, String ageGroup, String gender, String groupSize, String theme,
            String startDate, String endDate) {
        return String.format("다음 조건에 따라 %d일간의 여행 일정을 JSON 형식으로 작성해 주세요: "
                + "연령대: %s, 성별: %s, 그룹 크기: %s, 테마: %s, 시작일: %s, 종료일: %s. "
                + "일정은 매일 아침 9시부터 저녁 9시까지로 구성하고, 관광지, 식당, 카페, 숙박을 포함해야 합니다. "
                + "각 장소는 실제로 존재하는 지역이어야 하며, 허구의 장소나 가상의 위치는 포함하지 마세요. "
                + "모든 장소 정보는 신뢰할 수 있는 출처(예: Google 지도, 위키피디아)를 기준으로 작성해 주세요. "
                + "시간은 반드시 HH:MM으로 작성해주세요."
                + "응답은 반드시 완전한 JSON 형식이어야 하며, JSON 외의 내용은 포함하지 마세요. 형식은 다음과 같습니다:\n" + "```json\n" + "{\n"
                + "  \"day1\": [\n"
                + "    {\"name\": \"장소명\", \"lat\": 위도, \"lng\": 경도, \"description\": \"설명\", \"departTime\": \"출발시간\", \"arriveTime\": \"도착시간\", \"type\": \"관광지/식당/카페/숙박\"}\n"
                + "  ],\n" + "  \"day2\": [...],\n" + "  ...\n" + "  \"day%d\": [...]\n" + "}\n" + "```", days,
                ageGroup, gender, groupSize, theme, startDate, endDate, days);
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
}