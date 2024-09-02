package com.oggo.planmaker.model;

import lombok.Data;

@Data
public class Schedule {
    private int scheIdx;
    private String userId;
    private String scheTitle;
    private String scheDesc;
    private String scheStDt;
    private String scheStTm;
    private String scheEdDt;
    private String scheEdTm;
    private String isBusiness;
    private String scheColor;
    private String isImportance;
    private int poiIdx;
    private String scheNum;


    // Getters and Setters

    public int getPoiIdx() {
		return poiIdx;
	}

	public void setPoiIdx(int poiIdx) {
		this.poiIdx = poiIdx;
	}



	public String getScheNum() {
		return scheNum;
	}

	public void setScheNum(String scheNum) { 


		this.scheNum = scheNum;
	}

	public int getScheIdx() {
        return scheIdx;
    }

    public void setScheIdx(int scheIdx) {
        this.scheIdx = scheIdx;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScheTitle() {
        return scheTitle;
    }

    public void setScheTitle(String scheTitle) {
        this.scheTitle = scheTitle;
    }

    public String getScheDesc() {
        return scheDesc;
    }

    public void setScheDesc(String scheDesc) {
        this.scheDesc = scheDesc;
    }

    public String getScheStDt() {
        return scheStDt;
    }

    public void setScheStDt(String scheStDt) {
        this.scheStDt = scheStDt;
    }

    public String getScheStTm() {
        return scheStTm;
    }

    public void setScheStTm(String scheStTm) {
        this.scheStTm = scheStTm;
    }

    public String getScheEdDt() {
        return scheEdDt;
    }

    public void setScheEdDt(String scheEdDt) {
        this.scheEdDt = scheEdDt;
    }

    public String getScheEdTm() {
        return scheEdTm;
    }

    public void setScheEdTm(String scheEdTm) {
        this.scheEdTm = scheEdTm;
    }

    public String getIsBusiness() {
        return isBusiness;
    }

    public void setIsBusiness(String isBusiness) {
        this.isBusiness = isBusiness;
    }


    public String getIsImportance() {

        return isImportance;
    }

    public void setIsImportance(String isImportance) {
        this.isImportance = isImportance;
    }
}
