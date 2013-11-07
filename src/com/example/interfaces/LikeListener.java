package com.example.interfaces;

import java.util.ArrayList;

import com.example.helpers.Like;

public interface LikeListener {
	public void onLikeArrived(Like like);
	public void onLikesArrived(ArrayList<Like> likes);
}
