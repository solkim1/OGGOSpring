package com.oggo.planmaker.service;


import com.oggo.planmaker.mapper.PoiMapper;
import com.oggo.planmaker.model.Poi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class PoiService {


    private static final Logger logger = LoggerFactory.getLogger(PoiService.class);


    @Autowired
    private PoiMapper poiMapper;

    public Optional<Poi> findPoiByName(String name) {

        logger.info("Searching for POI with name: {}", name);
        try {
            return Optional.ofNullable(poiMapper.findByName(name));
        } catch (Exception e) {
            logger.error("Error occurred while searching for POI with name: {}", name, e);
            throw new RuntimeException("Error occurred while searching for POI", e);
        }
    }

    public List<Poi> findPoiByThemeAndDemographics(String theme, String gender, String ageGroup) {
        logger.info("Searching for POIs with theme: {}, gender: {}, ageGroup: {}", theme, gender, ageGroup);
        try {
            return poiMapper.findByThemeAndDemographics(theme, gender, ageGroup);
        } catch (Exception e) {
            logger.error("Error occurred while searching for POIs with theme: {}", theme, e);
            throw new RuntimeException("Error occurred while searching for POIs", e);
        }
    }

    public void insertPoi(Poi poi) {
        logger.info("Inserting new POI: {}", poi.getPoiName());
        try {
            poiMapper.insertPOI(poi);
            logger.info("Successfully inserted POI: {}", poi.getPoiName());
        } catch (Exception e) {
            logger.error("Error occurred while inserting POI: {}", poi.getPoiName(), e);
            throw new RuntimeException("Error occurred while inserting POI", e);
        }
    }

    public Map<String, List<Map<String, Object>>> getGroupedPoiData(String theme, String gender, String ageGroup, int days) {
        logger.info("Getting grouped POI data for theme: {}, gender: {}, ageGroup: {}, days: {}", theme, gender, ageGroup, days);
        try {
            List<Poi> pois = poiMapper.findByThemeAndDemographics(theme, gender, ageGroup);
            // 여기서 필요에 따라 POI 데이터를 그룹화하거나 필터링할 수 있습니다.
            // 예를 들어, 일수에 따라 POI를 선택하거나 그룹화할 수 있습니다.
            return groupPoisByDay(pois, days);
        } catch (Exception e) {
            logger.error("Error occurred while getting grouped POI data", e);
            throw new RuntimeException("Error occurred while getting grouped POI data", e);
        }
    }

    private Map<String, List<Map<String, Object>>> groupPoisByDay(List<Poi> pois, int days) {
        Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();
        Random random = new Random();

        for (int i = 1; i <= days; i++) {
            String day = "day" + i;
            List<Map<String, Object>> dayPois = new ArrayList<>();

            // 각 날짜마다 3-5개의 POI를 무작위로 선택
            int poiCount = random.nextInt(3) + 3;
            for (int j = 0; j < poiCount && j < pois.size(); j++) {
                Poi poi = pois.get(random.nextInt(pois.size()));
                Map<String, Object> poiMap = new HashMap<>();
                poiMap.put("name", poi.getPoiName());
                poiMap.put("lat", poi.getLat());
                poiMap.put("lng", poi.getLng());
                poiMap.put("address", poi.getPoiDesc());  // 임시로 설명을 주소로 사용
                poiMap.put("description", poi.getPoiDesc());
                dayPois.add(poiMap);
            }

            groupedData.put(day, dayPois);
        }

        return groupedData;
    }
}

