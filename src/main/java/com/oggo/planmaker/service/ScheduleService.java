package com.oggo.planmaker.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

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
import com.oggo.planmaker.model.Poi;
import com.oggo.planmaker.model.Schedule;

@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private PoiMapper poiMapper;

    @Autowired
    private OpenAIService openAIService;

    private final Map<String, Map<String, List<Map<String, Object>>>> temporaryItineraries = new ConcurrentHashMap<>();

    public CompletableFuture<Map<String, List<Map<String, Object>>>> generateItinerary(String userId, int days, String ageGroup, String gender, String groupSize, String theme, String startDate, String endDate) {
        String prompt = generatePrompt(days, ageGroup, gender, groupSize, theme, startDate, endDate);
        
        return openAIService.generateItinerary(prompt)
            .thenApply(aiGeneratedText -> {
                log.info("AI Generated Text: " + aiGeneratedText);
                Map<String, List<Map<String, Object>>> itinerary = parseAIResponse(aiGeneratedText);

                if (itinerary == null) {
                    log.error("Itinerary parsing failed. Check the AI response.");
                    throw new RuntimeException("일정 생성 중 오류가 발생했습니다. AI 응답을 확인하세요.");
                }

                temporaryItineraries.put(userId, itinerary);
                return itinerary;
            });
    }

    @Transactional
    public void saveItinerary(String userId, String startDate, String endDate) {
        Map<String, List<Map<String, Object>>> itinerary = temporaryItineraries.get(userId);
        if (itinerary == null) {
            throw new RuntimeException("No temporary itinerary found for user: " + userId);
        }

        int scheNum = scheduleMapper.getLastScheNum() + 1;
        String scheTitle = "서울 여행"; // 타이틀 생성 로직 필요 시 추가
        String scheDesc = String.format("%s부터 %s까지의 여행 일정", startDate, endDate);

        for (Map.Entry<String, List<Map<String, Object>>> entry : itinerary.entrySet()) {
            List<Map<String, Object>> dayPlans = entry.getValue();

            for (Map<String, Object> plan : dayPlans) {
                String poiName = (String) plan.get("name");
                Poi poi = poiMapper.findByName(poiName);
                if (poi == null) {
                    poi = new Poi();
                    poi.setPoiName(poiName);
                    poi.setLat(Double.parseDouble(plan.get("lat").toString()));
                    poi.setLng(Double.parseDouble(plan.get("lng").toString()));
                    poi.setPoiDesc((String) plan.get("description"));
                    poiMapper.insertPOI(poi);

                    poi = poiMapper.findByName(poiName);
                    if (poi == null) {
                        throw new RuntimeException("Failed to insert new POI: " + poiName);
                    }
                }

                Schedule schedule = new Schedule();
                schedule.setUserId(userId);
                schedule.setScheTitle(scheTitle);
                schedule.setScheDesc(scheDesc);
                schedule.setScheStDt(startDate);
                schedule.setScheEdDt(endDate);
                schedule.setScheStTm((String) plan.get("departTime"));
                schedule.setScheEdTm((String) plan.get("arriveTime"));
                schedule.setIsBusiness("N");
                schedule.setIsImportance("N");
                schedule.setPoiIdx(poi.getPoiIdx());
                schedule.setScheNum(scheNum);

                scheduleMapper.insertSchedule(schedule);
            }
        }

        temporaryItineraries.remove(userId);
    }

    public List<Schedule> getUserSchedules(String userId) {
        return scheduleMapper.findAllSchedulesByUserId(userId);
    }

    @Transactional
    public void updateSchedule(Schedule schedule) {
        scheduleMapper.updateSchedule(schedule.getScheIdx(), schedule.getScheTitle(), schedule.getScheDesc());
    }

    @Transactional
    public void deleteSchedule(int scheduleId) {
        scheduleMapper.deleteByScheNum(scheduleId);
    }

    private String generatePrompt(int days, String ageGroup, String gender, String groupSize, String theme, String startDate, String endDate) {
        return String.format(
            "다음 조건을 고려하여 %d일 동안의 일정을 생성해주세요: " +
            "연령대: %s, 성별: %s, 그룹 크기: %s, 테마: %s, 시작일: %s, 종료일: %s. " +
            "일정은 매일 아침 9시부터 저녁 9시 사이에 구성되며, 각 일정은 관광지, 식당, 카페, " +
            "숙박을 포함하여 다양하게 배치해 주세요. 숙박 장소는 가급적 동일하게 유지하되, " +
            "일정이 너무 먼 곳에 있을 경우에는 변경해도 됩니다. " +
            "응답을 반드시 완전한 JSON 형식으로 작성하고, JSON 외의 내용은 포함하지 말아주세요. " +
            "응답을 다음과 같은 JSON 코드 블록으로 작성하세요:\n" +
            "```json\n" +
            "{\n" +
            "  \"day1\": [\n" +
            "    {\n" +
            "      \"name\": \"장소명\",\n" +
            "      \"lat\": 위도,\n" +
            "      \"lng\": 경도,\n" +
            "      \"address\": \"주소\",\n" +
            "      \"description\": \"설명\",\n" +
            "      \"departTime\": \"출발시간\",\n" +
            "      \"arriveTime\": \"도착시간\",\n" +
            "      \"type\": \"관광지/식당/카페/숙박\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"day2\": [...],\n" +
            "  ...\n" +
            "  \"day%d\": [...]\n" +
            "}\n" +
            "```",
            days, ageGroup, gender, groupSize, theme, startDate, endDate, days
        );
    }

    private Map<String, List<Map<String, Object>>> parseAIResponse(String aiGeneratedText) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String jsonText = aiGeneratedText.trim();
            // JSON 코드 블록 제거
            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }

            jsonText = jsonText.trim();

            // JSON 객체의 시작과 끝 확인
            if (jsonText.startsWith("{") && jsonText.endsWith("}")) {
                return objectMapper.readValue(jsonText, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
            } else {
                log.error("Invalid JSON format: " + jsonText);
                throw new JsonProcessingException("Invalid JSON format") {};
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: " + e.getMessage(), e);
            return null;
        }
    }
}