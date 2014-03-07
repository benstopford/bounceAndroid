package com.picktr.example.helpers;

public class BounceOption {

	// private variables
	private long id;
	private long bounce_db_id;
	private int optionNumber;
	private int type;
	private byte[] image;
	private String url;
	private String title;

	// Empty constructor
	public BounceOption() {

	}

	// constructor
	public BounceOption(long id, long bounceID, int optionNumber, int type,
			String title, byte[] image, String url) {
		this.id = id;
		this.bounce_db_id = bounceID;
		this.optionNumber = optionNumber;
		this.type = type;
		this.title = title;
		this.image = image;
		this.url = url;
	}

	public void setID(long id) {
		this.id = id;
	}

	public long getID() {
		return this.id;
	}

	public void setBounceID(long bounceID) {
		this.bounce_db_id = bounceID;
	}

	public long getBounceID() {
		return this.bounce_db_id;
	}

	public void setOptionNumber(int optionNumber) {
		this.optionNumber = optionNumber;
	}

	public int getOptionNumber() {
		return this.optionNumber;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public byte[] getImage() {
		return this.image;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return this.url;
	}

}
