package com.example.helpers;

public class Like {

	private String bounce_id;
	private Integer sender_id;
	private Integer bounce_owner;
	private Integer option;
	private String sender_login;

	public Like(String bounce_id, Integer sender_id, String sender_login,
			Integer bounce_owner, Integer option) {
		this.bounce_id = bounce_id;
		this.sender_id = sender_id;
		this.sender_login = sender_login;
		this.bounce_owner = bounce_owner;
		this.option = option;
	}

	public int getOption() {
		return option;
	}

	public int getBounceOwner() {
		return bounce_owner;
	}

	public int getSenderId() {
		return sender_id;
	}

	public String getBounceId() {
		return bounce_id;
	}

	public String getSenderLogin() {
		return sender_login;
	}

}
