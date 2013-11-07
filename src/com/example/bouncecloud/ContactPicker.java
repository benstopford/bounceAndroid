package com.example.bouncecloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.helpers.ContactListAdapter;
import com.example.helpers.DataHolder;
import com.example.interfaces.ContactListListener;
import com.quickblox.module.users.model.QBUser;

public class ContactPicker extends Activity implements OnItemClickListener, ContactListListener {

	String TAG = "ContactPicker";
	ListView contactsListView;
	ContactListAdapter contactsAdapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_picker); 
	    
	    contactsListView = (ListView) findViewById(R.id.contact_picker_list);
	    contactsListView.setOnItemClickListener(this);
	    contactsAdapter = new ContactListAdapter(this);
	    contactsListView.setAdapter(contactsAdapter);
	    DataHolder.getDataHolder().registerContactListListener(this); 
	    
	}

	@Override
	public void onContactsChanged() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onContactsChanged called");
		contactsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		QBUser user = DataHolder.getDataHolder().getContactAtIndex(position);
		Intent returnIntent = new Intent();
		Log.d(TAG, user.getId().toString());
		returnIntent.putExtra("chosen_id",user.getId());
		setResult(RESULT_OK,returnIntent);     
		finish();
	}
}
