package com.picktr.example.helpers;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class BounceCloudPageAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;
	
	private static final String[] titles = { "Personal", "Bounce", "Contacts"};

	public BounceCloudPageAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int pos) {
		// TODO Auto-generated method stub
		return fragments.get(pos);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fragments.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return titles[position]; 		
	}
}
