package com.picktr.example.services;

import static com.picktr.example.definitions.Consts.MESSAGE_TYPE_BOUNCE;
import static com.picktr.example.definitions.Consts.MESSAGE_TYPE_LIKE;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.picktr.example.definitions.Consts;
import com.picktr.example.picktrbeta.BounceCloudActivity;
import com.picktr.example.picktrbeta.R;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private static String TAG = "GcmIntentService";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		Log.i(TAG, "something received: " + extras.toString());

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				// Post notification of received message.
				sendNotification(extras.getString("message"));
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notificatison and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		JSONObject jsonObject = null;
		Log.d(TAG, msg);
		try {
			jsonObject = new JSONObject(msg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, jsonObject.toString());
		String type = null;
		try {
			type = jsonObject.getString("type");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Intent intent = null;
		String bounce_id = null;
		String sender_login = null;
		String message = null;
		Integer option = 0;

		intent = new Intent(this, BounceCloudActivity.class);

		if (type.equals(MESSAGE_TYPE_BOUNCE)) {
			try {
				message = jsonObject.getString("message");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (type.equals(MESSAGE_TYPE_LIKE)) {
			try {
				message = jsonObject.getString("message");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (type.equals(Consts.MESSAGE_TYPE_SEEN)) {
			try {
				message = jsonObject.getString("message");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.picktr_icon)
				.setContentTitle("New activities:")
				.setStyle(
						new NotificationCompat.BigTextStyle().bigText(message))
				.setContentText(message).setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}