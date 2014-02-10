package com.picktr.example.picktrbeta;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.picktr.example.definitions.Consts;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.services.NetworkService;
import com.quickblox.core.QBSettings;

@ReportsCrashes(formKey = "", // This is required for backward compatibility but
// not used
mailTo = "bouncecloudteam@gmail.com")
public class PicktrApplication extends Application {

	public DataHolder dataHolder;
	public static final String TAG = "BounceCloudApplication";

	public NetworkService networkService = null;
	private Boolean mIsBound = false;

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "Disconnected");
			networkService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "Connected to the service.");
			networkService = ((NetworkService.NetworkBinder) service)
					.getService();
		}
	};

	void doBindService() {
		bindService(new Intent(this, NetworkService.class), mConnection,
				BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreateForApplication called");
		QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY,
				Consts.AUTH_SECRET);
		ACRA.init(this);
		doBindService();
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		doUnbindService();
		super.onTerminate();
	}
}
