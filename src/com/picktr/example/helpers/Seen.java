package com.picktr.example.helpers;

public class Seen {

	private long ID;
	private String bounce_id;
	private Integer contactID;

	public Seen(String bounce_id, Integer contactID) {
		this.bounce_id = bounce_id;
		this.contactID = contactID;
	}

	public Seen(long ID, String bounce_id, Integer contactID) {
		this.ID = ID;
		this.bounce_id = bounce_id;
		this.contactID = contactID;
	}

	public long getID() {
		return this.ID;
	}

	public void setID(long ID) {
		this.ID = ID;
	}

	public String getBounceID() {
		return this.bounce_id;
	}

	public void setBounceID(String bounce_id) {
		this.bounce_id = bounce_id;
	}

	public Integer getContactID() {
		return this.contactID;
	}

	public void setContactID(Integer contactID) {
		this.contactID = contactID;
	}

}
