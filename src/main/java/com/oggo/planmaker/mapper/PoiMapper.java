package com.oggo.planmaker.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oggo.planmaker.model.Poi;

@Mapper
public interface PoiMapper {
    
    Poi findByName(@Param("poiName") String poiName);
    
    void insertPOI(Poi poi);
    
    List<Poi> findByTheme(@Param("theme") String theme);
    
    
}