package com.picktr.example.picktrbeta;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.picktr.example.definitions.Consts;
import com.picktr.example.helpers.Bounce;
import com.picktr.example.helpers.Contact;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.helpers.Utils;
import com.picktr.example.interfaces.BouncesListListener;
import com.picktr.example.interfaces.LikeListener;
import com.picktr.example.services.NetworkService;

public class DisplayBounceToSelf extends Activity implements
		BouncesListListener, LikeListener {

	public static final String TAG = "DisplayBounceFromSelf";

	Bounce bounce;
	DataHolder dataHolder;
	NetworkService networkService;
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
		setContentView(R.layout.display_bounce_to_self);

		Bundle extras = getIntent().getExtras();
		bounce_id = extras.getLong("bounce_id");
		dataHolder = DataHolder.getDataHolder(getApplicationContext());
		networkService = ((PicktrApplication) getApplication()).networkService;

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

	private void applyLikeButton(ToggleButton likeButton, int optionNumber) {
		likeButton.setVisibility(View.VISIBLE);
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
				int option = (Integer) v.getTag(R.string.option_number);
				networkService.sendLike(bounce, option);
			}
		});
	}

	private void openURL(int option) {
		String url = bounce.getOptions().get(option).getUrl();
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	private void applyOptions() {
		optionsView.removeAllViews();

		for (int i = 0; i < bounce.getNumberOfOptions(); i++) {

			View optionView = layoutInflater.inflate(
					R.layout.bounce_option_view_display_view, null);

			optionsView.addView(optionView);

			LayoutParams optionParams = optionView.getLayoutParams();
			optionParams.width = Utils.getDisplaySize(this).x;
			optionParams.height = Utils.getDisplaySize(this).x;
			optionView.setLayoutParams(optionParams);

			TextView optionText = (TextView) optionView
					.findViewById(R.id.option_text);
			optionText.setText(bounce.getOptions().get(i).getTitle());

			ImageView optionImage = (ImageView) optionView
					.findViewById(R.id.option_image);

			WebView optionWebview = (WebView) optionView
					.findViewById(R.id.option_webview);

			ImageButton fullScreenButton = (ImageButton) optionView
					.findViewById(R.id.fullscreen_button);

			fullScreenButton.setTag(R.string.bounce_id, bounce.getID());
			fullScreenButton.setTag(R.string.option_number, i);
			fullScreenButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int option = (Integer) v.getTag(R.string.option_number);
					startFullScreen(option);
				}
			});

			ImageButton openURLButton = (ImageButton) optionView
					.findViewById(R.id.open_url_button);
			openURLButton.setTag(R.string.bounce_id, bounce.getID());
			openURLButton.setTag(R.string.option_number, i);
			openURLButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int option = (Integer) v.getTag(R.string.option_number);
					openURL(option);
				}
			});

			ImageButton refreshButton = (ImageButton) optionView
					.findViewById(R.id.refresh_button);

			LayoutParams textParams = optionText.getLayoutParams();
			textParams.width = Utils.getDisplaySize(this).x;
			optionText.setLayoutParams(textParams);

			if (bounce.getOptions().get(i).getType() == Consts.CONTENT_TYPE_IMAGE) {
				Utils.displayImage(bounce.getOptions().get(i).getImage(),
						optionImage);
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
				openURLButton.setVisibility(View.INVISIBLE);
				optionWebview.setVisibility(View.INVISIBLE);
				optionImage.setVisibility(View.VISIBLE);
				refreshButton.setVisibility(View.INVISIBLE);
			} else if (bounce.getOptions().get(i).getType() == Consts.CONTENT_TYPE_URL) {
				Utils.setupWebView(optionWebview);
				optionWebview.setWebViewClient(new WebViewClient());
				optionWebview.loadUrl(bounce.getOptions().get(i).getUrl());
				openURLButton.setVisibility(View.VISIBLE);
				optionWebview.setVisibility(View.VISIBLE);

				setRefreshButton(refreshButton, optionWebview);
				refreshButton.setVisibility(View.VISIBLE);
				optionImage.setVisibility(View.INVISIBLE);
			}

			ToggleButton likeButton = (ToggleButton) optionView
					.findViewById(R.id.option_like);
			applyLikeButton(likeButton, i);

		}
	}

	private void setRefreshButton(ImageButton refreshButton,
			final WebView optionWebview) {
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				optionWebview.reload();
			}
		});
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
		timestampTextview.setText("Received "
				+ Utils.getTimeAgo(bounce.getSendAt(), this));
	}

	private void applyProfileImage() {
		senderProfileImage.setImageResource(R.drawable.no_photo_icon);

		Contact sender = dataHolder.getContactWithUserId(bounce.getSender());
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
		seenByTextview.setText("Seen by you");
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
		dataHolder.deregisterBouncesListListener(this);
		dataHolder.deregisterLikeListener(this);
		super.onDestroy();
	}

	@Override
	public void onBouncesChanged() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				bounce = dataHolder.getBounceWithInternalId(bounce_id);
				setupBounce();
			}
		});
	}

	@Override
	public void onLikesChanged() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				setupBounce();
			}
		});
	}
}
