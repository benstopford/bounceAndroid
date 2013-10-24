package com.example.bouncecloud;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;

import static com.example.definitions.Consts.*; 


public class MainActivity extends Activity implements QBCallback {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//QBSettings.getInstance().fastConfigInit("1028", "jCr7OwnvgV5wFmm", "4JmKPAnwN7ps5Xt");
		QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
		
		QBAuth.createSession(this); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onComplete(Result result) {	
			if (result.isSuccess()) {
				Intent intent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(intent); 
			} else {
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
	            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
	                    "please. Errors: " + result.getErrors()).create().show();
			}
	}

	@Override
	public void onComplete(Result arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
