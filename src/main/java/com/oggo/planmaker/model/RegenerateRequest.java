package com.oggo.planmaker.model;

import java.util.List;

public class RegenerateRequest {
    private String selectedDay;
    private List<ScheduleItem> locationData;

    // Getters and setters
    public String getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(String selectedDay) {
        this.selectedDay = selectedDay;
    }

    public List<ScheduleItem> getLocationData() {
        return locationData;
    }

    public void setLocationData(List<ScheduleItem> locationData) {
        this.locationData = locationData;
    }
}