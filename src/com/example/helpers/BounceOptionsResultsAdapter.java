package com.example.helpers;

import java.util.ArrayList;
import java.util.HashSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.bouncecloud.R;

@SuppressLint("NewApi")
public class BounceOptionsResultsAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	String TAG = "BounceOptionsHorizontalListAdapter";
	Context ctx;
	Bounce bounce;
	ArrayList<Like> likes;
	HashSet<Integer> visible;

	public BounceOptionsResultsAdapter(Context ctx, Bounce bounce,
			ArrayList<Like> likes) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = ctx;
		this.bounce = bounce;
		this.likes = likes;
		visible = new HashSet<Integer>();
	}

	public void setLikes(ArrayList<Like> likes) {
		this.likes = likes;
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
			convertView = layoutInflater.inflate(
					R.layout.bounce_option_result_view, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
			viewHolder.likeButton = (Button) convertView
					.findViewById(R.id.like_button);
			viewHolder.text = (TextView) convertView
					.findViewById(R.id.like_text);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		applyImage(viewHolder.image, position);
		applyLikeButton(viewHolder.likeButton, position);
		applyText(viewHolder.text, position);

		return convertView;
	}

	private Integer getNumberOfLikes(int option) {
		int result = 0;
		if (likes == null)
			return result;
		for (int i = 0; i < likes.size(); i++)
			if (likes.get(i).getOption() == option)
				++result;
		return result;
	}

	private String getLoginsOfLikes(int option) {
		String result = "";
		if (likes == null)
			return "Nobody yet";
		for (int i = 0; i < likes.size(); i++)
			if (likes.get(i).getOption() == option) {
				if (result.length() > 0)
					result += ", ";
				result += likes.get(i).getSenderLogin();
			}
		if (result.length() == 0)
			return "Nobody yet";
		return result;
	}

	private void applyImage(ImageView image, int position) {

		Utils.displayImage(ctx, bounce.getContentAt(position), image);

		Point size = Utils.getDisplaySize(ctx);
		int width = size.x;
		int height = size.y;

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				width, height * 2 / 3);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.like_button);
		image.setLayoutParams(layoutParams);
	}

	private void applyLikeButton(Button button, int position) {
		button.setText(getNumberOfLikes(position).toString());
	}

	private void applyText(TextView likeText, int position) {
		likeText.setText(getLoginsOfLikes(position) + " liked this option");
		if (visible.contains(position)) {
			likeText.setVisibility(View.VISIBLE);
		} else {
			likeText.setVisibility(View.INVISIBLE);
		}
	}

	static class ViewHolder {
		Button likeButton;
		ImageView image;
		TextView text;
	}

	public void changeVisibility(int position) {
		if (visible.contains(position)) {
			visible.remove(position);
		} else {
			visible.add(position);
		}
	}

}
