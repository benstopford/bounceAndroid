package com.picktr.example.picktrbeta;

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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.picktr.example.definitions.Consts;
import com.picktr.example.helpers.Bounce;
import com.picktr.example.helpers.BouncesListAdapter;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.helpers.Utils;
import com.picktr.example.interfaces.BouncesListListener;
import com.picktr.example.interfaces.ContactListListener;
import com.picktr.example.interfaces.LikeListener;
import com.picktr.example.services.NetworkService;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.users.model.QBUser;

public class BouncesFragment extends Fragment implements BouncesListListener,
		OnItemClickListener, ContactListListener, LikeListener {

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
	NetworkService networkService;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getActivity());
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode,
						getActivity(), PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
			}
			return false;
		}
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "onCreateView called");

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.bounces_fragment, container, false);

		checkPlayServices();

		dataHolder = DataHolder.getDataHolder(getActivity()
				.getApplicationContext());
		networkService = ((PicktrApplication) getActivity().getApplication()).networkService;
		bounceItButton = (Button) rootView.findViewById(R.id.bounceit_button);
		bounceItButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBounceitClick(v);
			}
		});

		bouncesListView = (ListView) rootView.findViewById(R.id.bounces_list);

		LinearLayout viewFooter = new LinearLayout(getActivity());
		viewFooter.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams lp = new LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				bounceItButton.getLayoutParams().height);
		viewFooter.setLayoutParams(lp);

		bouncesListView.addFooterView(viewFooter, null, false);

		LinearLayout viewHeader = new LinearLayout(getActivity());
		viewHeader.setOrientation(LinearLayout.HORIZONTAL);
		int height = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 5, getResources()
						.getDisplayMetrics());
		LayoutParams lp2 = new LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, height);
		viewHeader.setLayoutParams(lp2);
		bouncesListView.addHeaderView(viewHeader);

		bounces = dataHolder.getBounces();

		for (int i = 0; i < bounces.size(); i++) {
			Log.d(TAG, " bounce statues: " + bounces.get(i).getStatus());
		}

		bouncesListAdapter = new BouncesListAdapter(getActivity(), bounces,
				networkService);
		bouncesListView.setAdapter(bouncesListAdapter);
		bouncesListView.setOnItemClickListener(this);

		dataHolder.registerBouncesListListener(this);
		dataHolder.registerLikeListener(this);
		initialize(dataHolder.getSelfUser());
		QBUser user = dataHolder.getSelfUser();
		Log.d(TAG, "user is " + user.getLogin() + " " + user.getPassword());

		return rootView;
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView called");
		dataHolder.deregisterBouncesListListener(this);
		dataHolder.deregisterLikeListener(this);
		super.onDestroyView();
	}

	private void onBounceitClick(View v) {
		Bounce bounce = new Bounce();
		bounce.setSender(dataHolder.getSelf().getUserID());
		bounce.setSendAt(new Date(System.currentTimeMillis()));
		bounce.setStatus(Consts.BOUNCE_STATUS_DRAFT);
		bounce.setIsFromSelf(Consts.FROM_SELF);
		bounce.setNumberOfOptions(0);
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
		Log.d(TAG, "creating register");
		registerBackground();
		// }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, "OnItemClick called for position " + position);
		Bounce bounce = bounces.get(position - 1);
		Utils.startBounceActivity(getActivity(), bounce, 0);
	}

	@Override
	public void onContactsChanged() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				bouncesListAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onLikesChanged() {
		Log.d(TAG, "onLikesChanged called");
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.d(TAG, "onLikesChanged implemented called");
				bouncesListAdapter.notifyDataSetChanged();
			}
		});
	}
}
