package com.picktr.example.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.picktr.example.definitions.Consts;
import com.picktr.example.picktrbeta.R;

public class FullScreenImageAdapter extends PagerAdapter {

	private Activity _activity;
	private LayoutInflater inflater;
	private Bounce bounce;

	// constructor
	public FullScreenImageAdapter(Activity activity, Bounce bounce) {
		this._activity = activity;
		this.bounce = bounce;
	}

	@Override
	public int getCount() {
		return this.bounce.getNumberOfOptions();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((RelativeLayout) object);
	}

	private void openURL(int option) {
		String url = bounce.getOptions().get(option).getUrl();
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		_activity.startActivity(i);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ImageView image;
		Button btnClose;
		WebView webview;

		inflater = (LayoutInflater) _activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image,
				container, false);

		image = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
		btnClose = (Button) viewLayout.findViewById(R.id.btnClose);
		webview = (WebView) viewLayout.findViewById(R.id.imgWebview);

		ImageButton openURLButton = (ImageButton) viewLayout
				.findViewById(R.id.open_url_button);
		openURLButton.setTag(R.string.bounce_id, bounce.getID());
		openURLButton.setTag(R.string.option_number, position);
		openURLButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int option = (Integer) v.getTag(R.string.option_number);
				openURL(option);
			}
		});

		ImageButton refreshButton = (ImageButton) viewLayout
				.findViewById(R.id.refresh_button);

		if (bounce.getOptions().get(position).getType() == Consts.CONTENT_TYPE_IMAGE) {
			image.setVisibility(View.VISIBLE);
			webview.setVisibility(View.INVISIBLE);
			Utils.displayImage(bounce.getOptions().get(position).getImage(),
					image);
			openURLButton.setVisibility(View.INVISIBLE);
			refreshButton.setVisibility(View.INVISIBLE);
		} else if (bounce.getOptions().get(position).getType() == Consts.CONTENT_TYPE_URL) {
			webview.setWebViewClient(new WebViewClient());
			webview.loadUrl(bounce.getOptions().get(position).getUrl());
			image.setVisibility(View.INVISIBLE);
			webview.setVisibility(View.VISIBLE);
			setRefreshButton(refreshButton, webview);
			openURLButton.setVisibility(View.VISIBLE);
			refreshButton.setVisibility(View.VISIBLE);
		}

		// close button click event
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_activity.finish();
			}
		});

		((ViewPager) container).addView(viewLayout);

		return viewLayout;
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

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((RelativeLayout) object);

	}
}