package com.oggo.planmaker.service;

import com.oggo.planmaker.mapper.ScheduleMapper;
import com.oggo.planmaker.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/completions";
    private static final String OPENAI_API_KEY = "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";

    // 여행 선호도 저장 메서드
    public void generateSchedule(Map<String, String> preferences, String additionalData) {
        if (preferences.get("user_id") == null) {
            throw new IllegalArgumentException("user_id cannot be null");
        }
        // Step 1: Save travel preferences to the database
        scheduleMapper.insertTravelPreferences(preferences);

        // Step 2: Use OpenAI API (or any other service) to generate a travel schedule based on preferences
        // Assuming additionalData would be the result from the AI model or any other processing
        
        // Save the generated schedule to the database
        if (additionalData != null) {
            scheduleMapper.insertGeneratedSchedule(additionalData);
        }
    }

    // 일정 생성 메서드
    public void generateSchedule(Map<String, String> preferences) {
        // OpenAI API 요청을 위한 설정
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(OPENAI_API_KEY);

        // OpenAI API에 보낼 요청 데이터 생성
        String prompt = generatePrompt(preferences);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "text-davinci-003");
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 1000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            // OpenAI API 호출
            Map<String, Object> response = restTemplate.postForObject(OPENAI_API_URL, request, Map.class);

            if (response != null && response.containsKey("choices")) {
                String aiGeneratedText = ((List<Map<String, String>>) response.get("choices")).get(0).get("text");

                // AI로부터 받은 텍스트를 바탕으로 일정 저장
                saveGeneratedSchedule(aiGeneratedText, preferences);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generatePrompt(Map<String, String> preferences) {
        // OpenAI API에 보낼 프롬프트를 생성합니다.
        return "Create a travel schedule based on these preferences: " + preferences.toString();
    }

    private void saveGeneratedSchedule(String aiGeneratedText, Map<String, String> preferences) {
        // aiGeneratedText를 파싱하고, tb_schedule 테이블에 데이터를 저장하는 로직 구현
        // 이 예제에서는 간단히 설명 문자열로 저장합니다.

        // 예시: OpenAI가 생성한 텍스트를 스케줄로 저장
        String scheTitle = "AI Generated Schedule";
        String scheDesc = aiGeneratedText;
        String scheStDt = preferences.get("trav_sche");
        String scheEdDt = preferences.get("trav_sche_end");
        String isBusiness = "N";
        String isImportance = "N";

        // 실제로는 JSON 파싱 등 로직을 통해 schedule을 구성해야 함.
        Schedule schedule = new Schedule();
        schedule.setUserId("user"); // 예시로 사용자 ID를 하드코딩
        schedule.setScheTitle(scheTitle);
        schedule.setScheDesc(scheDesc);
        schedule.setScheStDt(scheStDt);
        schedule.setScheEdDt(scheEdDt);
        schedule.setIsBusiness(isBusiness);
        schedule.setIsImportance(isImportance);

        // scheduleMapper를 통해 DB에 저장
        scheduleMapper.insertSchedule(schedule);
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

    public void toggleImportance(int scheNum) {
        scheduleMapper.updateImportanceByScheNum(scheNum);
    }

    public void deleteSchedule(int scheNum) {
        scheduleMapper.deleteByScheNum(scheNum);
    }

    public void updateSchedule(int scheNum, String scheTitle, String scheDesc) {
        scheduleMapper.updateSchedule(scheNum, scheTitle, scheDesc);
    }
}
