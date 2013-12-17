package com.example.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.bouncecloud.R;

@SuppressLint("NewApi")
public class BounceOptionsHListAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	String TAG = "BounceOptionsHorizontalListAdapter";
	Context ctx;
	Bounce bounce;
	double image_height_mult = 2.0 / 3.0;

	public BounceOptionsHListAdapter(Context ctx, Bounce bounce) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = ctx;
		this.bounce = bounce;
	}

	@Override
	public int getCount() {
		Log.d(TAG, "getCount called");
		return bounce.getNumberOfOptions();
	}

	@Override
	public Object getItem(int position) {
		Log.d(TAG, "getItem called");
		return null;
	}

	@Override
	public long getItemId(int position) {
		Log.d(TAG, "getItemId called");
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "getView called");
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.bounce_option_view,
					null);
			viewHolder = new ViewHolder();
			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
			viewHolder.likeButton = (Button) convertView
					.findViewById(R.id.like_button);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		applyImage(viewHolder.image, position);
		applyLikeButton(viewHolder.likeButton, position);

		return convertView;
	}

	private static Point getDisplaySize(final Display display) {
		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) { // Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		return point;
	}

	@SuppressLint("NewApi")
	private void applyImage(ImageView image, int position) {

		Utils.displayImage(ctx, bounce.getContentAt(position), image);

		Point size = Utils.getDisplaySize(ctx);
		int width = size.x;
		int height = size.y;

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				width, (int) Math.round(image_height_mult * height));

		layoutParams.addRule(RelativeLayout.BELOW, R.id.like_button);
		image.setLayoutParams(layoutParams);

	}

	private void applyLikeButton(Button button, int position) {

	}

	static class ViewHolder {
		Button likeButton;
		ImageView image;
	}

	public void setImageHeightMultiplier(double d) {
		image_height_mult = d;
	}

}
