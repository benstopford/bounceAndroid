package com.picktr.example.helpers;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.picktr.example.definitions.Consts;
import com.picktr.example.picktrbeta.R;

public class DraftsListAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	String TAG = "BouncesListAdapter";
	Context ctx;
	ArrayList<Bounce> bounces;
	DataHolder dataHolder;

	public DraftsListAdapter(Context ctx, ArrayList<Bounce> bounces) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = ctx;
		this.bounces = bounces;
		this.dataHolder = DataHolder.getDataHolder(ctx);
	}

	@Override
	public int getCount() {
		return bounces.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private void setupOptionsFirstTime(BounceViewHolder viewHolder) {
		viewHolder.options = new BounceOptionHolder[5];

		for (int i = 0; i < 5; i++) {
			BounceOptionHolder optionHolder = new BounceOptionHolder();
			viewHolder.options[i] = optionHolder;

			optionHolder.optionLayout = (RelativeLayout) layoutInflater
					.inflate(R.layout.bounce_option_view, null);
			optionHolder.optionImage = (ImageView) optionHolder.optionLayout
					.findViewById(R.id.option_image);
			optionHolder.optionText = (TextView) optionHolder.optionLayout
					.findViewById(R.id.option_text);
			optionHolder.optionLike = (ToggleButton) optionHolder.optionLayout
					.findViewById(R.id.option_like);
			optionHolder.optionWebview = (WebView) optionHolder.optionLayout
					.findViewById(R.id.option_webview);

			viewHolder.optionsLinearLayout.addView(optionHolder.optionLayout);

			LayoutParams optionParams = optionHolder.optionLayout
					.getLayoutParams();
			optionParams.width = Utils.getDisplaySize(ctx).x / 2 - 50;
			optionParams.height = Utils.getDisplaySize(ctx).x / 2 - 50;
			optionHolder.optionLayout.setLayoutParams(optionParams);

			LayoutParams textParams = optionHolder.optionText.getLayoutParams();
			textParams.width = Utils.getDisplaySize(ctx).x / 2 - 50;
			optionHolder.optionText.setLayoutParams(textParams);

		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BounceViewHolder viewHolder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.bounce_view, null);

			viewHolder = new BounceViewHolder();
			viewHolder.question = (TextView) convertView
					.findViewById(R.id.question_textview);
			viewHolder.timestamp = (TextView) convertView
					.findViewById(R.id.timestamp_textview);
			viewHolder.profileImage = (ImageView) convertView
					.findViewById(R.id.sender_profile_image);
			viewHolder.seenBy = (TextView) convertView
					.findViewById(R.id.seen_by_textview);
			viewHolder.topBar = (LinearLayout) convertView
					.findViewById(R.id.top_bar);
			viewHolder.optionsLinearLayout = (LinearLayout) convertView
					.findViewById(R.id.options_linear_layout);
			viewHolder.questionTimestampBlock = (LinearLayout) convertView
					.findViewById(R.id.question_timestamp_block);
			setupOptionsFirstTime(viewHolder);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (BounceViewHolder) convertView.getTag();
		}
		applyQuestion(viewHolder.question, position);
		applyTimestamp(viewHolder.timestamp, position);
		applyOptions(viewHolder, position);
		applySenderProfileImage(viewHolder.profileImage, position);
		applyDeleteButton(viewHolder.deleteButton, position);
		applyTopBar(viewHolder, position);
		return convertView;
	}

	private void applyTopBar(BounceViewHolder viewHolder, int position) {
		Bounce bounce = bounces.get(position);

		if (bounce.isDraft() || bounce.isFromSelf()) {
			viewHolder.topBar.removeAllViews();
			viewHolder.topBar.addView(viewHolder.seenBy);
			viewHolder.topBar.addView(viewHolder.questionTimestampBlock);
			viewHolder.topBar.addView(viewHolder.profileImage);
		} else {
			viewHolder.topBar.removeAllViews();
			viewHolder.topBar.addView(viewHolder.profileImage);
			viewHolder.topBar.addView(viewHolder.questionTimestampBlock);
			viewHolder.topBar.addView(viewHolder.seenBy);
		}
	}

	private void applyQuestion(TextView question, int position) {
		question.setText("");
		if (bounces.get(position).getQuestion() != null) {
			String text = bounces.get(position).getQuestion();
			if (text.length() > 30) {
				text = text.substring(0, 27) + "...";
			}
			question.setText(text);
		}
	}

	private void applyTimestamp(TextView timestamp, int position) {
		timestamp.setText("");

		Bounce bounce = bounces.get(position);
		String text = "";

		if (bounce.isDraft()) {
			text = "Draft";
		} else if (bounce.getStatus().equals(Consts.BOUNCE_STATUS_LOADING)) {
			text = "Loading...";
		} else if (bounce.getStatus().equals(Consts.BOUNCE_STATUS_SENDING)) {
			text = "Sending...";
		} else if (bounce.getStatus().equals(Consts.BOUNCE_STATUS_SENT)) {
			text = "Sent " + Utils.getTimeAgo(bounce.getSendAt(), ctx);
		} else if (bounce.getStatus().equals(Consts.BOUNCE_STATUS_RECEIVED)) {
			text = "Received " + Utils.getTimeAgo(bounce.getSendAt(), ctx);
		} else if (bounce.getStatus().equals(Consts.BOUNCE_STATUS_PENDING)) {
			text = "Pending...";
		}

		timestamp.setText(text);
	}

	private void applyDeleteButton(ImageButton button, int position) {
		button.setVisibility(View.INVISIBLE);
	}

	private void applySenderProfileImage(ImageView profileImage, int position) {
		profileImage.setImageResource(R.drawable.no_photo_icon);
		Bounce bounce = bounces.get(position);
		Contact user;

		if (bounce.isFromSelf()) {
			user = dataHolder.getSelf();
		} else {
			user = dataHolder.getContactWithUserId(bounce.getSender());
		}
		if (user != null && user.getProfileImage() != null) {
			// Log.d(TAG, "setting profile Image");
			Bitmap bmp = BitmapFactory.decodeByteArray(user.getProfileImage(),
					0, user.getProfileImage().length);
			profileImage.setImageBitmap(Utils.createRoundImage(bmp));
		} else {
			// Log.e(TAG, "user is " + user.getID() + " and profile image is "
			// + user.getProfileImage());
		}
	}

	private void applyLikeButton(ToggleButton likeButton, Bounce bounce,
			int optionNumber) {
		likeButton.setVisibility(View.INVISIBLE);
	}

	private void applyOptions(BounceViewHolder viewHolder, int position) {
		Bounce bounce = bounces.get(position);
		Log.d(TAG, "Bounce is " + bounce + " #:" + bounce.getNumberOfOptions());

		for (int i = 0; i < bounce.getOptions().size(); i++) {

			viewHolder.options[i].optionText.setText(bounce.getOptions().get(i)
					.getTitle());

			if (bounce.getOptions().get(i).getType() == Consts.CONTENT_TYPE_IMAGE) {
				Utils.displayImage(bounce.getOptions().get(i).getImage(),
						viewHolder.options[i].optionImage);
				viewHolder.options[i].optionWebview
						.setVisibility(View.INVISIBLE);
				viewHolder.options[i].optionImage.setVisibility(View.VISIBLE);
			} else if (bounce.getOptions().get(i).getType() == Consts.CONTENT_TYPE_URL) {
				WebView wb = viewHolder.options[i].optionWebview;
				viewHolder.options[i].optionWebview
						.setWebViewClient(new WebViewClient());
				viewHolder.options[i].optionWebview.loadUrl(bounce.getOptions()
						.get(i).getUrl());
				viewHolder.options[i].optionWebview.setVisibility(View.VISIBLE);
				viewHolder.options[i].optionImage.setVisibility(View.INVISIBLE);
				Utils.setupWebView(wb);
			}
			applyLikeButton(viewHolder.options[i].optionLike, bounce, i);
			viewHolder.options[i].optionLayout.setVisibility(View.VISIBLE);
		}

		for (int i = bounce.getNumberOfOptions(); i < 5; i++) {
			viewHolder.options[i].optionLayout.setVisibility(View.GONE);
		}
	}

	static class BounceViewHolder {
		TextView question;
		TextView timestamp;
		TextView seenBy;
		ImageView profileImage;
		ImageButton deleteButton;
		LinearLayout topBar;
		LinearLayout optionsLinearLayout;
		LinearLayout questionTimestampBlock;
		BounceOptionHolder[] options;
	}

	static class BounceOptionHolder {
		ImageView optionImage;
		WebView optionWebview;
		TextView optionText;
		ToggleButton optionLike;
		RelativeLayout optionLayout;
	}

}
