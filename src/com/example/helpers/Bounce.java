package com.example.helpers;

import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

import com.example.definitions.Consts;

public class Bounce {

	private static String TAG = "Bounce Class";

	private long ID;
	private Integer sender_id;
	private Integer numberOfOptions;
	private String question;
	private ArrayList<String> optionNames;
	private ArrayList<Integer> type;
	private ArrayList<String> content;
	private ArrayList<Integer> receivers;
	private String bounce_id;
	private Integer isFromSelf; // 0 - false 1 - true
	private Date sendAt;
	private String status; // status Consts in Consts file
	private Integer isSeen; // 0 - false 1 - true

	public Bounce() {

	}

	public Bounce(int sender_id, int numberOfOptions, ArrayList<Integer> type,
			ArrayList<String> content, ArrayList<Integer> receivers,
			String bounce_id, int isFromSelf, String question,
			ArrayList<String> optionNames, Date sendAt, String status,
			int isSeen) {
		this.sender_id = sender_id;
		this.numberOfOptions = numberOfOptions;
		this.type = type;
		this.content = content;
		this.receivers = receivers;
		this.bounce_id = bounce_id;
		this.isFromSelf = isFromSelf;
		this.question = question;
		this.optionNames = optionNames;
		this.sendAt = sendAt;
		this.status = status;
		this.isSeen = isSeen;
	}

	public Bounce(int sender_id, int numberOfOptions, ArrayList<Integer> type,
			ArrayList<String> content, ArrayList<Integer> receivers,
			String bounce_id, long ID, int isFromSelf, String question,
			ArrayList<String> optionNames, Date sendAt, String status,
			int isSeen) {
		this.sender_id = sender_id;
		this.numberOfOptions = numberOfOptions;
		this.type = type;
		this.content = content;
		this.receivers = receivers;
		this.bounce_id = bounce_id;
		this.ID = ID;
		this.isFromSelf = isFromSelf;
		this.question = question;
		this.optionNames = optionNames;
		this.sendAt = sendAt;
		this.status = status;
		this.isSeen = isSeen;
	}

	public Date getSendAt() {
		return sendAt;
	}

	public void setSendAt(Date date) {
		sendAt = date;
	}

	public long getID() {
		return ID;
	}

	public void setID(long id) {
		this.ID = id;
	}

	public Integer getSender() {
		return sender_id;
	}

	public void setSender(int sender_id) {
		this.sender_id = sender_id;
	}

	public Integer getNumberOfOptions() {
		return numberOfOptions;
	}

	public void setNumberOfOptions(int numberOfOptions) {
		this.numberOfOptions = numberOfOptions;
	}

	public ArrayList<Integer> getTypes() {
		return type;
	}

	public void setTypes(ArrayList<Integer> types) {
		this.type = types;
	}

	public ArrayList<Integer> getReceivers() {
		return receivers;
	}

	public void setReceivers(ArrayList<Integer> receivers) {
		this.receivers = receivers;
	}

	public ArrayList<String> getContents() {
		return content;
	}

	public void setContents(ArrayList<String> newContents) {
		content = newContents;
	}

	public String getContentAt(int indexOfOption) {
		if (indexOfOption >= content.size())
			return null;

		return content.get(indexOfOption);
	}

	public String getReceiversAsString() {
		String res = "";

		Integer tt = receivers.get(0);
		Log.d(TAG, tt.toString());
		for (int i = 0; i < receivers.size(); i++) {
			String t = receivers.get(i).toString();
			res = res + "id" + t;
		}

		Log.d(TAG, "getReceiversAsString = " + res);

		return res;
	}

	@Override
	public String toString() {

		return super.toString() + "Sender_id " + sender_id + "Content "
				+ content + " receivers " + receivers + " number of options"
				+ numberOfOptions + " types " + type;
	}

	public String getBounceId() {
		return bounce_id;
	}

	public void setBounceId(String bounce_id) {
		this.bounce_id = bounce_id;
	}

	public boolean isFromSelf() {
		return (isFromSelf == 1);
	}

	public Integer getIsFromSelf() {
		return isFromSelf;
	}

	public void setIsFromSelf(int isFromSelf) {
		this.isFromSelf = isFromSelf;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String q) {
		question = q;
	}

	public ArrayList<String> getOptionNames() {
		return optionNames;
	}

	public void setOptionNames(ArrayList<String> optionNames) {
		this.optionNames = optionNames;
	}

	public boolean isDraft() {
		return (this.status.equals(Consts.BOUNCE_STATUS_DRAFT));
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getIsSeen() {
		return this.isSeen;
	}

	public void setIsSeen(int isSeen) {
		this.isSeen = isSeen;
	}

	public boolean isSeen() {
		return (isSeen == 1);
	}
}
