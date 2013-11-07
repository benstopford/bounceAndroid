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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.helpers.Bounce;
import com.example.helpers.BouncesListAdapter;
import com.example.helpers.DataHolder;
import com.example.interfaces.BouncesListListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.users.model.QBUser;

public class BouncesActivity extends Activity implements BouncesListListener,
		OnItemClickListener {

	private static final String TAG = "BounceActivity";

	QBUser user;
	String regId;
	String devId;
	GoogleCloudMessaging gcm;
	ListView bouncesListView;
	BouncesListAdapter bouncesListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bounces_activity);

		bouncesListView = (ListView) findViewById(R.id.bounces_list);
		bouncesListAdapter = new BouncesListAdapter(this);
		bouncesListView.setAdapter(bouncesListAdapter);
		bouncesListView.setOnItemClickListener(this);

		user = DataHolder.getDataHolder().getSignInUserId();
		DataHolder.getDataHolder().registerBouncesListListener(this);

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

	public void subscribeToPushNotifications(String registrationID) {
		String deviceId = ((TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

		regId = registrationID;
		devId = deviceId;

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				QBMessages.subscribeToPushNotificationsTask(regId, devId,
						QBEnvironment.DEVELOPMENT, new QBCallbackImpl() {
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
					Log.d("gcm:", "Device registered, registration ID="
							+ gcmRegId);
					subscribeToPushNotifications(gcmRegId); 

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}

		}.execute(null, null, null);
	}

	public void onBounceitClick(View v) {
		Intent intent = new Intent(this, BounceitActivity.class);
		startActivity(intent);
	}

	public void onContactsClick(View v) {
		Intent intent = new Intent(this, ContactsActivity.class);
		intent.putExtra("myId", user.getId());
		intent.putExtra("myUsername", user.getLogin());
		intent.putExtra("myPassword", user.getPassword());

		startActivity(intent);

	}

	@Override
	public void onBouncesChanged() {
		Log.d(TAG, "onBouncesChanged called");
		bouncesListAdapter.notifyDataSetChanged();
	}

	public void onLikeButtonClick(View v) {
		Log.d(TAG, "on Like Button Click");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, "OnItemClick called");
		Bounce bounce = DataHolder.getDataHolder().getBounceAtIndex(position);

		if (bounce.isFromSelf()) {
			Intent intent = new Intent(this, DisplayBounceFromSelf.class);
			intent.putExtra("bounce_id", bounce.getBounceId());
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, DisplayBounceToSelf.class);
			intent.putExtra("bounce_id", bounce.getBounceId());
			startActivity(intent);
		}

	}

}
