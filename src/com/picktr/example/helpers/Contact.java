package com.picktr.example.helpers;

import java.util.Date;

public class Contact {

	// private variables
	long id;
	int userID;
	String login;
	String name;
	String phoneNumber;
	String password;
	Integer blobID;
	byte[] profileImage;
	Date updatedAt;

	// Empty constructor
	public Contact() {

	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof Contact))
			return false;

		Contact otherObject = (Contact) other;
		return otherObject.userID == this.userID;

	}

	// constructor
	public Contact(int id, int userID, String login, String name,
			String phoneNumber, int blobID, byte[] profileImage, Date updatedAt) {
		this.id = id;
		this.userID = userID;
		this.login = login;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.blobID = blobID;
		this.profileImage = profileImage;
		this.updatedAt = updatedAt;
	}

	// constructor without ID, be careful! use it only for adding to database
	// and then get contact from DB
	public Contact(int userID, String login, String name, String phoneNumber,
			Integer blobID, byte[] profileImage, Date updatedAt) {
		this.userID = userID;
		this.login = login;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.blobID = blobID;
		this.profileImage = profileImage;
		this.updatedAt = updatedAt;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	// getting ID
	public long getID() {
		return this.id;
	}

	// setting id
	public void setID(long id) {
		this.id = id;
	}

	// getting name
	public Integer getUserID() {
		return this.userID;
	}

	// setting name
	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getDisplayName() {
		if (this.name != null)
			return this.name;
		if (this.phoneNumber != null)
			return "+" + this.phoneNumber;

		return "+" + this.login;
	}

	// getting name
	public String getName() {
		return this.name;
	}

	// setting name
	public void setName(String name) {
		this.name = name;
	}

	// getting phone number
	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	// getting Display phone Number with + sign
	public String getDisplayPhoneNumber() {
		return "+" + this.phoneNumber;
	}

	// setting phone number
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Integer getBlobID() {
		return blobID;
	}

	public void setBlobID(int blobID) {
		this.blobID = blobID;
	}

	public byte[] getProfileImage() {
		return this.profileImage;
	}

	public void setProfileImage(byte[] profileImage) {
		this.profileImage = profileImage;
	}

	public Date getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

}
