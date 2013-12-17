package com.example.bouncecloud;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
	Button addContactButton;
	Button inviteFriendsButton;

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

		addContactButton = (Button) rootView
				.findViewById(R.id.add_contact_button);
		addContactButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onAddContactClick(v);
			}
		});

		inviteFriendsButton = (Button) rootView
				.findViewById(R.id.invite_friends_button);

		inviteFriendsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onInviteFriendsCLick(v);
			}
		});

		contactsListView = (ListView) rootView.findViewById(R.id.contact_list);
		contactsListView.setOnItemClickListener(this);
		contacts = DataHolder.getDataHolder(
				getActivity().getApplicationContext()).getContacts();
		contactsAdapter = new ContactListAdapter(getActivity(), contacts);
		contactsListView.setAdapter(contactsAdapter);

		DataHolder.getDataHolder(getActivity().getApplicationContext())
				.registerContactListListener(this);

		// DataHolder.getDataHolder(getActivity().getApplicationContext())
		// .loadContactsFromPhoneBase();

		return rootView;
	}

	protected void onInviteFriendsCLick(View v) {
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Try BounceCloud for Android!");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				"Hello! I have a bounce for you! Try BounceCloud for free!");
		
		Intent chooserIntent = Intent.createChooser(shareIntent, "Invite with");
		chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(chooserIntent);
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
						DataHolder.getDataHolder(
								getActivity().getApplicationContext())
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
		contacts = DataHolder.getDataHolder(
				getActivity().getApplicationContext()).getContacts();
		contactsAdapter.setContacts(contacts);
	}

}
