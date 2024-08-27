package com.oggo.planmaker.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.oggo.planmaker.model.Schedule;

@Mapper
public interface ScheduleMapper {

    List<Schedule> findAllSchedulesByUserId(@Param("userId") String userId);

    List<Schedule> findByBusinessFlag(@Param("userId") String userId, @Param("isBusiness") String isBusiness);

    List<Schedule> findImportantSchedules(@Param("userId") String userId);

    void updateImportanceByScheNum(@Param("scheNum") int scheNum);

    void deleteByScheNum(@Param("scheNum") int scheNum);

    void updateSchedule(@Param("scheNum") int scheNum, @Param("scheTitle") String scheTitle, @Param("scheDesc") String scheDesc);

    void insertSchedule(Schedule schedule);  // 일정 저장 쿼리 추가
    
 
    
    @Select("SELECT poi_idx, poi_type, poi_name, poi_desc, poi_addr, poi_region, lat, lng FROM tb_poi")
    String getPoiDataAsCSV();
    
    void insertTravelPreferences(@Param("preferences") Map<String, String> preferences);

    void insertGeneratedSchedule(String scheduleData);
}
