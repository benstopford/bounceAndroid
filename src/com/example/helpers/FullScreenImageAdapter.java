package com.example.helpers;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.bouncecloud.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class FullScreenImageAdapter extends PagerAdapter {

	private Activity _activity;
	private LayoutInflater inflater;
	private Bounce bounce; 

	// constructor
	public FullScreenImageAdapter(Activity activity,
			Bounce bounce) {
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

		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheInMemory().cacheOnDisc()
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		// Load and display image
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				_activity).defaultDisplayImageOptions(options).build();
		ImageLoader.getInstance().init(config);

		ImageLoader.getInstance().displayImage(
				"http://qbprod.s3.amazonaws.com/"
						+ bounce.getContentAt(position), image);

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