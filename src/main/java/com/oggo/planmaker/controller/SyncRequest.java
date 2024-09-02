package com.oggo.planmaker.controller;

public class SyncRequest {
    private String accessToken;

    // 기본 생성자
    public SyncRequest() {}

    // 생성자
    public SyncRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getter와 Setter
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
