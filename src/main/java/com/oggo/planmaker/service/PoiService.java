package com.oggo.planmaker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oggo.planmaker.mapper.PoiMapper;
import com.oggo.planmaker.model.Poi;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PoiService {

    @Autowired
    private PoiMapper poiMapper;

    public Optional<Poi> findPoiByName(String name) {
        return Optional.ofNullable(poiMapper.findByName(name));
    }

    @Transactional
    public void insertPoi(Poi poi) {
        poiMapper.insertPOI(poi);
    }

    public List<Poi> findPoiByThemeAndDemographics(String theme, String gender, String ageGroup) {
        return poiMapper.findByThemeAndDemographics(theme, gender, ageGroup);
    }

    public Map<String, List<Map<String, Object>>> getGroupedPoiData(String theme, String gender, String ageGroup, int days) {
        // Implement the logic to group POI data based on the provided parameters
        // For example, you might group POIs by day, with certain categories of POIs appearing on specific days
        // You would use the findPoiByThemeAndDemographics method and additional logic to achieve this
        // This method would return a map where the key is the day (e.g., "day1", "day2") and the value is a list of POIs for that day

        // Example (pseudo-code):
        // Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();
        // for (int day = 1; day <= days; day++) {
        //     List<Map<String, Object>> poisForDay = ... // fetch and group POIs for this day
        //     groupedData.put("day" + day, poisForDay);
        // }
        // return groupedData;
        return null; // Replace this with your implementation
    }
}
