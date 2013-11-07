package com.example.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.TextView;

import com.example.bouncecloud.R;

public class BouncesListAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	String TAG = "BouncesListAdapter";
	Context ctx;

	public BouncesListAdapter(Context ctx) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = ctx;
	}

	@Override
	public int getCount() {
		Log.d(TAG, "getCount called and returned"
				+ DataHolder.getDataHolder().getContactsSize());
		return DataHolder.getDataHolder().getBouncesSize();
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
			convertView = layoutInflater.inflate(R.layout.bounce_view, null);
			viewHolder = new ViewHolder();
			viewHolder.sender = (TextView) convertView
					.findViewById(R.id.sender_textview);
			viewHolder.receiver = (TextView) convertView
					.findViewById(R.id.receivers_textview);
			viewHolder.optionsGallery = (Gallery) convertView
					.findViewById(R.id.gallery_view);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		applySender(viewHolder.sender, position);
		applyReceiver(viewHolder.receiver, position);
		applyOptions(viewHolder.optionsGallery, position);

		return convertView;
	}

	private void applySender(TextView sender, int position) {
		Log.d(TAG, "setting sender to "
				+ DataHolder.getDataHolder().getBounceAtIndex(position)
						.getSender());
		sender.setText(DataHolder.getDataHolder().getBounceAtIndex(position)
				.getSender().toString());
	}

	private void applyReceiver(TextView receiver, int position) {
		Log.d(TAG, "setting receiver to "
				+ DataHolder.getDataHolder().getBounceAtIndex(position)
						.getReceiversAsString());
		receiver.setText(DataHolder.getDataHolder().getBounceAtIndex(position)
				.getReceiversAsString());
	}

	@SuppressLint("NewApi")
	private void applyOptions(Gallery optionsView, int position) {
		Log.d(TAG, "setting receiver to "
				+ DataHolder.getDataHolder().getBounceAtIndex(position)
						.getReceiversAsString());

		Bounce bounce = DataHolder.getDataHolder().getBounceAtIndex(position);

		BounceOptionsHListAdapter adapter = new BounceOptionsHListAdapter(ctx,
				bounce);
		
		adapter.setImageHeightMultiplier(0.3);
		optionsView.setAdapter(adapter);
	}

	static class ViewHolder {
		TextView sender;
		TextView receiver;
		Gallery optionsGallery;
	}

}
