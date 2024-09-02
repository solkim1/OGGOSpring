package com.oggo.planmaker.model;

import java.sql.Date;
import java.sql.Time;

public class CustomEvent {
	/* private int poiidx; */
	private int schenum;
	private Date scheStDt;
	private Time scheStTm;
	private Date scheEdDt;
	private Time scheEdTm;
	private String poiName;
	private String scheDesc;

	
	
	public int getSchenum() {
		return schenum;
	}

	public void setSchenum(int schenum) {
		this.schenum = schenum;
	}

	/*
	 * // Getters and setters public int getPoiidx() { return poiidx; }
	 * 
	 * public void setPoiidx(int poiidx) { this.poiidx = poiidx; }
	 */

	public Date getScheStDt() {
		return scheStDt;
	}

	public void setScheStDt(Date scheStDt) {
		this.scheStDt = scheStDt;
	}

	public Time getScheStTm() {
		return scheStTm;
	}

	public void setScheStTm(Time scheStTm) {
		this.scheStTm = scheStTm;
	}

	public Date getScheEdDt() {
		return scheEdDt;
	}

	public void setScheEdDt(Date scheEdDt) {
		this.scheEdDt = scheEdDt;
	}

	public Time getScheEdTm() {
		return scheEdTm;
	}

	public void setScheEdTm(Time scheEdTm) {
		this.scheEdTm = scheEdTm;
	}

	public String getPoiName() {
		return poiName;
	}

	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}

	public String getScheDesc() {
		return scheDesc;
	}

	public void setScheDesc(String scheDesc) {
		this.scheDesc = scheDesc;
	}
}