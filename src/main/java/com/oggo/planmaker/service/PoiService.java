package com.oggo.planmaker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oggo.planmaker.mapper.PoiMapper;
import com.oggo.planmaker.model.Poi;

@Service
public class PoiService {

    private static final Logger log = LoggerFactory.getLogger(PoiService.class);

    @Autowired
    private PoiMapper poiMapper;

    public Poi findPoiByName(String name) {
        log.debug("Searching for POI by name: {}", name);
        Poi poi = poiMapper.findByName(name);
        if (poi != null) {
            log.debug("POI found: {}", poi);
        } else {
            log.debug("POI not found for name: {}", name);
        }
        return poi;
    }

    @Transactional
    public void insertPoi(Poi poi) {
        log.debug("Inserting new POI: {}", poi);
        poiMapper.insertPOI(poi);
        log.debug("POI inserted successfully");
    }

    @Transactional
    public Poi findOrCreatePoi(String name, double lat, double lng, String description, String type) {
        log.debug("Finding or creating POI: {}", name);
        Poi poi = poiMapper.findByName(name);
        if (poi == null) {
            log.debug("POI not found, creating new one: {}", name);
            poi = new Poi();
            poi.setPoiName(name);
            poi.setLat(lat);
            poi.setLng(lng);
            poi.setPoiDesc(description);
            poi.setPoiType(type);
            try {
                poiMapper.insertPOI(poi);
                log.debug("New POI inserted: {}", poi);
            } catch (Exception e) {
                log.error("Error inserting new POI: {}", name, e);
                throw new RuntimeException("POI 삽입 중 오류 발생: " + name, e);
            }
            
            // 새로 삽입된 POI의 정보를 다시 조회
            poi = poiMapper.findByName(name);
            if (poi == null) {
                log.error("Failed to retrieve newly inserted POI: {}", name);
                throw new RuntimeException("새로 삽입된 POI 조회 실패: " + name);
            }
        } else {
            log.debug("Existing POI found: {}", poi);
        }
        return poi;
    }
}