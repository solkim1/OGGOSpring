package com.oggo.planmaker.model;

public class ScheduleJson {
    private String userId; // 사용자 ID
    private String title; // 일정 제목
    private String scheNum; // 일정 구분을 위한 고유 UUID
    private String startDate; // 일정 시작 날짜
    private String endDate; // 일정 종료 날짜
    private String isBusiness; // 출장 여부
    private String name; // 장소 이름
    private String description; // 장소 설명
    private String departTime; // 출발 시간
    private String arriveTime; // 도착 시간
    private Double lat; // 위도
    private Double lng; // 경도
    private String type; // 장소 유형 (예: 관광지, 식당 등)
    private String scheduleDesc;


   @Override
   public String toString() {
      return "ScheduleJson [userId=" + userId + ", title=" + title + ", scheNum=" + scheNum + ", startDate="
            + startDate + ", endDate=" + endDate + ", isBusiness=" + isBusiness + ", name=" + name
            + ", description=" + description + ", departTime=" + departTime + ", arriveTime=" + arriveTime
            + ", lat=" + lat + ", lng=" + lng + ", type=" + type + ", scheduleDesc=" + scheduleDesc + "]";
   }
   
    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScheNum() {
        return scheNum;
    }

    public void setScheNum(String scheNum) {
        this.scheNum = scheNum;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getIsBusiness() {
        return isBusiness;
    }

    public void setIsBusiness(String isBusiness) {
        this.isBusiness = isBusiness;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartTime() {
        return departTime;
    }

    public void setDepartTime(String departTime) {
        this.departTime = departTime;
    }

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    

   public String getScheduleDesc() {
      return scheduleDesc;
   }

   public void setScheduleDesc(String scheduleDesc) {
      this.scheduleDesc = scheduleDesc;
   }
    
}
