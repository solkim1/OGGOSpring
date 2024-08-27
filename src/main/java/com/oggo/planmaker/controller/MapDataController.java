package com.oggo.planmaker.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/map")
public class MapDataController {

    // JSON 파일을 읽어서 Map으로 반환하는 메서드
    private Map<String, List<Map<String, Object>>> readTravelDataFromFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("data/POIDATA.json");

        // JSON 파일을 읽어서 Map으로 변환
        return objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<Map<String, List<Map<String, Object>>>>() {}
        );
    }

    // React에 JSON 데이터를 제공하는 엔드포인트
    @GetMapping("/mapdata")
    public Map<String, List<Map<String, Object>>> getTravelData() throws IOException {
        return readTravelDataFromFile();
    }
}