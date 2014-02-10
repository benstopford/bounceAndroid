package com.picktr.example.picktrbeta;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.picktr.example.helpers.Bounce;
import com.picktr.example.helpers.Contact;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.helpers.Like;
import com.picktr.example.helpers.Seen;
import com.picktr.example.helpers.Utils;
import com.picktr.example.interfaces.BouncesListListener;
import com.picktr.example.interfaces.LikeListener;

public class DisplayBounceFromSelf extends Activity implements LikeListener,
		BouncesListListener, OnItemClickListener {

	public static final String TAG = "DisplayBounceToSelfActivity";

	Bounce bounce;
	DataHolder dataHolder;
	long bounce_id;
	ImageView senderProfileImage;
	TextView questionTextview;
	TextView timestampTextview;
	TextView seenByTextview;
	LinearLayout optionsView;
	LayoutInflater layoutInflater;
	HorizontalScrollView optionsScrollView;
	int option = 0;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_bounce_from_self);
		Bundle extras = getIntent().getExtras();
		bounce_id = extras.getLong("bounce_id");

		dataHolder = DataHolder.getDataHolder(getApplicationContext());
		bounce = dataHolder.getBounceWithInternalId(bounce_id);

		dataHolder.registerBouncesListListener(this);
		dataHolder.registerLikeListener(this);
		senderProfileImage = (ImageView) findViewById(R.id.sender_profile_image);
		questionTextview = (TextView) findViewById(R.id.question_textview);
		timestampTextview = (TextView) findViewById(R.id.timestamp_textview);
		seenByTextview = (TextView) findViewById(R.id.seen_by_textview);
		optionsView = (LinearLayout) findViewById(R.id.options_linear_layout);
		optionsScrollView = (HorizontalScrollView) findViewById(R.id.options_scrollview);
		layoutInflater = getLayoutInflater();
		option = extras.getInt("option", 0);
		setupBounce();
		context = this;
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				optionsScrollView.scrollTo(Utils.getDisplaySize(context).x
						* option, 0);
			}
		}, 100L);

	}

	private void showDialogForLikes(ArrayList<Like> likes) {
		String text = "";
		if (likes.size() == 0) {
			text = "Not liked by anybody yet.";
		} else {
			ArrayList<String> names = new ArrayList<String>();
			for (int i = 0; i < likes.size(); i++) {
				{
					Contact contact = dataHolder.getContactWithUserId(likes
							.get(i).getSenderId());
					if (contact != null)
						names.add(contact.getDisplayName());
				}
			}
			text = "Liked by " + names.get(0);
			for (int i = 1; i < names.size() - 1; i++) {
				text += ", " + names.get(i);
			}
			text += " & " + names.get(names.size() - 1);
			if (names.size() == 1) {
				text = "Liked by " + names.get(0);
			}
		}
		new AlertDialog.Builder(this).setTitle("Likes").setMessage(text).show();
	}

	private void applyLikeButton(ToggleButton likeButton, int optionNumber) {

		likeButton.setVisibility(View.VISIBLE);
		likeButton.setText(String.valueOf(dataHolder.getAllLikes(
				bounce.getQBID(), optionNumber).size()));

		likeButton.setTag(R.string.bounce_id, bounce.getQBID());
		likeButton.setTag(R.string.option_number, optionNumber);
		likeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int option = (Integer) v.getTag(R.string.option_number);
				ArrayList<Like> likes = dataHolder.getAllLikes(
						bounce.getQBID(), option);
				showDialogForLikes(likes);
				ToggleButton button = (ToggleButton) v;
				button.setChecked(false);
				button.setText(String.valueOf(dataHolder.getAllLikes(
						bounce.getQBID(), option).size()));
			}
		});
	}

	private void applyOptions() {
		optionsView.removeAllViews();

		for (int i = 0; i < bounce.getNumberOfOptions(); i++) {
			View optionView = layoutInflater.inflate(
					R.layout.bounce_option_view, null);
			TextView optionText = (TextView) optionView
					.findViewById(R.id.option_text);
			optionText.setText(bounce.getOptions().get(i).getTitle());

			ImageView optionImage = (ImageView) optionView
					.findViewById(R.id.option_image);

			LayoutParams imageParams = optionImage.getLayoutParams();
			imageParams.width = Utils.getDisplaySize(this).x;
			imageParams.height = Utils.getDisplaySize(this).x;
			optionImage.setLayoutParams(imageParams);

			LayoutParams textParams = optionText.getLayoutParams();
			textParams.width = Utils.getDisplaySize(this).x;
			optionText.setLayoutParams(textParams);
			Utils.displayImage(bounce.getOptions().get(i).getImage(),
					optionImage);

			ToggleButton likeButton = (ToggleButton) optionView
					.findViewById(R.id.option_like);
			applyLikeButton(likeButton, i);

			optionImage.setTag(R.string.bounce_id, bounce.getID());
			optionImage.setTag(R.string.option_number, i);

			optionImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int option = (Integer) v.getTag(R.string.option_number);
					startFullScreen(option);
				}
			});
			optionsView.addView(optionView);
		}
	}

	private void startFullScreen(int position) {
		Intent i = new Intent(this, DisplayBounceOptionsFullScreen.class);
		i.putExtra("bounce_id", bounce.getID());
		i.putExtra("position", position);
		this.startActivity(i);
	}

	private void applyQuestion() {
		if (bounce.getQuestion() != null)
			questionTextview.setText(bounce.getQuestion());
	}

	private void applyTimestamp() {
		timestampTextview.setText("Sent "
				+ Utils.getTimeAgo(bounce.getSendAt(), this));
	}

	private void applyProfileImage() {
		senderProfileImage.setImageResource(R.drawable.no_photo_icon);

		Contact sender = dataHolder.getSelf();
		if (sender != null && sender.getProfileImage() != null) {
			// Log.d(TAG, "setting profile Image");
			Bitmap bmp = BitmapFactory.decodeByteArray(
					sender.getProfileImage(), 0,
					sender.getProfileImage().length);
			senderProfileImage.setImageBitmap(bmp);
		} else {
			// Log.e(TAG, "user is " + user.getID() + " and profile image is "
			// + user.getProfileImage());
		}
	}

	private void applySeenBy() {
		ArrayList<Seen> whoSaw = dataHolder.getAllSeenBy(bounce.getQBID());

		String text = "Seen by ";
		if (whoSaw.size() == 0) {
			text = "Not seen by anybody yet.";
		} else {
			ArrayList<String> names = new ArrayList<String>();
			for (int i = 0; i < whoSaw.size(); i++) {
				{
					Contact contact = dataHolder.getContactWithUserId(whoSaw
							.get(i).getContactID());
					if (contact != null)
						names.add(contact.getDisplayName());
				}
			}
			text = "Seen by " + names.get(0);
			for (int i = 1; i < names.size() - 1; i++) {
				text += ", " + names.get(i);
			}
			text += " & " + names.get(names.size() - 1);
			if (names.size() == 1)
				text = "Seen by " + names.get(0);
		}

		seenByTextview.setText(text);
	}

	private void setupBounce() {
		applyQuestion();
		applyTimestamp();
		applyProfileImage();
		applySeenBy();
		applyOptions();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		dataHolder.deregisterLikeListener(this);
		dataHolder.deregisterBouncesListListener(this);
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, parent + ":" + view + ":" + position + ":" + id);
		Intent i = new Intent(this, DisplayBounceOptionsFullScreen.class);
		i.putExtra("bounce_id", bounce.getID());
		i.putExtra("position", position);
		this.startActivity(i);
	}

	@Override
	public void onLikesChanged() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bounce = dataHolder.getBounceWithInternalId(bounce_id);
				setupBounce();
			}
		});
	}

	@Override
	public void onBouncesChanged() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bounce = dataHolder.getBounceWithInternalId(bounce_id);
				setupBounce();
			}
		});
	}

}
