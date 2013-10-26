package com.example.bouncecloud;

import java.util.concurrent.RunnableScheduledFuture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.helpers.ContactListAdapter;
import com.example.helpers.DataHolder;
import com.example.interfaces.ContactListListener;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

public class ContactsActivity extends Activity implements AdapterView.OnItemClickListener, ContactListListener {
	
	String TAG = "ContactsActivity"; 
	
	TextView textView; 
	QBUser user;
	ListView contactsListView;
	ContactListAdapter contactsAdapter; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_activity);
		Bundle extras = getIntent().getExtras();
		
		Log.d(TAG, "contacts.size = " + DataHolder.getDataHolder().getContactsSize());
		
		user = new QBUser();
	    user.setId(extras.getInt("myId"));
	    user.setLogin(extras.getString("myUsername"));
	    user.setPassword(extras.getString("myPassword"));
	    
	    contactsListView = (ListView) findViewById(R.id.contact_list);
	    
	    contactsListView.setOnItemClickListener(this);
	    contactsAdapter = new ContactListAdapter(this);
	    contactsListView.setAdapter(contactsAdapter);
	    DataHolder.getDataHolder().registerContactListListener(this); 
	}
	
	public void onAddContactClick(View v)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Add contact");
		alert.setMessage("Please type the username of a friend");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
			
		public void onClick(DialogInterface dialog, int whichButton) {
		  Editable value = input.getText();
		  
		  QBUsers.getUserByLogin(value.toString(), new QBCallback() {
			
			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					QBUserResult qbUserResult = (QBUserResult) result;
					DataHolder.getDataHolder().addContact(qbUserResult.getUser()); 
				} else {
					Log.d("Error", result.toString());
				}
			}
			
		}); 
		  
		  // Do something with value!
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
		contactsAdapter.notifyDataSetChanged();
	}
	
}
