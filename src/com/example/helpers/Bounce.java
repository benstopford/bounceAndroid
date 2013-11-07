package com.example.helpers;

import java.util.ArrayList;

import android.util.Log;

public class Bounce {

	private static String TAG = "Bounce Class";

	private Integer sender_id;
	private Integer numberOfOptions;
	private ArrayList<Integer> type;
	private ArrayList<String> content;
	private ArrayList<Integer> receivers;
	private String bounce_id;

	public Bounce(int sender_id, int numberOfOptions, ArrayList<Integer> type,
			ArrayList<String> content, ArrayList<Integer> receivers,
			String bounce_id) {
		this.sender_id = sender_id;
		this.numberOfOptions = numberOfOptions;
		this.type = type;
		this.content = content;
		this.receivers = receivers;
		this.bounce_id = bounce_id;
	}

	public String getContentAt(int indexOfOption) {
		return content.get(indexOfOption);
	}

	public Integer getSender() {
		return sender_id;
	}

	public int getNumberOfOptions() {
		return numberOfOptions;
	}

	public ArrayList<Integer> getTypes() {
		return type;
	}

	public ArrayList<Integer> getReceivers() {
		return receivers;
	}

	public ArrayList<String> getContents() {
		return content;
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

	public boolean isFromSelf() {
		return (sender_id.equals(DataHolder.getDataHolder().getSignInUserId()
				.getId()));
	}

}
