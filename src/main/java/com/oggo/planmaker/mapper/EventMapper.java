package com.oggo.planmaker.mapper;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.google.api.client.util.DateTime; // Google API의 DateTime
import com.google.api.services.calendar.model.Event;
import com.oggo.planmaker.model.CustomEvent;

// MyBatis Mapper 인터페이스
@Mapper
public interface EventMapper {
    
    // 모든 이벤트를 선택하는 SQL 쿼리
    @Select("SELECT s.sche_st_dt AS scheStDt, s.sche_st_tm AS scheStTm, s.sche_ed_dt AS scheEdDt, s.sche_ed_tm AS scheEdTm, p.poi_idx AS poiidx, p.poi_name AS poiName, s.sche_desc AS scheDesc, s.sche_num AS schenum FROM tb_schedule s JOIN fake_poi p ON s.poi_idx = p.poi_idx")
    List<CustomEvent> selectAllEvents(); 

    @Delete("DELETE FROM tb_schedule WHERE poi_idx = #{poiidx} AND sche_num = #{schenum}")
    void deleteEvent(@Param("schenum") int schenum);

    // 이벤트를 삽입하는 메소드
    void insertEvents(List<CustomEvent> events);

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    ZoneId zoneId = ZoneId.of("Asia/Seoul");

    // Google Calendar의 Event를 CustomEvent로 변환하는 메소드
    CustomEvent mapGoogleEventToDTO(Event googleEvent);

    // CustomEvent를 Google Calendar의 Event로 변환하는 메소드
    Event mapDTOToGoogleEvent(CustomEvent eventDTO);

    // LocalDate와 LocalTime을 Google API의 DateTime으로 변환
    default DateTime convertToDateTime(LocalDate date, LocalTime time) {
        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        return new DateTime(localDateTime.toString());
    }

    // 문자열을 LocalDateTime으로 변환
    default LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
    }
}

