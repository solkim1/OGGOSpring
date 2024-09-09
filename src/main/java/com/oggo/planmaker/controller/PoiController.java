package com.oggo.planmaker.controller;


import java.util.List;
import java.util.Map;
import java.util.Optional;


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
    


    @Autowired
    private PoiService poiService;

    @GetMapping("/search")
    public ResponseEntity<?> searchPoi(@RequestParam String name) {

        try {
            Optional<Poi> poiOpt = poiService.findPoiByName(name);
            return poiOpt
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("POI 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/theme")
    public ResponseEntity<?> getPoiByTheme(
            @RequestParam String theme,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageGroup) {
        try {
            List<Poi> pois = poiService.findPoiByThemeAndDemographics(theme, gender, ageGroup);
            return ResponseEntity.ok(pois);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("테마별 POI 검색 중 오류가 발생했습니다: " + e.getMessage());

        }
    }

    @PostMapping
    public ResponseEntity<?> addPoi(@RequestBody Poi poi) {

        try {
            poiService.insertPoi(poi);
            return ResponseEntity.ok("POI가 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("POI 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/grouped")
    public ResponseEntity<?> getGroupedPoiData(
        @RequestParam String theme,
        @RequestParam String gender,
        @RequestParam String ageGroup,
        @RequestParam int days) {
        try {
            Map<String, List<Map<String, Object>>> groupedData = poiService.getGroupedPoiData(theme, gender, ageGroup, days);
            return ResponseEntity.ok(groupedData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("그룹화된 POI 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


}



