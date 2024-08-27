package com.oggo.planmaker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateSchedule(Map<String, String> preferences, String csvData) {
        String url = "https://api.openai.com/v1/engines/davinci-codex/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", createPrompt(preferences, csvData));
        requestBody.put("max_tokens", 1000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to generate schedule: " + response.getStatusCode());
        }
    }

    private String createPrompt(Map<String, String> preferences, String csvData) {
        StringBuilder prompt = new StringBuilder("Using the following travel preferences and data, create a travel schedule:\n");
        preferences.forEach((key, value) -> prompt.append(key).append(": ").append(value).append("\n"));
        prompt.append("CSV Data:\n").append(csvData);
        return prompt.toString();
    }
}
