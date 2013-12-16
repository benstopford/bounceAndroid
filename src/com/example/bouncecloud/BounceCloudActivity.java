package com.example.bouncecloud;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.example.definitions.Consts;
import com.example.helpers.BounceCloudPageAdapter;

public class BounceCloudActivity extends FragmentActivity implements
		OnPageChangeListener {

	private static final String TAG = "BounceCloudActivity";
	private ViewPager viewPager;
	private BounceCloudPageAdapter adapter;
	private View settingsHighlighter;
	private View bouncesHighlighter;
	private View contactsHighlighter;
	private ImageView settingsIcon;
	private ImageView bouncesIcon;
	private ImageView contactsIcon;
	private List<Fragment> fragments;

	private int current = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bouncecloud_activity);
		viewPager = (ViewPager) findViewById(R.id.viewpager);

		settingsHighlighter = (View) findViewById(R.id.personal_highlighter);
		bouncesHighlighter = (View) findViewById(R.id.bounces_highlighter);
		contactsHighlighter = (View) findViewById(R.id.contacts_highlighter);

		settingsIcon = (ImageView) findViewById(R.id.personal_info_button);
		bouncesIcon = (ImageView) findViewById(R.id.bounces_button);
		contactsIcon = (ImageView) findViewById(R.id.contacts_button);

		fragments = getFragments();
		adapter = new BounceCloudPageAdapter(getSupportFragmentManager(),
				fragments);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(1);

		viewPager.setOnPageChangeListener(this);

		current = 1;
		highlight(1);

		settingsIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				highlight(0);
			}
		});

		bouncesIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				highlight(1);
			}
		});

		contactsIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				highlight(2);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivity Result called requestCode is " + requestCode
				+ " and Result code is " + resultCode);
		if (requestCode == Consts.YOUR_SELECT_PICTURE_REQUEST_CODE
				|| requestCode == Consts.PIC_CROP) {
			Log.d(TAG, "calling " + fragments.get(0));
			fragments.get(0).onActivityResult(requestCode, resultCode, data);
		}

	};

	private List<Fragment> getFragments() {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(new PersonalFragment());
		fragments.add(new BouncesFragment());
		fragments.add(new ContactsFragment());
		return fragments;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	private void highlight(int pos) {
		current = pos;
		if (pos == 0) {
			settingsHighlighter.setBackgroundColor(getResources().getColor(
					android.R.color.holo_blue_dark));
			settingsIcon.setSelected(true);
		} else {
			settingsHighlighter.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			settingsIcon.setSelected(false);
		}

		if (pos == 1) {
			bouncesHighlighter.setBackgroundColor(getResources().getColor(
					android.R.color.holo_blue_dark));
			bouncesIcon.setSelected(true);
		} else {
			bouncesHighlighter.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			bouncesIcon.setSelected(false);
		}

		if (pos == 2) {
			contactsHighlighter.setBackgroundColor(getResources().getColor(
					android.R.color.holo_blue_dark));
			contactsIcon.setSelected(true);
		} else {
			contactsHighlighter.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			contactsIcon.setSelected(false);
		}
		viewPager.setCurrentItem(pos);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		// Log.d(TAG, "onPageScrolled called");
		int pos = this.viewPager.getCurrentItem();
		if (current != pos)
			highlight(pos);
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub

	}
}
