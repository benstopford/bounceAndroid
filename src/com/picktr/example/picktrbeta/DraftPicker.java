package com.picktr.example.picktrbeta;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import com.picktr.example.definitions.Consts;
import com.picktr.example.helpers.Bounce;
import com.picktr.example.helpers.BounceOption;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.helpers.DraftsListAdapter;
import com.picktr.example.helpers.Utils;

public class DraftPicker extends Activity implements OnItemClickListener {

	String TAG = "Draft";
	ListView draftsListview;
	ArrayList<Bounce> bounces;
	DataHolder dataHolder;
	DraftsListAdapter adapter;
	String link;
	BounceOption bounceOption;
	WebView webView;
	Button newDraftButton;
	View headerView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.draft_picker);
		dataHolder = DataHolder.getDataHolder(getApplicationContext());
		draftsListview = (ListView) findViewById(R.id.drafts_list);
		webView = (WebView) findViewById(R.id.webview);

		headerView = getLayoutInflater().inflate(
				R.layout.drafts_listview_header, null);

		draftsListview.addHeaderView(headerView);

		newDraftButton = (Button) headerView
				.findViewById(R.id.new_draft_button);
		newDraftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNewDraftClicked();
			}
		});

		bounces = dataHolder.getAllDrafts();
		adapter = new DraftsListAdapter(this, bounces);
		draftsListview.setAdapter(adapter);
		draftsListview.setOnItemClickListener(this);
		link = getIntent().getStringExtra(Intent.EXTRA_TEXT);

		bounceOption = new BounceOption();
		bounceOption.setType(Consts.CONTENT_TYPE_URL);
		bounceOption.setUrl(link);

		setupWebView();
		Log.d(TAG, "link  = " + link);
	}

	private void setupWebView() {
		Utils.setupWebView(webView);
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl(link);

		LinearLayout.LayoutParams params = (LayoutParams) webView
				.getLayoutParams();
		params.height = Utils.getDisplaySize(this).x / 2;
		params.width = Utils.getDisplaySize(this).x / 2;
		webView.setLayoutParams(params);
	}

	private void onNewDraftClicked() {
		Bounce bounce = new Bounce();
		bounce.setSender(dataHolder.getSelf().getUserID());
		bounce.setSendAt(new Date(System.currentTimeMillis()));
		bounce.setStatus(Consts.BOUNCE_STATUS_DRAFT);
		bounce.setIsFromSelf(Consts.FROM_SELF);
		bounceOption.setOptionNumber(0);
		bounceOption.setTitle(webView.getTitle());
		bounce.addOption(bounceOption);
		bounce.setNumberOfOptions(1);
		bounce.setID(dataHolder.addDraftBounce(bounce));
		Utils.startBounceActivity(this, bounce, 0);
		finish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		Bounce bounce = bounces.get(pos - 1);

		if (bounce.getNumberOfOptions() < 5) {
			bounceOption.setOptionNumber(bounce.getNumberOfOptions());
			bounceOption.setTitle(webView.getTitle());
			bounce.addOption(bounceOption);
			bounce.setNumberOfOptions(bounce.getNumberOfOptions() + 1);
			dataHolder.updateBounce(bounce);
			Log.d(TAG, "on item click " + bounce.getNumberOfOptions() + " "
					+ bounce.getOptions().size());
			Utils.startBounceActivity(this, bounce,
					bounceOption.getOptionNumber());
			finish();
		} else {
			Toast.makeText(this, "Sorry, this draft already has 5 options.",
					Toast.LENGTH_SHORT).show();
		}
	}
}
