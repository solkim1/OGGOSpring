package com.oggo.planmaker.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.oggo.planmaker.mapper.PoiMapper;
import com.oggo.planmaker.model.Poi;

@Service
public class PoiService {
    @Autowired
    private PoiMapper poiMapper;

    public Map<String, List<Map<String, Object>>> getPoiDataGroupedByDay(int days) {
        List<Poi> pois = poiMapper.findAll();
        Map<String, List<Map<String, Object>>> poiData = new HashMap<>();

        for (int day = 1; day <= days; day++) {
            String dayKey = "day" + day;
            String dayString = String.valueOf(day); // 람다에서 사용할 dayString 변수 생성
            List<Map<String, Object>> dayPois = pois.stream()
                    .filter(poi -> poi.getPoi_region().endsWith(dayString))  // day 대신 dayString 사용
                    .map(poi -> {
                        Map<String, Object> poiMap = new HashMap<>();
                        poiMap.put("name", poi.getPoi_name());
                        poiMap.put("lat", poi.getLat());
                        poiMap.put("lng", poi.getLng());
                        poiMap.put("address", poi.getPoi_addr());
                        poiMap.put("description", poi.getPoi_desc());
                        poiMap.put("departTime", "10:00"); 
                        poiMap.put("arriveTime", "12:00");
                        return poiMap;
                    })
                    .collect(Collectors.toList());

            poiData.put(dayKey, dayPois);
        }

        return poiData;
    }
}
