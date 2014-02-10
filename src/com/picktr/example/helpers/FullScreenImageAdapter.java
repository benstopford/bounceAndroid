package com.picktr.example.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.picktr.example.picktrbeta.R;

public class FullScreenImageAdapter extends PagerAdapter {

	private Activity _activity;
	private LayoutInflater inflater;
	private Bounce bounce;

	// constructor
	public FullScreenImageAdapter(Activity activity, Bounce bounce) {
		this._activity = activity;
		this.bounce = bounce;
	}

	@Override
	public int getCount() {
		return this.bounce.getNumberOfOptions();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((RelativeLayout) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ImageView image;
		Button btnClose;

		inflater = (LayoutInflater) _activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image,
				container, false);

		image = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
		btnClose = (Button) viewLayout.findViewById(R.id.btnClose);

		Utils.displayImage(bounce.getOptions().get(position).getImage(), image);

		// close button click event
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_activity.finish();
			}
		});

		((ViewPager) container).addView(viewLayout);

		return viewLayout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((RelativeLayout) object);

	}
}