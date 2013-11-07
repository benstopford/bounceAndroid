package com.example.bouncecloud;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;

import com.example.helpers.Bounce;
import com.example.helpers.BounceOptionsResultsAdapter;
import com.example.helpers.DataHolder;
import com.example.helpers.Like;
import com.example.interfaces.BounceListener;
import com.example.interfaces.LikeListener;

public class DisplayBounceFromSelf extends Activity implements BounceListener,
		LikeListener, OnItemClickListener {

	public static final String TAG = "DisplayBounceToSelfActivity";

	Gallery galleryListview;
	BounceOptionsResultsAdapter galleryAdapter = null;
	Bounce bounce;
	ArrayList<Like> likes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_bounce_from_self);

		Bundle extras = getIntent().getExtras();
		String bounce_id = extras.getString("bounce_id");
		bounce = DataHolder.getDataHolder().getBounceWithId(bounce_id);
		DataHolder.getDataHolder().registerBounceListener(this, bounce_id);
		DataHolder.getDataHolder().registerLikeListener(this, bounce_id);
		DataHolder.getDataHolder().getLikes(bounce_id);

		galleryListview = (Gallery) findViewById(R.id.options_horizontal_listview);

		if (bounce != null)
			addBounceView(bounce);
	}

	@Override
	public void onBounceArrived(Bounce bounce) {
		addBounceView(bounce);
	}

	public void onLikeButtonClick(View v) {
		int position = galleryListview.getPositionForView(v);
		Log.d(TAG, "Pressed like for " + position);

		galleryAdapter.changeVisibility(position);
		galleryAdapter.notifyDataSetChanged();

		// DataHolder.getDataHolder().sendLike(bounce, position);

	}

	private void addBounceView(Bounce bounce) {
		galleryAdapter = new BounceOptionsResultsAdapter(this, bounce, likes);
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

	@Override
	public void onLikeArrived(Like like) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLikesArrived(ArrayList<Like> likes) {
		this.likes = likes;
		if (galleryAdapter != null) {
			galleryAdapter.setLikes(likes);
			galleryAdapter.notifyDataSetChanged();
		}
	}

}
