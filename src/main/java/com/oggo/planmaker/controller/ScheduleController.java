package com.oggo.planmaker.controller;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oggo.planmaker.mapper.ScheduleMapper;
import com.oggo.planmaker.model.Schedule;
import com.oggo.planmaker.model.ScheduleJson;
import com.oggo.planmaker.service.OpenAIService;
import com.oggo.planmaker.service.ScheduleService;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);



    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired

	private ScheduleMapper scheduleMapper;
    
    @Autowired
    private OpenAIService openAIService;

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
    
    @GetMapping("/patchschedule")
	public String patchschedule(@RequestParam("scheNum") String scheNum) {
		System.out.println(scheNum);

		List<ScheduleJson> schedule = scheduleMapper.patchschedule(scheNum);

//        for (ScheduleJson sche : schedule) {
//            System.out.println(sche.toString());
//        }

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

				if (sche.getIsBusiness().equals("Y")) {
					scheduleNode.put("isBusiness", true);
				} else {
					scheduleNode.put("isBusiness", false);
				}
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

    @PostMapping("/travel/recall")
    public CompletableFuture<ResponseEntity<Map<String, List<Map<String, Object>>>>> regenerateTravelSchedule(
            @RequestBody Map<String, List<Map<String, Object>>> excludedItems) {
        logger.info("Received request to regenerate schedule with excluded items: {}", excludedItems);

        // OpenAI 프롬프트 생성
        StringBuilder promptBuilder = new StringBuilder("안녕하세요! 일정 교체를 위한 AI입니다. 다음 정보를 바탕으로 새로운 일정을 제안하겠습니다.\n\n");

        // 기존 일정 정보
        promptBuilder.append("제거할 일정 정보:\n");
        excludedItems.forEach((day, items) -> {
            items.forEach(item -> {
                promptBuilder.append(String.format("- 날짜: %s, 이름: %s, 유형: %s, 위치: (%f, %f), 시간: %s - %s\n",
                        day, item.get("name"), item.get("type"), item.get("lat"), item.get("lng"), 
                        item.get("departTime"), item.get("arriveTime")));
            });
        });
        promptBuilder.append("\n");

        promptBuilder.append("새로운 일정 추천 시 고려할 조건:\n");
        promptBuilder.append("- 위치: 반경 3km 내외\n");
        promptBuilder.append("- 유형: 입력된 유형(type)에 맞는 장소 (관광지/식당/카페/숙박 등)\n");
        promptBuilder.append("- 시간대: 기존 일정과 동일\n");
        promptBuilder.append("- 기타: 추가 선호 조건이 있다면 제공해주세요.\n\n");

        promptBuilder.append("응답은 다음 JSON 형식으로 제공해 주세요:\n");
        promptBuilder.append("{\n  \"day1\": [\n    {\n      \"name\": \"장소명\",\n      \"lat\": 위도,\n      \"lng\": 경도,\n      \"description\": \"40자 이하의 설명\",\n      \"departTime\": \"출발시간\",\n      \"arriveTime\": \"도착시간\",\n      \"type\": \"장소 유형\"\n    }\n  ],\n  \"day2\": [...]\n}\n\n");

        promptBuilder.append("중요 조건:\n");
        promptBuilder.append("1. description은 40자 이하로 작성해 주세요.\n");
        promptBuilder.append("2. 기존 일정과 동일한 시간대 및 근처 위치에서 새로운 일정 항목을 제안해 주세요.\n");

        String finalPrompt = promptBuilder.toString(); 
        logger.info("Generated prompt for OpenAI: {}", finalPrompt);

        // OpenAI 서비스에 요청을 보내 새로운 일정을 생성
        return openAIService.generateItinerary(finalPrompt)
                .thenApply(response -> {
                    // OpenAI로부터 받은 응답에서 JSON 부분만 추출
                    String jsonPart = extractValidJson(response);
                    if (jsonPart == null) {
                        logger.error("Received response does not contain valid JSON: {}", response);
                        Map<String, List<Map<String, Object>>> errorResponse = new HashMap<>();
                        errorResponse.put("error", List.of(Map.of("message", "유효한 JSON 데이터가 없습니다.")));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                    }
                    // JSON 부분을 파싱하여 적절한 형식으로 반환
                    Map<String, List<Map<String, Object>>> result = parseOpenAIResponse(jsonPart);
                    return ResponseEntity.ok(result); // 클라이언트로 응답 반환
                })
                .exceptionally(ex -> {
                    logger.error("Error during itinerary regeneration: {}", ex.getMessage());
                    Map<String, List<Map<String, Object>>> errorResponse = new HashMap<>();
                    errorResponse.put("error", List.of(Map.of("message", "일정 재생성 중 오류가 발생했습니다: " + ex.getMessage())));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                });
    }


    @PostMapping("/business/recall")
    public CompletableFuture<ResponseEntity<Map<String, List<Map<String, Object>>>>> regenerateBusinessSchedule(
            @RequestBody Map<String, List<Map<String, Object>>> excludedItems) {
        
        logger.info("Received request to regenerate schedule with excluded items: {}", excludedItems);

        // OpenAI 프롬프트 생성
        StringBuilder promptBuilder = new StringBuilder("안녕하세요! 출장 일정을 재생성하기 위한 AI입니다. 다음 정보를 바탕으로 새로운 출장 일정을 제안하겠습니다.\n\n");

        // 기존 일정 정보 추가
        promptBuilder.append("제거할 출장 일정 정보:\n");
        excludedItems.forEach((day, items) -> items.forEach(item -> {
            promptBuilder.append(String.format("- 날짜: %s, 이름: %s, 유형: %s, 위치: (%.6f, %.6f), 시간: %s - %s\n",
                    day, item.get("name"), item.get("type"), 
                    (Double) item.get("lat"), (Double) item.get("lng"), 
                    item.get("departTime"), item.get("arriveTime")));
        }));
        promptBuilder.append("\n");

        // 새로운 출장 일정 추천 시 고려할 조건 추가
        promptBuilder.append("새로운 출장 일정 추천 시 고려할 조건:\n")
                     .append("- 지역: 기존 지역 내에서 반경 5km 내외\n")
                     .append("- 유형: 출장 관련 장소 (회의실, 오피스, 비즈니스 라운지 등)와 비즈니스 관련 활동만 포함\n")
                     .append("- 업무 시간: 기존 일정의 출장 업무 시간과 겹치지 않는 시간대로 구성\n")
                     .append("- 추가 조건: 비즈니스 미팅 및 업무에 적합한 장소와 환경을 고려해 주세요.\n\n");

        // 응답 형식 설명
        promptBuilder.append("응답은 다음 JSON 형식으로 제공해 주세요:\n")
                     .append("{\n  \"day1\": [\n    {\n      \"name\": \"장소명\",\n      \"lat\": 위도,\n      \"lng\": 경도,\n      \"description\": \"40자 이하의 설명\",\n      \"departTime\": \"출발시간\",\n      \"arriveTime\": \"도착시간\",\n      \"type\": \"장소 유형\"\n    }\n  ],\n  \"day2\": [...]\n}\n\n");

        // 중요 조건 추가
        promptBuilder.append("중요 조건:\n")
                     .append("1. description은 40자 이하로 작성해 주세요.\n")
                     .append("2. 기존 일정과 동일한 시간대 및 근처 위치에서, 비즈니스에 적합한 새로운 장소를 제안해 주세요.\n")
                     .append("3. 모든 장소는 실제로 존재하는 장소여야 하며, 허구의 장소는 포함하지 마세요.\n")
                     .append("4. 장소 정보는 신뢰할 수 있는 출처(예: Google 지도, 위키피디아)에 기반해 주세요.\n");

        String finalPrompt = promptBuilder.toString();
        logger.info("Generated prompt for OpenAI: {}", finalPrompt);

        // OpenAI 서비스에 요청을 보내 새로운 일정을 생성
        return openAIService.generateItinerary(finalPrompt)
                .thenApply(response -> {
                    String jsonPart = extractValidJson(response); // OpenAI 응답에서 JSON 추출
                    if (jsonPart == null) {
                        logger.error("Received response does not contain valid JSON: {}", response);
                        Map<String, List<Map<String, Object>>> errorResponse = Map.of("error", List.of(Map.of("message", "유효한 JSON 데이터가 없습니다.")));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                    }
                    
                    Map<String, List<Map<String, Object>>> result = parseOpenAIResponse(jsonPart); // JSON 파싱
                    return ResponseEntity.ok(result); // 클라이언트로 성공적인 응답 반환
                })
                .exceptionally(ex -> {
                    logger.error("Error during itinerary regeneration: {}", ex.getMessage());
                    Map<String, List<Map<String, Object>>> errorResponse = Map.of("error", List.of(Map.of("message", "일정 재생성 중 오류가 발생했습니다: " + ex.getMessage())));
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

