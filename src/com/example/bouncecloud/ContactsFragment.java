package com.example.bouncecloud;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.helpers.Contact;
import com.example.helpers.ContactListAdapter;
import com.example.helpers.DataHolder;
import com.example.interfaces.ContactListListener;

public class ContactsFragment extends Fragment implements
		AdapterView.OnItemClickListener, ContactListListener {

	String TAG = "ContactsActivity";

	TextView textView;
	ListView contactsListView;
	ContactListAdapter contactsAdapter;
	ArrayList<Contact> contacts;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.contacts_activity, container, false);

		Log.d(TAG,
				"contacts.size = "
						+ DataHolder.getDataHolder(
								getActivity().getApplicationContext())
								.getContactsSize());

		contactsListView = (ListView) rootView.findViewById(R.id.contact_list);
		contactsListView.setOnItemClickListener(this);
		contacts = DataHolder.getDataHolder(
				getActivity().getApplicationContext()).getContacts();
		contactsAdapter = new ContactListAdapter(getActivity(), contacts);
		contactsListView.setAdapter(contactsAdapter);

		DataHolder.getDataHolder(getActivity().getApplicationContext())
				.registerContactListListener(this);

//		DataHolder.getDataHolder(getActivity().getApplicationContext())
//				.loadContactsFromPhoneBase();

		return rootView;
	}

	EditText input;

	public void onAddContactClick(View v) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle("Add contact");
		alert.setMessage("Please type the phone number of a friend");

		// Set an EditText view to get user input
		input = new EditText(getActivity());
		alert.setView(input);

		alert.setPositiveButton("Add Friend",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						DataHolder.getDataHolder(getActivity().getApplicationContext())
								.addContactByPhone(input.getText().toString());
					}
				});
 
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContactsChanged() {
		Log.d(TAG, "onContactsChanged called");
		contacts = DataHolder.getDataHolder(getActivity().getApplicationContext())
				.getContacts();
		contactsAdapter.setContacts(contacts);
	}

}
