package com.picktr.example.helpers;

public class Like {

	private long ID;
	private String bounce_id;
	private Integer sender_id;
	private Integer option;

	public Like(long ID, String bounce_id, Integer sender_id, Integer option) {
		this.ID = ID;
		this.bounce_id = bounce_id;
		this.sender_id = sender_id;
		this.option = option;
	}

	public Like(String bounce_id, Integer sender_id, Integer option) {
		this.bounce_id = bounce_id;
		this.sender_id = sender_id;
		this.option = option;
	}

	public long getID() {
		return this.ID;
	}

	public void setID(long ID) {
		this.ID = ID;
	}

	public int getOption() {
		return option;
	}

	public void setOption(int option) {
		this.option = option;
	}

	public int getSenderId() {
		return sender_id;
	}

	public void setSenderId(int sender_id) {
		this.sender_id = sender_id;
	}

	public String getBounceId() {
		return bounce_id;
	}

	public void setBounceId(String bounce_id) {
		this.bounce_id = bounce_id;
	}

}
