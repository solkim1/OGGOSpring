package com.oggo.planmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oggo.planmaker.model.Schedule;

@Mapper
public interface ScheduleMapper {

	List<Schedule> findAllSchedulesByUserId(@Param("userId") String userId);

    List<Schedule> findByBusinessFlag(@Param("userId") String userId, @Param("isBusiness") String isBusiness);

    List<Schedule> findImportantSchedules(@Param("userId") String userId);

    void updateImportanceByScheNum(@Param("scheNum") String scheNum);

    void deleteByScheNum(@Param("scheNum") String scheNum);

    void updateSchedule(@Param("scheNum") String scheNum, @Param("scheTitle") String scheTitle, @Param("scheDesc") String scheDesc);

    void insertSchedule(Schedule schedule);  // 일정 저장 쿼리 추가
    
    int getLastScheNum();
}
