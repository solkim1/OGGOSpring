package com.oggo.planmaker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Poi {

    private int poi_idx;
    private String poi_type;
    private String poi_name;
    private String poi_desc;
    private String poi_addr;
    private String poi_region;
    private double lat;
    private double lng;
    private String created_at;
	public int getPoi_idx() {
		return poi_idx;
	}
	public void setPoi_idx(int poi_idx) {
		this.poi_idx = poi_idx;
	}
	public String getPoi_type() {
		return poi_type;
	}
	public void setPoi_type(String poi_type) {
		this.poi_type = poi_type;
	}
	public String getPoi_name() {
		return poi_name;
	}
	public void setPoi_name(String poi_name) {
		this.poi_name = poi_name;
	}
	public String getPoi_desc() {
		return poi_desc;
	}
	public void setPoi_desc(String poi_desc) {
		this.poi_desc = poi_desc;
	}
	public String getPoi_addr() {
		return poi_addr;
	}
	public void setPoi_addr(String poi_addr) {
		this.poi_addr = poi_addr;
	}
	public String getPoi_region() {
		return poi_region;
	}
	public void setPoi_region(String poi_region) {
		this.poi_region = poi_region;
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
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}


}