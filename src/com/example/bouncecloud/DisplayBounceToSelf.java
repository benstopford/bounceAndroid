package com.example.bouncecloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;

import com.example.helpers.Bounce;
import com.example.helpers.BounceOptionsHListAdapter;
import com.example.helpers.DataHolder;
import com.example.interfaces.BounceListener;

public class DisplayBounceToSelf extends Activity implements BounceListener,
		OnItemClickListener {

	public static final String TAG = "DisplayBounceFromSelf";

	Gallery galleryListview;
	BounceOptionsHListAdapter galleryAdapter;
	Bounce bounce;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_bounce_to_self);

		Bundle extras = getIntent().getExtras();
		String bounce_id = extras.getString("bounce_id");
		bounce = DataHolder.getDataHolder(getApplicationContext())
				.getBounceWithId(bounce_id);
		DataHolder.getDataHolder(getApplicationContext())
				.registerBounceListener(this, bounce_id);

		galleryListview = (Gallery) findViewById(R.id.options_horizontal_listview);

		if (bounce != null)
			addBounceView(bounce);
	}

	@Override
	public void onBounceArrived(Bounce newbounce) {
		this.bounce = newbounce; 
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				addBounceView(bounce);
			}
		});
	}

	public void onLikeButtonClick(View v) {
		int position = galleryListview.getPositionForView(v);
		Log.d(TAG, "Pressed like for " + position);

		DataHolder.getDataHolder(getApplicationContext()).sendLike(bounce,
				position);

	}

	private void addBounceView(Bounce bounce) {
		galleryAdapter = new BounceOptionsHListAdapter(this, bounce);
		galleryListview.setAdapter(galleryAdapter);
		galleryListview.setOnItemClickListener(this);

		galleryListview.setFocusable(false);
		galleryListview.setFocusableInTouchMode(false);
		galleryListview.setClickable(false);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, parent + ":" + view + ":" + position + ":" + id);
		Intent i = new Intent(this, DisplayBounceOptionsFullScreen.class);
		i.putExtra("bounce_id", bounce.getBounceId());
		i.putExtra("position", position);
		this.startActivity(i);
	}

}
