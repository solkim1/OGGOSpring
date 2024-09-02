package com.oggo.planmaker.model;

import lombok.Data;

@Data
public class User {

	private String userId;

	private String userNick;

	private String userPw;

	private String userEmail;

	private char isGoogle;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserNick() {
		return userNick;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}

	public String getUserPw() {
		return userPw;
	}

	public void setUserPw(String userPw) {
		this.userPw = userPw;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public char getIsGoogle() {
		return isGoogle;
	}

	public void setIsGoogle(char isGoogle) {
		this.isGoogle = isGoogle;
	}

}
