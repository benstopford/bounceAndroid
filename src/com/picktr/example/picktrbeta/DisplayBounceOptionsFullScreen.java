package com.picktr.example.picktrbeta;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.picktr.example.helpers.Bounce;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.helpers.FullScreenImageAdapter;

public class DisplayBounceOptionsFullScreen extends Activity {

	private static final String TAG = "DisplayBounceOptionsF";

	ViewPager pager;
	FullScreenImageAdapter adapter;
	Bounce bounce;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.bounce_options_fullscreen);

		Bundle extras = getIntent().getExtras();
		Long bounce_id = extras.getLong("bounce_id");

		Log.d(TAG, "bounce id is " + bounce_id);

		Bounce bounce = DataHolder.getDataHolder(getApplicationContext())
				.getBounceWithInternalId(bounce_id);
		int position = extras.getInt("position", 0);

		pager = (ViewPager) findViewById(R.id.pager);
		adapter = new FullScreenImageAdapter(this, bounce);

		pager.setAdapter(adapter);
		pager.setCurrentItem(position);
	}

}
