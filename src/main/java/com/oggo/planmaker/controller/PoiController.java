package com.oggo.planmaker.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oggo.planmaker.service.PoiService;

@RestController
@RequestMapping("/api/poi")
public class PoiController {

    @Autowired
    private PoiService poiService;

    @GetMapping("/grouped")
    public Map<String, List<Map<String, Object>>> getGroupedPoiData() {
        return poiService.getPoiDataGroupedByDay(6); // 예시로 6일치 데이터 제공
    }
}
