package com.example.bouncecloud;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.helpers.Contact;
import com.example.helpers.ContactListAdapter;
import com.example.helpers.ContactListPickerAdapter;
import com.example.helpers.DataHolder;
import com.example.interfaces.ContactListListener;

public class ContactPicker extends Activity implements OnItemClickListener,
		ContactListListener {

	String TAG = "ContactPicker";
	ListView contactsListView;
	ContactListPickerAdapter contactsAdapter;
	ArrayList<Contact> contacts;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_picker); 

		contactsListView = (ListView) findViewById(R.id.contact_picker_list);
		contactsListView.setOnItemClickListener(this);
		contacts = DataHolder.getDataHolder(getApplicationContext())
				.getContacts();
		contactsAdapter = new ContactListPickerAdapter(this, contacts);
		contactsListView.setAdapter(contactsAdapter);
		DataHolder.getDataHolder(getApplicationContext())
				.registerContactListListener(this);

	}

	public void onSendButtonClick(View v) {
		Log.d(TAG, "onSendButton called");
		ArrayList<String> clicked = contactsAdapter.getClicked();
		if (clicked.size() == 0)
			return;
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("chosen_ids", clicked);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	@Override
	public void onContactsChanged() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onContactsChanged called");
		contacts = DataHolder.getDataHolder(getApplicationContext())
				.getContacts();
		contactsAdapter.setContacts(contacts);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onItemClick called");
		contactsAdapter.clicked(position); 	
	}
}
