package com.oggo.planmaker.model;

public class Poi {
    private int poiIdx;
    private String poiName;
    private String poiType;
    private double lat;
    private double lng;
    private String poiDesc;

    // Getters and Setters
    public int getPoiIdx() {
        return poiIdx;
    }

    public void setPoiIdx(int poiIdx) {
        this.poiIdx = poiIdx;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public String getPoiType() {
        return poiType;
    }

    public void setPoiType(String poiType) {
        this.poiType = poiType;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPoiDesc() {
        return poiDesc;
    }

    public void setPoiDesc(String poiDesc) {
        this.poiDesc = poiDesc;
    }
}
