package com.example.bouncecloud;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.example.definitions.Consts;
import com.example.helpers.DataHolder;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.model.QBUser;

public class BounceCloudApplication extends Application {

	public DataHolder dataHolder;
	public static final String TAG = "BounceCloudApplication";
	private Runnable createSessionRunnable;
	private Handler handler;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreateForApplication called");
		dataHolder = DataHolder.getDataHolder(getApplicationContext());

		QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY,
				Consts.AUTH_SECRET);
		handler = new Handler();

		createSessionRunnable = new Runnable() {
			@Override
			public void run() {
				dataHolder.createSession();
				handler.postDelayed(createSessionRunnable, 3600000);
			}
		};

		QBUser user = dataHolder.getSelfUser();
		if (user != null) {
			QBAuth.createSession(user, new QBCallback() {

				@Override
				public void onComplete(Result arg0, Object arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onComplete(Result result) {
					// TODO Auto-generated method stub

					if (result.isSuccess()) {
						Log.d(TAG, "successfully created session");
					} else {
						Log.e(TAG, "FAILED TO CREATE SESSION");
					}
					callSuperOnCreate();
				}
			});
		} else {
			callSuperOnCreate();
		}

		createSessionRunnable.run();

	}

	public void callSuperOnCreate() {
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		handler.removeCallbacks(createSessionRunnable);
		super.onTerminate();
	}
}
