package com.oggo.planmaker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.oggo.planmaker.model.Poi;
import com.oggo.planmaker.service.PoiService;

@RestController
@RequestMapping("/api/poi")
public class PoiController {
    
    private static final Logger logger = LoggerFactory.getLogger(PoiController.class);
    
    @Autowired
    private PoiService poiService;

    @GetMapping("/search")
    public ResponseEntity<?> searchPoi(@RequestParam String name) {
        logger.info("Searching for POI with name: {}", name);
        try {
            Poi poi = poiService.findPoiByName(name);
            if (poi != null) {
                logger.info("POI found: {}", poi);
                return ResponseEntity.ok(poi);
            } else {
                logger.info("POI not found: {}", name);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error occurred while searching POI: {}", name, e);
            return ResponseEntity.internalServerError().body("POI 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addPoi(@RequestBody Poi poi) {
        logger.info("Attempting to add new POI: {}", poi);
        try {
            poiService.insertPoi(poi);
            logger.info("POI successfully added: {}", poi);
            return ResponseEntity.ok("POI가 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            logger.error("Error occurred while adding POI: {}", poi, e);
            return ResponseEntity.internalServerError().body("POI 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/find-or-create")
    public ResponseEntity<?> findOrCreatePoi(@RequestBody Poi poi) {
        logger.info("Attempting to find or create POI: {}", poi);
        try {
            Poi result = poiService.findOrCreatePoi(poi.getPoiName(), poi.getLat(), poi.getLng(), poi.getPoiDesc(), poi.getPoiType());
            logger.info("POI found or created: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error occurred while finding or creating POI: {}", poi, e);
            return ResponseEntity.internalServerError().body("POI 조회 또는 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}