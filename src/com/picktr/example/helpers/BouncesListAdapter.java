package com.picktr.example.helpers;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.picktr.example.definitions.Consts;
import com.picktr.example.picktrbeta.R;
import com.picktr.example.services.NetworkService;

public class BouncesListAdapter extends BaseAdapter {

	LayoutInflater layoutInflater;
	String TAG = "BouncesListAdapter";
	Context ctx;
	ArrayList<Bounce> bounces;
	DataHolder dataHolder;
	ImageLoader imageLoader;
	NetworkService networkService;

	public BouncesListAdapter(Context ctx, ArrayList<Bounce> bounces,
			NetworkService networkService) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = ctx;
		this.bounces = bounces;
		this.dataHolder = DataHolder.getDataHolder(ctx);
		this.networkService = networkService;
		setupImageLoader();
	}

	public void setBounces(ArrayList<Bounce> bounces) {
		this.bounces = bounces;
		notifyDataSetChanged();
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

			LayoutParams imageParams = optionHolder.optionImage
					.getLayoutParams();
			imageParams.width = Utils.getDisplaySize(ctx).x / 2 - 50;
			imageParams.height = Utils.getDisplaySize(ctx).x / 2 - 50;
			optionHolder.optionImage.setLayoutParams(imageParams);

			LayoutParams textParams = optionHolder.optionText.getLayoutParams();
			textParams.width = Utils.getDisplaySize(ctx).x / 2 - 50;
			optionHolder.optionText.setLayoutParams(textParams);

			viewHolder.optionsLinearLayout.addView(optionHolder.optionLayout);
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
			viewHolder.deleteButton = (ImageButton) convertView
					.findViewById(R.id.delete_button);
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
		applyIsSeenBy(viewHolder.seenBy, position);
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

	private void applyIsSeenBy(TextView seenByView, int position) {
		seenByView.clearComposingText();
		seenByView.setText("");
		Bounce bounce = bounces.get(position);
		if (!bounce.isFromSelf() && !bounce.isDraft() && !bounce.isSeen()) {
			// First time we see a bounce!
			bounce.setIsSeen(1); // 1 - for true
			dataHolder.updateBounce(bounce);
			networkService.sendIsSeenMessage(bounce);
		}

		String text = "";

		if (bounce.isDraft()
				|| bounce.getStatus().equals(Consts.BOUNCE_STATUS_LOADING)
				|| bounce.getStatus().equals(Consts.BOUNCE_STATUS_SENDING)) {
			text = "";
		} else if (bounce.isFromSelf()) {
			ArrayList<Seen> whoSaw = dataHolder.getAllSeenBy(bounce.getQBID());
			text = "Seen by ";
			ArrayList<String> names = new ArrayList<String>();
			for (int i = 0; i < whoSaw.size(); i++) {
				{
					Contact contact = dataHolder.getContactWithUserId(whoSaw
							.get(i).getContactID());
					if (contact != null)
						names.add(contact.getDisplayName());
				}
			}

			if (names.size() == 0) {
				text = "Not seen by anybody yet.";
			} else if (names.size() == 1) {
				text = "Seen by " + names.get(0);
			} else if (names.size() <= 3) {
				text = "Seen by " + names.get(0);
				for (int i = 1; i < names.size() - 1; i++) {
					text += ", " + names.get(i);
				}
				text += " & " + names.get(names.size() - 1);
			} else if (names.size() > 3) {
				text = "Seen by " + names.get(0);
				for (int i = 1; i < 3; i++) {
					text += ", " + names.get(i);
				}
				text += " & " + (names.size() - 3) + " others";
			}
		} else {
			text = "Seen by you";
		}
		seenByView.setText(text);
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
				// Log.d(TAG, "onButtonClick called");
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
		if (bounce.isDraft()
				|| bounce.getStatus().equals(Consts.BOUNCE_STATUS_LOADING)
				|| bounce.getStatus().equals(Consts.BOUNCE_STATUS_SENDING)) {
			likeButton.setVisibility(View.INVISIBLE);
			return;
		}
		likeButton.setVisibility(View.VISIBLE);

		if (bounce.isFromSelf()) {
			likeButton.setChecked(false);
			likeButton.setClickable(false);
			likeButton.setText(String.valueOf(dataHolder.getAllLikes(
					bounce.getQBID(), optionNumber).size()));
		} else {
			if (dataHolder.getIsLikedBySelf(bounce.getQBID(), optionNumber)) {
				likeButton.setChecked(true);
			} else {
				likeButton.setChecked(false);
			}

			likeButton.setTag(R.string.bounce_id, bounce.getQBID());
			likeButton.setTag(R.string.option_number, optionNumber);

			likeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String bounce_id = (String) v.getTag(R.string.bounce_id);
					Bounce bounce = dataHolder.getBounceWithId(bounce_id);
					int option = (Integer) v.getTag(R.string.option_number);
					networkService.sendLike(bounce, option);
				}
			});

		}

	}

	private void setupImageLoader() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheInMemory().cacheOnDisc()
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.displayer(new RoundedBitmapDisplayer(2)).build();
		// Load and display image
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				ctx).defaultDisplayImageOptions(options).build();
		ImageLoader.getInstance().init(config);
		imageLoader = ImageLoader.getInstance();

	}

	private void applyOptions(BounceViewHolder viewHolder, int position) {
		Bounce bounce = bounces.get(position);
		Log.d(TAG, "Bounce is " + bounce + " #:" + bounce.getNumberOfOptions());

		for (int i = 0; i < bounce.getOptions().size(); i++) {
			Utils.displayImage(bounce.getOptions().get(i).getImage(),
					viewHolder.options[i].optionImage);
			applyLikeButton(viewHolder.options[i].optionLike, bounce, i);
			viewHolder.options[i].optionText.setText(bounce.getOptions().get(i)
					.getTitle());
			viewHolder.options[i].optionImage.setTag(R.string.bounce_id,
					bounce.getID());
			viewHolder.options[i].optionImage.setTag(R.string.option_number, i);
			viewHolder.options[i].optionImage
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							long bounceID = (Long) v.getTag(R.string.bounce_id);
							Bounce bounce = dataHolder
									.getBounceWithInternalId(bounceID);
							if (bounce == null) {
								Log.e(TAG, "bounce is null!!!");
								return;
							}

							int option = (Integer) v
									.getTag(R.string.option_number);
							Utils.startBounceActivity(ctx, bounce, option);
						}
					});
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
		TextView optionText;
		ToggleButton optionLike;
		RelativeLayout optionLayout;
	}

}
