package com.oggo.planmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.oggo.planmaker.model.Schedule;

@Mapper
public interface ScheduleMapper {

	void insertSchedule(Schedule schedule);
    List<Schedule> findAllSchedulesByUserId(String userId);
    List<Schedule> findByBusinessFlag(String userId, String isBusiness);
    List<Schedule> findImportantSchedules(String userId);
    void updateImportanceByScheNum(int scheNum);
    void deleteByScheNum(int scheNum);
    void updateSchedule(int scheIdx, String scheTitle, String scheDesc);
    int getLastScheNum();
}
