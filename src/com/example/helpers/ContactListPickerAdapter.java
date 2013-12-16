package com.example.helpers;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.bouncecloud.R; 

public class ContactListPickerAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	String TAG = "ContactListAdapter";
	ArrayList<Contact> contacts;
	ArrayList<String> chosen_ids;

	public ContactListPickerAdapter(Context ctx, ArrayList<Contact> contacts) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.contacts = contacts;
		chosen_ids = new ArrayList<String>();
	}

	public void setContacts(ArrayList<Contact> contacts) {
		this.contacts = contacts;
		notifyDataSetChanged();
	}

	public void clicked(int position) {
		if (chosen_ids.contains(contacts.get(position).getUserID().toString())) {
			chosen_ids.remove(contacts.get(position).getUserID().toString());
		} else {
			chosen_ids.add(contacts.get(position).getUserID().toString());
		}
		notifyDataSetChanged();
	}
	
	public ArrayList<String> getClicked()
	{
		return chosen_ids; 
	}

	@Override
	public int getCount() {
		Log.d(TAG, "getCount called and returned" + contacts.size());
		return contacts.size();
	}

	@Override
	public Object getItem(int position) {
		Log.d(TAG, "getItem called");
		return null;
	}

	@Override
	public long getItemId(int position) {
		Log.d(TAG, "getItemId called");
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "getView called");
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(
					R.layout.contact_clickable_view, null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.contact_name);
			viewHolder.clicked = (CheckBox) convertView
					.findViewById(R.id.contact_clicked);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		applyName(viewHolder.name, position);
		applyCheckBox(viewHolder.clicked, position);

		return convertView;
	}

	private void applyName(TextView name, int position) {
		Log.d(TAG, "setting name to " + contacts.get(position).getName());
		name.setText(contacts.get(position).getName());
	}

	private void applyCheckBox(CheckBox checkBox, int position) {
		checkBox.setChecked(chosen_ids.contains(contacts.get(position)
				.getUserID().toString()));
	}

	static class ViewHolder {
		TextView name;
		CheckBox clicked;
	}

}
