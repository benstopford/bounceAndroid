package com.picktr.example.helpers;

import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

import com.picktr.example.definitions.Consts;

public class Bounce {

	private static final String TAG = "Bounce Class";

	private long ID;
	private String QBID;
	private Integer senderID;
	private String question;
	private Integer numberOfOptions;
	private ArrayList<BounceOption> options;
	private String status; // status Consts in Consts file
	private Integer isSeen; // 0 - false 1 - true
	private Integer isFromSelf; // 0 - false 1 - true
	private ArrayList<Integer> receivers;
	private Date sendAt;

	public void deleteOption(int optionNumber) {
		Log.d(TAG, "removing option number " + optionNumber);
		if (optionNumber >= numberOfOptions || optionNumber < 0) {
			Log.e(TAG, "on Delete Option called outside the interval");
			return;
		}
		options.remove(optionNumber);
		numberOfOptions -= 1;
	}

	public Bounce() {

	}

	public Bounce(long ID, String QBID, Integer senderID, String question,
			Integer numberOfOptions, ArrayList<BounceOption> options,
			String status, Integer isSeen, Integer isFromSelf,
			ArrayList<Integer> receivers, Date sendAt) {
		this.ID = ID;
		this.QBID = QBID;
		this.senderID = senderID;
		this.question = question;
		this.numberOfOptions = numberOfOptions;
		this.options = options;
		this.status = status;
		this.isSeen = isSeen;
		this.isFromSelf = isFromSelf;
		this.receivers = receivers;
		this.sendAt = sendAt;
	}

	public Bounce(String QBID, Integer senderID, String question,
			Integer numberOfOptions, ArrayList<BounceOption> options,
			String status, Integer isSeen, Integer isFromSelf,
			ArrayList<Integer> receivers, Date sendAt) {
		this.QBID = QBID;
		this.senderID = senderID;
		this.question = question;
		this.numberOfOptions = numberOfOptions;
		this.options = options;
		this.status = status;
		this.isSeen = isSeen;
		this.isFromSelf = isFromSelf;
		this.receivers = receivers;
		this.sendAt = sendAt;
	}

	public long getID() {
		return ID;
	}

	public void setID(long id) {
		this.ID = id;
	}

	public String getQBID() {
		return QBID;
	}

	public void setQBID(String qbID) {
		this.QBID = qbID;
	}

	public Integer getSender() {
		return senderID;
	}

	public void setSender(int sender_id) {
		this.senderID = sender_id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String q) {
		question = q;
	}

	public Integer getNumberOfOptions() {
		return numberOfOptions;
	}

	public void setNumberOfOptions(int numberOfOptions) {
		this.numberOfOptions = numberOfOptions;
	}

	public ArrayList<BounceOption> getOptions() {
		if (options == null)
			options = new ArrayList<BounceOption>();
		return this.options;
	}

	public void setOptions(ArrayList<BounceOption> options) {
		this.options = options;
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

	public boolean isFromSelf() {
		return (isFromSelf == 1);
	}

	public Integer getIsFromSelf() {
		return isFromSelf;
	}

	public void setIsFromSelf(int isFromSelf) {
		this.isFromSelf = isFromSelf;
	}

	public ArrayList<Integer> getReceivers() {
		return receivers;
	}

	public void setReceivers(ArrayList<Integer> receivers) {
		this.receivers = receivers;
	}

	public Date getSendAt() {
		return sendAt;
	}

	public void setSendAt(Date date) {
		sendAt = date;
	}

	public void addOption(BounceOption option) {
		if (options == null)
			options = new ArrayList<BounceOption>();
		options.add(option);
	}

	public void deleteAllOptions() {
		options = new ArrayList<BounceOption>();
	}

}
