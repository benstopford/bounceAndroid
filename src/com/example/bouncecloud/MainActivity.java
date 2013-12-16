package com.example.bouncecloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.helpers.DataHolder;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (DataHolder.getDataHolder(getApplicationContext()).getSelf() == null) {
			Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(MainActivity.this,
					BounceCloudActivity.class);
			startActivity(intent);
		}
		
		finish();
	}
}
