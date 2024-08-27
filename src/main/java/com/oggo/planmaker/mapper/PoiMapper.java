package com.oggo.planmaker.mapper;

import com.oggo.planmaker.model.Poi;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PoiMapper {
    List<Poi> findAll();
    Poi findById(int poi_idx);
}
