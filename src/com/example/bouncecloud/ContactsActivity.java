package com.example.bouncecloud;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

public class ContactsActivity extends Activity implements QBCallback {
	
	
	TextView textView; 
	QBUser user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_activity);
		Bundle extras = getIntent().getExtras();
		
		user = new QBUser();
	    user.setId(extras.getInt("myId"));
	    user.setLogin(extras.getString("myUsername"));
	    user.setPassword(extras.getString("myPassword"));
	    	    
		
		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder(); 
		requestBuilder.eq("owner", user.getId()); 
		
		QBCustomObjects.getObjects("Contacts", requestBuilder, new QBCallback() {
			
			@Override
			public void onComplete(Result result, Object arg1) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onComplete(Result result) {
				// TODO Auto-generated method stub
				if (result.isSuccess()) {
		             QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
		             ArrayList<QBCustomObject> co = coresult.getCustomObjects();
		             Log.d("Records: ", co.toString());
		             for (int i=0; i < co.size(); i++) {
		                  	 
		             }
		         } else {
		             Log.e("Errors",result.getErrors().toString());
		         }
			}
			
		});
		
		
		
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
					Log.d("User", qbUserResult.getUser().toString());
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
	public void onComplete(Result arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onComplete(Result arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	
}
