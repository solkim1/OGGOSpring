package com.oggo.planmaker.mapper;

import com.oggo.planmaker.model.Poi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface PoiMapper {
    Poi findByName(String poiName);
    
    @Options(useGeneratedKeys = true, keyProperty = "poiIdx")
    void insertPOI(Poi poi);

    List<Poi> findByThemeAndDemographics(String theme, String gender, String ageGroup);


}


