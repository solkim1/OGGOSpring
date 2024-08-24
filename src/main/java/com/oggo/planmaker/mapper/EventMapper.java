package com.oggo.planmaker.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.oggo.planmaker.model.Event;

@Mapper
public interface EventMapper {
    void insertEvents(List<Event> events);
    List<Event> selectAllEvents();
}