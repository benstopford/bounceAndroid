package com.example.bouncecloud;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.definitions.Consts;
import com.example.helpers.Bounce;
import com.example.helpers.DataHolder;
import com.example.helpers.Utils;

public class BounceitActivity extends Activity implements
		SurfaceHolder.Callback, Camera.PreviewCallback {

	private static final String TAG = "Bounceit Activity";
	private SurfaceView cameraPreviewView;
	private SurfaceHolder cameraPreviewHolder;
	private Camera camera;
	private HorizontalScrollView takenPicturesView;
	private LinearLayout takenPicturesLinearLayout;
	private LinearLayout bounceLinearView;
	private ImageButton flushButton;
	private Button takePictureButton;
	private Boolean inPreview = false;
	private Boolean isFlushOn = false;
	private Boolean isFrontCamera = true;
	private boolean cameraConfigured = false;
	private int optionNumber = 0;
	private EditText questionView;
	private ArrayList<EditText> optionTitleViews;

	private String question;
	private ArrayList<String> imageURIs;
	private ArrayList<Integer> types;
	private ArrayList<Integer> receivers;
	private ArrayList<String> optionTitles;
	private Camera.Size previewSize;
	private Camera.Parameters parameters;
	private byte[] lastShownImage;
	private Bounce bounce;
	private DataHolder dataHolder;

	private void setupCamera() {
		parameters = camera.getParameters();
		if (Integer.parseInt(Build.VERSION.SDK) >= 8)
			setDisplayOrientation(camera, 90);
		else {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				parameters.set("orientation", "portrait");
				parameters.set("rotation", 90);
			}
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				parameters.set("orientation", "landscape");
				parameters.set("rotation", 90);
			}
		}
	}

	private void setupPreviewView() {
		flushButton = (ImageButton) findViewById(R.id.change_flash_icon);
		cameraPreviewView = (SurfaceView) findViewById(R.id.camera_preview);
		Point size = Utils.getDisplaySize(this);
		int StatusBarHeight = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (StatusBarHeight > 0) {
			StatusBarHeight = getResources().getDimensionPixelSize(
					StatusBarHeight);
		}
		Log.d(TAG, "Status Bar height: " + StatusBarHeight);
		int width = size.x;
		int height = size.y - StatusBarHeight;

		bounceLinearView = (LinearLayout) findViewById(R.id.created_bounce_view_layout);
		RelativeLayout.LayoutParams bounceParams = (android.widget.RelativeLayout.LayoutParams) bounceLinearView
				.getLayoutParams();
		bounceParams.height = height - width;
		bounceParams.width = width;
		bounceLinearView.setLayoutParams(bounceParams);

		Log.d(TAG, " setting height to " + bounceParams.height
				+ " and width to: " + bounceParams.width);
		takenPicturesView = (HorizontalScrollView) findViewById(R.id.taken_pictures);
		takenPicturesLinearLayout = (LinearLayout) findViewById(R.id.taken_pictures_linear_layout);
		questionView = (EditText) findViewById(R.id.question);
		imageURIs = new ArrayList<String>();
		cameraPreviewHolder = cameraPreviewView.getHolder();
		isFrontCamera = true;
		flushButton.setVisibility(View.INVISIBLE);
		camera = getCameraInstance();
		cameraPreviewHolder.addCallback(this);
		cameraPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setupCamera();
		previewSize = Utils.getBestPreviewSize(width, width, parameters);
		RelativeLayout.LayoutParams previewParams = (android.widget.RelativeLayout.LayoutParams) cameraPreviewView
				.getLayoutParams();
		previewParams.height = (int) (width * 1.0 * (previewSize.width * 1.0 / previewSize.height));
		previewParams.width = width;
		getWindow()
				.setLayout(width, previewParams.height + bounceParams.height);
		takePictureButton = (Button) findViewById(R.id.take_picture_button);
		RelativeLayout.LayoutParams buttonParams = (android.widget.RelativeLayout.LayoutParams) takePictureButton
				.getLayoutParams();
		buttonParams.setMargins(0, 0, 0, (previewParams.height
				+ bounceParams.height - height - StatusBarHeight));
		takePictureButton.setLayoutParams(buttonParams);
		cameraPreviewView.setLayoutParams(previewParams);
		Log.d(TAG, " setting preview height to " + previewParams.height
				+ " and width to: " + previewParams.width);
	}

	private void setupBounceViews() {
		ArrayList<String> contents = new ArrayList<String>();

		if (bounce.getNumberOfOptions() != null)
			optionNumber = bounce.getNumberOfOptions();
		if (bounce.getReceivers() != null)
			receivers = bounce.getReceivers();
		if (bounce.getContents() != null)
			contents = bounce.getContents();
		if (bounce.getOptionNames() != null)
			optionTitles = bounce.getOptionNames();
		if (bounce.getQuestion() != null)
			question = bounce.getQuestion();

		for (int i = 0; i < optionNumber; i++) {
			addOption(contents.get(i), optionTitles.get(i));
		}

		questionView.setText(question);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bounceit_activity);
		dataHolder = DataHolder.getDataHolder(getApplicationContext());
		setupPreviewView();

		receivers = new ArrayList<Integer>();
		types = new ArrayList<Integer>();
		optionTitleViews = new ArrayList<EditText>();
		optionTitles = new ArrayList<String>();
		lastShownImage = null;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			long bounceID = extras.getLong("id");
			Log.d(TAG, "got id " + bounceID);
			bounce = DataHolder.getDataHolder(getApplicationContext())
					.getBounceWithInternalId(bounceID);
			setupBounceViews();
		} else {
			Log.e(TAG, "no ID is provided");
		}

		isFlushOn = false;

	}

	protected void setDisplayOrientation(Camera camera, int angle) {
		Method downPolymorphic;
		try {
			downPolymorphic = camera.getClass().getMethod(
					"setDisplayOrientation", new Class[] { int.class });
			if (downPolymorphic != null)
				downPolymorphic.invoke(camera, new Object[] { angle });
		} catch (Exception e1) {
		}
	}

	private void initPreview() {
		Log.d(TAG, "Init preview called");
		if (camera != null && cameraPreviewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(cameraPreviewHolder);
				camera.setPreviewCallback(this);
			} catch (Throwable t) {
				Log.e(TAG, "PREVIEW Display setup failed");
			}

			if (!cameraConfigured) {
				if (previewSize != null) {
					parameters.setPreviewSize(previewSize.width,
							previewSize.height);
					Log.d(TAG, "Setting camera parameters");
					if (isFlushOn && !isFrontCamera) {
						parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
					} else {
						parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
					}
					if (isFrontCamera) {
						parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
					}
					camera.setParameters(parameters);
					cameraConfigured = true;
				}
			} else {
				Log.d(TAG, "Camera is already configured");
			}
		} else {
			Log.e(TAG, "Init preview failed");
		}
	}

	private void startPreview() {
		Log.d(TAG, "Start preview called");
		if (cameraConfigured && camera != null) {
			camera.startPreview();
			inPreview = true;
		}
	}

	private void setupBounce() {

		question = questionView.getText().toString();

		optionTitles.clear();
		for (int i = 0; i < optionTitleViews.size(); i++) {
			optionTitles.add(optionTitleViews.get(i).getText().toString());
		}

		bounce.setSender(dataHolder.getSelf().getUserID());
		bounce.setNumberOfOptions(optionTitles.size());
		bounce.setQuestion(question);
		bounce.setOptionNames(optionTitles);
		bounce.setTypes(types);
		bounce.setSendAt(new Date(System.currentTimeMillis()));
		bounce.setReceivers(receivers);
		bounce.setContents(imageURIs);

		Log.d(TAG, "Contents: " + imageURIs + " optionTitles:" + optionTitles);

	}

	private void saveDraft() {
		Log.d(TAG, "onSaveDraft called");
		setupBounce();
		dataHolder.updateBounce(bounce);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		if (inPreview) {
			cameraConfigured = false;
			camera.stopPreview();
			camera.setPreviewCallback(null);
		}
		saveDraft();
		camera.release();
		camera = null;
		inPreview = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (camera == null) {
			Log.d(TAG, "Setting camera onResume");
			camera = getCameraInstance();
			setupCamera();
		}
		super.onResume();
	}

	@SuppressLint("NewApi")
	private Camera getCameraInstance() {
		Camera c = null;
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (isFrontCamera
					&& cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
				c = Camera.open(i);
				return c;
			}

			if (!isFrontCamera
					&& cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				c = Camera.open(i);
				return c;
			}
		}

		c = Camera.open();
		return c;
	}

	public void onSwapCameraClick(View v) {
		if (inPreview) {
			cameraConfigured = false;
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			isFrontCamera = !isFrontCamera;
			if (isFrontCamera)
				flushButton.setVisibility(View.INVISIBLE);
			else
				flushButton.setVisibility(View.VISIBLE);
			camera = getCameraInstance();
			setupCamera();
			initPreview();
			startPreview();
		}

	}

	public void onFlushClick(View v) {

		if (!inPreview)
			return;
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FLASH))
			return;

		cameraConfigured = false;
		camera.stopPreview();
		camera.setPreviewCallback(null);
		camera.release();
		isFlushOn = !isFlushOn;
		camera = getCameraInstance();
		setupCamera();
		initPreview();
		startPreview();
	}

	public void onTakePictureClick(View v) {
		if (lastShownImage != null)
			onPictureTaken(lastShownImage);
	}

	public void onBackButtonClick(View v) {
		super.onBackPressed();
	}

	public void onSendButtonClick(View v) {
		Intent i = new Intent(this, ContactPicker.class);
		startActivityForResult(i, 1);
	}

	private void sendToBackendAndFinish() {
		dataHolder.sendBounce(bounce);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			ArrayList<String> ids = data.getStringArrayListExtra("chosen_ids");
			receivers = Utils.castToIntArrayFromStringArray(ids);
			saveDraft();
			sendToBackendAndFinish();
		}
		if (resultCode == RESULT_CANCELED) {
			// Write your code if there's no result
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		initPreview();
		startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// Log.d(TAG, "start Preview called");
		// camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@SuppressLint("NewApi")
	private static Point getDisplaySize(final Display display) {
		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) { // Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		return point;
	}

	private void addOption(String picturePath, String textOption) {

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View optionView = inflater.inflate(
				R.layout.bounce_option_view_layout_small, null);

		EditText optionText = (EditText) optionView
				.findViewById(R.id.option_text);
		optionText.setText(textOption);

		ImageView optionImage = (ImageView) optionView
				.findViewById(R.id.option_image);

		Utils.displayImage(this, picturePath, optionImage);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		takenPicturesLinearLayout.addView(optionView, params);
		optionTitleViews.add(optionText);
		imageURIs.add(picturePath);
		types.add(Consts.CONTENT_TYPE_IMAGE);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				takenPicturesView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		}, 100L);

	}

	private Uri saveImage(byte[] pictureData) {
		int w = previewSize.width;
		int h = previewSize.height;
		int format = parameters.getPreviewFormat();
		YuvImage image = new YuvImage(pictureData, format, w, h, null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Rect area = new Rect(0, 0, w, h);
		image.compressToJpeg(area, 100, out);
		Bitmap bmp = BitmapFactory.decodeByteArray(out.toByteArray(), 0,
				out.size());
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Log.d(TAG, "image width: " + width + " image height: " + height);
		Matrix matrix = new Matrix();
		matrix.postRotate(270);
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height,
				matrix, false);
		Log.d(TAG, "rotated image width: " + resizedBitmap.getWidth()
				+ " image height: " + resizedBitmap.getHeight());
		resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0,
				Math.min(height, width), Math.min(height, width));
		resizedBitmap = Utils.getResizedBitmap(resizedBitmap, 500, 500);
		Uri uriTarget = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
				new ContentValues());
		OutputStream imageFileOS;
		try {
			imageFileOS = getContentResolver().openOutputStream(uriTarget);
			resizedBitmap
					.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS);
			// imageFileOS.write(pictureData);
			imageFileOS.flush();
			imageFileOS.close();
			Toast.makeText(BounceitActivity.this,
					"Image saved: " + uriTarget.getPath(), Toast.LENGTH_LONG)
					.show();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uriTarget;
	}

	public void onPictureTaken(byte[] pictureData) {
		optionNumber++;
		Log.d(TAG, "option number is " + optionNumber);

		if (optionNumber >= 6) {
			Toast.makeText(BounceitActivity.this,
					"Sorry, no more than 5 options! ", Toast.LENGTH_LONG)
					.show();
		} else {
			Uri uriTarget = saveImage(pictureData);
			addOption(Utils.getRealPathFromURI(this, uriTarget), "Option "
					+ optionNumber);
		}
		startPreview();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		lastShownImage = data;
	}

}
