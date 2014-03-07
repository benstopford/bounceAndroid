package com.picktr.example.helpers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.picktr.example.definitions.Consts;
import com.picktr.example.picktrbeta.BounceCloudActivity;
import com.picktr.example.picktrbeta.MainActivity;
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
		viewHolder.options = new BounceOptionHolder[7];

		for (int i = 0; i < 7; i++) {
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
			viewHolder.overflowMenuButton = (ImageButton) convertView
					.findViewById(R.id.overflow_menu_button);
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
		applyOverflowMenuButton(viewHolder.overflowMenuButton, position);
		applyIsSeenBy(viewHolder.seenBy, position);
		applyTopBar(viewHolder, position);
		return convertView;
	}

	private void applyTopBar(BounceViewHolder viewHolder, int position) {
		Bounce bounce = bounces.get(position);

		if (bounce.isDraft() || bounce.isFromSelf()) {
			viewHolder.topBar.removeAllViews();
			viewHolder.topBar.addView(viewHolder.overflowMenuButton);
			viewHolder.topBar.addView(viewHolder.seenBy);
			viewHolder.topBar.addView(viewHolder.questionTimestampBlock);
			viewHolder.topBar.addView(viewHolder.profileImage);
		} else {
			viewHolder.topBar.removeAllViews();
			viewHolder.topBar.addView(viewHolder.profileImage);
			viewHolder.topBar.addView(viewHolder.questionTimestampBlock);
			viewHolder.topBar.addView(viewHolder.seenBy);
			viewHolder.topBar.addView(viewHolder.overflowMenuButton);
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

	private void showConfirmDialog(final long bounceID) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("Confirm");
		builder.setMessage("Are you sure?");
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dataHolder.removeBounceWithInternalID(bounceID);
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

	private PopupWindow popupWindowDogs = null;
	private int popupPosition = 0;

	private void applyOverflowMenuButton(ImageButton button, final int position) {
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindowDogs = popupWindowDogs();
				popupPosition = position;
				popupWindowDogs.showAsDropDown(v, -5, 0);
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

			viewHolder.options[i].optionText.setText(bounce.getOptions().get(i)
					.getTitle());

			if (bounce.getOptions().get(i).getType() == Consts.CONTENT_TYPE_IMAGE) {
				Utils.displayImage(bounce.getOptions().get(i).getImage(),
						viewHolder.options[i].optionImage);
				viewHolder.options[i].optionImage.setTag(R.string.bounce_id,
						bounce.getID());
				viewHolder.options[i].optionImage.setTag(
						R.string.option_number, i);
				viewHolder.options[i].optionImage
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								long bounceID = (Long) v
										.getTag(R.string.bounce_id);
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
				viewHolder.options[i].optionWebview
						.setVisibility(View.INVISIBLE);
				viewHolder.options[i].optionImage.setVisibility(View.VISIBLE);
			} else if (bounce.getOptions().get(i).getType() == Consts.CONTENT_TYPE_URL) {
				WebView wb = viewHolder.options[i].optionWebview;
				Utils.setupWebView(wb);
				viewHolder.options[i].optionWebview
						.setWebViewClient(new WebViewClient());
				viewHolder.options[i].optionWebview.loadUrl(bounce.getOptions()
						.get(i).getUrl());
				viewHolder.options[i].optionWebview.setVisibility(View.VISIBLE);
				viewHolder.options[i].optionImage.setVisibility(View.INVISIBLE);

				wb.setTag(R.string.bounce_id, bounce.getID());
				wb.setTag(R.string.option_number, i);
				wb.setOnTouchListener(new OnTouchListener() {

					private static final int MAX_CLICK_DURATION = 200;
					private long startClickTime;

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN: {
							startClickTime = Calendar.getInstance()
									.getTimeInMillis();
							break;
						}
						case MotionEvent.ACTION_UP: {
							long clickDuration = Calendar.getInstance()
									.getTimeInMillis() - startClickTime;
							if (clickDuration < MAX_CLICK_DURATION) {
								long bounceID = (Long) v
										.getTag(R.string.bounce_id);
								Bounce bounce = dataHolder
										.getBounceWithInternalId(bounceID);
								if (bounce == null) {
									Log.e(TAG, "bounce is null!!!");
									return false;
								}
								int option = (Integer) v
										.getTag(R.string.option_number);
								Utils.startBounceActivity(ctx, bounce, option);
							}
						}
						}
						return true;
					}

				});

			}
			applyLikeButton(viewHolder.options[i].optionLike, bounce, i);
			viewHolder.options[i].optionLayout.setVisibility(View.VISIBLE);
		}

		for (int i = bounce.getNumberOfOptions(); i < 7; i++) {
			viewHolder.options[i].optionLayout.setVisibility(View.GONE);
		}
	}

	static class BounceViewHolder {
		TextView question;
		TextView timestamp;
		TextView seenBy;
		ImageView profileImage;
		ImageButton overflowMenuButton;
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

	private void forwardBounce(int position) {
		Bounce bounce = bounces.get(position);
		Bounce newBounce = new Bounce();
		newBounce.setSender(dataHolder.getSelf().getUserID());
		newBounce.setSendAt(new Date(System.currentTimeMillis()));
		newBounce.setStatus(Consts.BOUNCE_STATUS_DRAFT);
		newBounce.setIsFromSelf(Consts.FROM_SELF);
		newBounce.setNumberOfOptions(bounce.getNumberOfOptions());
		newBounce.setOptions(bounce.getOptions());
		newBounce.setQuestion(bounce.getQuestion());
		newBounce.setID(dataHolder.addDraftBounce(newBounce));
		Utils.startBounceActivity(ctx, newBounce, 0);
	}

	public PopupWindow popupWindowDogs() {

		// initialize a pop up window type
		PopupWindow popupWindow = new PopupWindow(ctx);

		// the drop down list is a list view
		ListView listViewDogs = new ListView(ctx);
		listViewDogs.setBackgroundColor(ctx.getResources().getColor(
				R.color.lightgrey));

		String[] popUpContents = new String[2];
		popUpContents[0] = "Forward::1";
		popUpContents[1] = "Delete::2";
		// set our adapter and pass our pop up window contents
		listViewDogs.setAdapter(dogsAdapter(popUpContents));

		// set the item click listener
		listViewDogs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				// get the context and main activity to access variables
				Context mContext = v.getContext();

				// add some animation when a list item was clicked
				Animation fadeInAnimation = AnimationUtils.loadAnimation(
						v.getContext(), android.R.anim.fade_in);
				fadeInAnimation.setDuration(10);
				v.startAnimation(fadeInAnimation);

				// dismiss the pop up
				popupWindowDogs.dismiss();

				// get the id
				String selectedItemTag = ((TextView) v).getTag().toString();

				if (selectedItemTag.equals("2")) {
					showConfirmDialog(bounces.get(popupPosition).getID());
				} else {
					forwardBounce(popupPosition);
				}

			}

		});

		// some other visual settings
		popupWindow.setFocusable(true);
		popupWindow.setWidth(250);
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

		// set the list view as pop up window content
		popupWindow.setContentView(listViewDogs);

		return popupWindow;
	}

	private ArrayAdapter<String> dogsAdapter(String dogsArray[]) {

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx,
				android.R.layout.simple_list_item_1, dogsArray) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				// setting the ID and text for every items in the list
				String item = getItem(position);
				String[] itemArr = item.split("::");
				String text = itemArr[0];
				String id = itemArr[1];

				// visual settings for the list item
				TextView listItem = new TextView(ctx);

				listItem.setText(text);
				listItem.setTag(id);
				listItem.setTextSize(15);
				listItem.setPadding(10, 10, 10, 10);
				listItem.setTextColor(Color.parseColor("#555555"));

				return listItem;
			}
		};

		return adapter;
	}

}
