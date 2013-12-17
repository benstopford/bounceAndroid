package com.example.bouncecloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.example.definitions.Consts;
import com.example.helpers.Bounce;
import com.example.helpers.BouncesListAdapter;
import com.example.helpers.DataHolder;
import com.example.interfaces.BouncesListListener;
import com.example.interfaces.NewsArrivedListener;
import com.example.interfaces.SessionCreatedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.users.model.QBUser;

public class BouncesFragment extends Fragment implements BouncesListListener,
		OnItemClickListener, SessionCreatedListener, NewsArrivedListener {

	private static final String TAG = "BounceActivity";

	ListView bouncesListView;
	BouncesListAdapter bouncesListAdapter;
	ArrayList<Bounce> bounces;
	GoogleCloudMessaging gcm;
	Button bounceItButton;
	String regId;
	String devId;
	String purpose;
	DataHolder dataHolder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.bounces_fragment, container, false);

		dataHolder = DataHolder.getDataHolder(getActivity()
				.getApplicationContext());
		bounceItButton = (Button) rootView.findViewById(R.id.bounceit_button);
		bounceItButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBounceitClick(v);
			}
		});

		bouncesListView = (ListView) rootView.findViewById(R.id.bounces_list);
		bounces = dataHolder.getBounces();

		for (int i = 0; i < bounces.size(); i++) {
			Log.d(TAG, " bounce statues: " + bounces.get(i).getStatus());
		}

		bouncesListAdapter = new BouncesListAdapter(getActivity(), bounces);
		bouncesListView.setAdapter(bouncesListAdapter);
		bouncesListView.setOnItemClickListener(this);
		dataHolder.registerBouncesListListener(this);
		dataHolder.registerSessionCreatedListener(this);
		dataHolder.registerNewsListener(this);
		dataHolder.updateNews();
		initialize(dataHolder.getSelfUser());
		QBUser user = dataHolder.getSelfUser();
		Log.d(TAG, "user is " + user.getLogin() + " " + user.getPassword());

		return rootView;
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		dataHolder.deregisterBouncesListListener(this);
		dataHolder.deregisterSessionCreatedListener(this);
		dataHolder.deregisterNewsListener(this);
		super.onDestroyView();
	}

	public void onBounceitClick(View v) {
		Bounce bounce = new Bounce();
		bounce.setSender(dataHolder.getSelf().getUserID());
		bounce.setSendAt(new Date(System.currentTimeMillis()));
		bounce.setStatus(Consts.BOUNCE_STATUS_DRAFT);
		bounce.setIsFromSelf(Consts.FROM_SELF);
		bounce.setID(dataHolder.addDraftBounce(bounce));
		Intent intent = new Intent(getActivity(), BounceitActivity.class);
		intent.putExtra("id", bounce.getID());
		startActivity(intent);
	}

	@Override
	public void onBouncesChanged() {

		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.d(TAG, "onBouncesChanged called");
				bounces = dataHolder.getBounces();

				for (int i = 0; i < bounces.size(); i++) {
					Log.d(TAG, " bounce contents: "
							+ bounces.get(i).getContents());
				}

				bouncesListAdapter.setBounces(bounces);
			}
		});
	}

	public void onLikeButtonClick(View v) {
		Log.d(TAG, "on Like Button Click");
	}

	public void subscribeToPushNotifications(String registrationID) {
		String deviceId = ((TelephonyManager) getActivity().getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

		regId = registrationID;
		devId = deviceId;

		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				QBMessages.subscribeToPushNotificationsTask(regId, devId,
						QBEnvironment.DEVELOPMENT, new QBCallbackImpl() {
							@Override
							public void onComplete(Result result) {
								if (result.isSuccess()) {
									dataHolder.setRegistered(true);
									Log.d("SUBSCRIBED", "subscribed");
								} else {
									dataHolder.setRegistered(false);
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
				gcm = GoogleCloudMessaging.getInstance(getActivity()
						.getApplicationContext());

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

	private void initialize(QBUser user) {
		// if (dataHolder.isRegistered() == false) {
		registerBackground();
		// }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, "OnItemClick called");
		Bounce bounce = bounces.get(position);

		if (bounce.getStatus().equals(Consts.BOUNCE_STATUS_LOADING)
				|| bounce.getStatus().equals(Consts.BOUNCE_STATUS_SENDING)) {
			return;
		}
		
		if (bounce.isDraft()) {
			Log.d(TAG, "putting extra to bounceIt " + bounce.getID());
			Intent intent = new Intent(getActivity(), BounceitActivity.class);
			intent.putExtra("id", bounce.getID());
			startActivity(intent);
		} else if (bounce.isFromSelf()) {
			Intent intent = new Intent(getActivity(),
					DisplayBounceFromSelf.class);
			intent.putExtra("bounce_id", bounce.getBounceId());
			intent.putExtra("option", 0);
			startActivity(intent);
		} else {
			Intent intent = new Intent(getActivity(), DisplayBounceToSelf.class);
			intent.putExtra("bounce_id", bounce.getBounceId());
			intent.putExtra("option", 0);
			startActivity(intent);
		}

	}

	@Override
	public void onSessionWithUserCreated() {
		initialize(DataHolder.getDataHolder(getActivity()).getSelfUser());
		dataHolder.updateNews();
	}

	@Override
	public void onNewNews() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dataHolder.updateNews();
			}
		});
	}

}
