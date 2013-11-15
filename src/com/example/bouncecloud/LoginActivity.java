package com.example.bouncecloud;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.helpers.DataHolder;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBErrors;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

public class LoginActivity extends Activity implements QBCallback {

	private static final String TAG = "LoginActivity";

	private EditText phoneNumberEdit;
	private QBUser user;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		phoneNumberEdit = (EditText) findViewById(R.id.editPhoneNumber);

		TelephonyManager tMgr = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mPhoneNumber = tMgr.getLine1Number();
		phoneNumberEdit.setText(mPhoneNumber);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Connecting");
	}

	public void onLoginButtonClick(View v) {
		String username = phoneNumberEdit.getText().toString();
		String password = "bounceit";
		user = new QBUser(username, password);
		progressDialog.show();

		QBUsers.signIn(user, LoginActivity.this);
	}

	@Override
	public void onComplete(Result result) {
		// TODO Auto-generated method stub

		if (result.isSuccess()) {
			onUserLoggedIn(result);
		} else {
			// which basically means that user is not registered yet
			if (result.getErrors().get(0).equals(QBErrors.UNAUTHORIZED)) {
				Log.d(TAG, "Unauthorized user");
				progressDialog.setTitle("Creating new account");
				registerNewUser();
			}
		}

	}

	private void registerNewUser() {
		String username = phoneNumberEdit.getText().toString();
		String password = "bounceit";
		user = new QBUser(username, password);
		QBUsers.signUp(user, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					onUserLoggedIn(result);
				}
			}
		});
	}

	private void onUserLoggedIn(Result result) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}

		QBUserResult qbUser = (QBUserResult) result;
		user = qbUser.getUser();

		Intent intent = new Intent(this, BouncesActivity.class);
		intent.putExtra("myId", user.getId());
		intent.putExtra("myUsername", user.getLogin());
		intent.putExtra("myPassword", user.getPassword());

		DataHolder.getDataHolder().userLogin(user);

		startActivity(intent);
		Toast.makeText(this, "You've been successfully logged in application",
				Toast.LENGTH_SHORT).show();
		
		finish();
	}

	@Override
	public void onComplete(Result arg0, Object arg1) {
		// TODO Auto-generated method stub
	}

}
