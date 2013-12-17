package com.example.helpers;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.bouncecloud.R;

public class BouncesListAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	String TAG = "BouncesListAdapter";
	Context ctx;
	ArrayList<Bounce> bounces;
	DataHolder dataHolder;

	public BouncesListAdapter(Context ctx, ArrayList<Bounce> bounces) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = ctx;
		this.bounces = bounces;
		this.dataHolder = DataHolder.getDataHolder(ctx);
	}

	public void setBounces(ArrayList<Bounce> bounces) {
		this.bounces = bounces;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		Log.d(TAG, "getCount called and returned" + bounces.size());
		return bounces.size();
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
			viewHolder.question = (TextView) convertView
					.findViewById(R.id.question_textview);
			viewHolder.timestamp = (TextView) convertView
					.findViewById(R.id.timestamp_textview);
			viewHolder.optionsLayout = (LinearLayout) convertView
					.findViewById(R.id.options_linear_layout);
			viewHolder.profileImage = (ImageView) convertView
					.findViewById(R.id.sender_profile_image);
			viewHolder.deleteButton = (ImageButton) convertView
					.findViewById(R.id.delete_button);
			viewHolder.seenBy = (TextView) convertView
					.findViewById(R.id.seen_by_textview);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		applyQuestion(viewHolder.question, position);
		applyTimestamp(viewHolder.timestamp, position);
		applyOptions(viewHolder.optionsLayout, position);
		applySenderProfileImage(viewHolder.profileImage, position);
		applyDeleteButton(viewHolder.deleteButton, position);
		applyIsSeenBy(viewHolder.seenBy, position);
		
		

		return convertView;
	}

	private void applyIsSeenBy(TextView seenByView, int position) {
		seenByView.clearComposingText();
		Bounce bounce = bounces.get(position);
		if (!bounce.isFromSelf() && !bounce.isDraft() && !bounce.isSeen()) {
			// First time we see a bounce!
			bounce.setIsSeen(1); // 1 - for true
			dataHolder.updateBounce(bounce);
			dataHolder.sendIsSeenMessage(bounce);
		}

		if (bounce.isFromSelf()) {
			ArrayList<Seen> whoSaw = dataHolder.getAllSeenBy(bounce
					.getBounceId());
			String seenByLine = "Seen By";
			for (int i = 0; i < whoSaw.size(); i++) {
				seenByLine += whoSaw.get(i).getContactID();
			}
			seenByView.setText(seenByLine);
		} else {
			seenByView.setText("Seen by you");
		}

	}

	private void applyQuestion(TextView question, int position) {
		question.clearComposingText();
		if (bounces.get(position).getQuestion() != null)
			question.setText(bounces.get(position).getQuestion().toString());
	}

	private void applyTimestamp(TextView timestamp, int position) {
		timestamp.clearComposingText();

		Log.d(TAG, "setting timestamp to " + bounces.get(position).getSendAt());

		timestamp.setText("Sent "
				+ Utils.getTimeAgo(bounces.get(position).getSendAt(), ctx)
				+ "status : " + bounces.get(position).getStatus());
	}

	private void showConfirmDialog(final String bounceID) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("Confirm");
		builder.setMessage("Are you sure?");
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dataHolder.removeBounceWithInternalID(Integer
						.parseInt(bounceID));
				dialog.dismiss();
			}

		});
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void applyDeleteButton(ImageButton button, int position) {
		button.setTag(bounces.get(position).getID());
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "onButtonClick called");
				showConfirmDialog(v.getTag().toString());
			}
		});
	}

	private void applySenderProfileImage(ImageView profileImage, int position) {
		profileImage.setImageResource(R.drawable.no_photo_icon);
		Bounce bounce = bounces.get(position);
		Contact user;

		if (bounce.isFromSelf()) {
			user = dataHolder.getSelf();
			Log.d(TAG, "user is Self, setting personal profile Image");
		} else {
			user = dataHolder.getContactWithUserId(bounce.getSender());
		}
		if (user != null && user.getProfileImage() != null) {
			Bitmap bmp = BitmapFactory.decodeByteArray(user.getProfileImage(),
					0, user.getProfileImage().length);
			profileImage.setImageBitmap(bmp);
		} else {

		}
	}

	@SuppressLint("NewApi")
	private void applyOptions(LinearLayout optionsView, int position) {
		Bounce bounce = bounces.get(position);
		optionsView.removeAllViews();

		for (int i = 0; i < bounce.getNumberOfOptions(); i++) {
			ImageView image = new ImageView(ctx);

			RelativeLayout bounceOptionView = new RelativeLayout(ctx);
			LinearLayout.LayoutParams bounceOptionparams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			bounceOptionparams.setMargins(0, 0, 5, 5);
			bounceOptionView.setLayoutParams(bounceOptionparams);

			RelativeLayout.LayoutParams optionImageparams = new RelativeLayout.LayoutParams(
					Utils.getDisplaySize(ctx).x / 2,
					Utils.getDisplaySize(ctx).x / 2);
			optionImageparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			image.setLayoutParams(optionImageparams);
			Utils.displayImage(ctx, bounce.getContentAt(i), image);
			bounceOptionView.addView(image);

			TextView optionTextView = new TextView(ctx);
			RelativeLayout.LayoutParams optionTextParams = new RelativeLayout.LayoutParams(
					Utils.getDisplaySize(ctx).x / 2,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			optionTextParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			optionTextView.setLayoutParams(optionTextParams);
			optionTextView.setBackgroundColor(Color.parseColor("#60000000"));
			optionTextView.setText(bounce.getOptionNames().get(i));
			bounceOptionView.addView(optionTextView);

			optionsView.addView(bounceOptionView);

		}
	}

	static class ViewHolder {
		TextView question;
		TextView timestamp;
		TextView seenBy;
		LinearLayout optionsLayout;
		ImageView profileImage;
		ImageButton deleteButton;
	}

}
