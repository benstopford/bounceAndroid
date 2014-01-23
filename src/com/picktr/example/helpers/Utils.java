package com.picktr.example.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.picktr.example.definitions.Consts;
import com.picktr.example.picktrbeta.BounceitActivity;
import com.picktr.example.picktrbeta.DisplayBounceFromSelf;
import com.picktr.example.picktrbeta.DisplayBounceToSelf;

public class Utils {

	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
	private static final String TAG = "Utils";

	public static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static Bitmap createRoundImage(Bitmap loadedImage) {
		Bitmap circleBitmap = Bitmap.createBitmap(loadedImage.getWidth(),
				loadedImage.getHeight(), Bitmap.Config.ARGB_8888);

		BitmapShader shader = new BitmapShader(loadedImage,
				Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(shader);

		Canvas c = new Canvas(circleBitmap);
		c.drawCircle(loadedImage.getWidth() / 2, loadedImage.getHeight() / 2,
				loadedImage.getWidth() / 2, paint);

		return circleBitmap;
	}

	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

	public static String getTimeAgo(Date date, Context ctx) {

		if (date == null)
			return "just now";

		long time = date.getTime();

		if (time < 1000000000000L) {
			// if timestamp given in seconds, convert to millis
			time *= 1000;
		}

		long now = System.currentTimeMillis();

		if (time > now || time <= 0) {
			return "just now";
		}

		// TODO: localize
		final long diff = now - time;
		if (diff < MINUTE_MILLIS) {
			return "just now";
		} else if (diff < 2 * MINUTE_MILLIS) {
			return "a minute ago";
		} else if (diff < 50 * MINUTE_MILLIS) {
			return diff / MINUTE_MILLIS + " minutes ago";
		} else if (diff < 90 * MINUTE_MILLIS) {
			return "an hour ago";
		} else if (diff < 24 * HOUR_MILLIS) {
			return diff / HOUR_MILLIS + " hours ago";
		} else if (diff < 48 * HOUR_MILLIS) {
			return "yesterday";
		} else {
			return diff / DAY_MILLIS + " days ago";
		}
	}

	public static String standartPhoneNumber(String contactNumber) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

		PhoneNumber phoneNumber = null;

		try {
			phoneNumber = phoneUtil.parse(contactNumber, Locale.getDefault()
					.getCountry());
		} catch (NumberParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (phoneNumber == null)
			return null;

		String number = phoneUtil.format(phoneNumber,
				PhoneNumberFormat.INTERNATIONAL);
		String ans = "";
		for (int i = 0; i < number.length(); i++)
			if (Character.isDigit(number.charAt(i))) {
				ans += number.charAt(i);
			}
		return ans;
	}

	public static String convertArrayOfStringToString(ArrayList<String> array) {
		if (array == null)
			return null;
		String str = "";
		for (int i = 0; i < array.size(); i++) {
			str = str + array.get(i);
			// Do not append comma at the end of last element
			if (i < array.size() - 1) {
				str = str + ",";
			}
		}
		return str;
	}

	public static String convertArrayOfIntsToString(ArrayList<Integer> array) {
		if (array == null)
			return null;
		String str = "";
		for (int i = 0; i < array.size(); i++) {
			str = str + array.get(i).toString();
			// Do not append comma at the end of last element
			if (i < array.size() - 1) {
				str = str + ",";
			}
		}
		return str;
	}

	public static ArrayList<String> convertStringToArrayOfString(String str) {
		if (str == null)
			return null;
		String[] arr = str.split(",");
		ArrayList<String> res = new ArrayList<String>(Arrays.asList(arr));
		return res;
	}

	public static ArrayList<Integer> convertStringToArrayOfInt(String str) {
		if (str == null)
			return null;
		return castToIntArrayFromStringArray(convertStringToArrayOfString(str));
	}

	public static ArrayList<Integer> castToIntArrayFromStringArray(
			ArrayList<String> arrayList) {
		if (arrayList == null)
			return null;
		ArrayList<Integer> array = new ArrayList<Integer>();

		for (int index = 0; index < arrayList.size(); index++) {
			if (arrayList.get(index) != "")
				array.add(Integer.parseInt(arrayList.get(index)));
		}

		return array;
	}

	@SuppressLint("NewApi")
	public static Point getDisplaySize(Context ctx) {

		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) { // Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		return point;
	}

	public static Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;
					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}
		return (result);
	}

	public static void displayImage(Context ctx, String imageURI,
			ImageView image) {
		if (imageURI == null)
			return;
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheInMemory().cacheOnDisc()
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.displayer(new RoundedBitmapDisplayer(2)).build();
		// Load and display image
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				ctx).defaultDisplayImageOptions(options).build();
		ImageLoader.getInstance().init(config);
		File file = new File(imageURI);
		ImageLoader.getInstance().displayImage(Uri.fromFile(file).toString(),
				image);
	}

	public static void startBounceActivity(Context context, Bounce bounce,
			int position) {

		if (bounce.getStatus().equals(Consts.BOUNCE_STATUS_LOADING)
				|| bounce.getStatus().equals(Consts.BOUNCE_STATUS_SENDING)) {
			return;
		}

		if (bounce.isDraft()) {
			Log.d(TAG, "putting extra to bounceIt " + bounce.getID());
			Intent intent = new Intent(context, BounceitActivity.class);
			intent.putExtra("id", bounce.getID());
			context.startActivity(intent);
		} else if (bounce.isFromSelf()) {
			Intent intent = new Intent(context, DisplayBounceFromSelf.class);
			intent.putExtra("bounce_id", bounce.getBounceId());
			intent.putExtra("option", position);
			context.startActivity(intent);
		} else {
			Intent intent = new Intent(context, DisplayBounceToSelf.class);
			intent.putExtra("bounce_id", bounce.getBounceId());
			intent.putExtra("option", position);
			context.startActivity(intent);
		}

	}

}
