package com.example.bouncecloud;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.users.model.QBUser;

public class BouncesActivity extends Activity {
	
	
	TextView textView; 
	QBUser user; 
	GoogleCloudMessaging gcm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bounces_activity);
		Bundle extras = getIntent().getExtras();
		
		textView = (TextView) findViewById(R.id.some_text); 
		
		user = new QBUser();
	    user.setId(extras.getInt("myId"));
	    user.setLogin(extras.getString("myUsername"));
	    user.setPassword(extras.getString("myPassword")); 
	    
	    textView.setText(user.toString());
	    
	    QBAuth.createSession(user, new QBCallback() {
			
			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onComplete(Result result) {
				// TODO Auto-generated method stub
				
	            if (result.isSuccess()) {
	            	registerBackground(); 
	            }
				
			}
		}); 

		
	}
	
	String regId; 
	String devId; 
	
	public void subscribeToPushNotifications(String registrationID) {
	    String deviceId = ((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	    
	    regId = registrationID; 
	    devId = deviceId; 
	    
	    runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				QBMessages.subscribeToPushNotificationsTask(regId, devId, QBEnvironment.DEVELOPMENT, new QBCallbackImpl() {
			        @Override
			        public void onComplete(Result result) {
			            if (result.isSuccess()) {
			                Log.d("SUBSCRIBED", "subscribed");
			            } else {
			            	Log.e("UNSUBSCRIBED", "BAD BAD BAD"); 
			            }
			        }
			    });
			}
		}); 
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerBackground() {
	    new AsyncTask() {

			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				gcm = GoogleCloudMessaging.getInstance(getApplicationContext()); 
				
				try {
					String gcmRegId = gcm.register("547257665434");
					Log.d("gcm:", "Device registered, registration ID=" + gcmRegId);
					subscribeToPushNotifications(gcmRegId); 
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null;
			}
	    	
	    }.execute(null,null,null); 
	}
	
	
	    	
	public void onContactsClick(View v)
	{
		Intent intent = new Intent(this, ContactsActivity.class);
        intent.putExtra("myId", user.getId());
        intent.putExtra("myUsername", user.getLogin());
        intent.putExtra("myPassword", user.getPassword());
        
        startActivity(intent);

	}
	
	
}
