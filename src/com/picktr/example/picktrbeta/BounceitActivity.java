package com.picktr.example.picktrbeta;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.picktr.example.definitions.Consts;
import com.picktr.example.helpers.Bounce;
import com.picktr.example.helpers.BounceOption;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.helpers.MyClipboardManager;
import com.picktr.example.helpers.Utils;

public class BounceitActivity extends Activity implements
		SurfaceHolder.Callback, Camera.PreviewCallback {

	private static final String TAG = "Bounceit Activity";
	private RelativeLayout cameraPreviewView;
	private SurfaceView cameraPreviewSurface;
	private SurfaceHolder cameraPreviewHolder;
	private Camera camera;
	private HorizontalScrollView takenPicturesView;
	private LinearLayout takenPicturesLinearLayout;
	private LinearLayout bounceLinearView;
	private LinearLayout bounceTopLinearView;
	private LinearLayout URLView;
	private RelativeLayout buttonsLayout;
	private WebViewClient webClient;
	private WebView webView;
	private EditText urlEditText;
	private ImageButton flushButton;
	private ImageButton takePictureButton;
	private Boolean inPreview = false;
	private Boolean isFlushOn = false;
	private Boolean isFrontCamera = false;
	private Boolean draftNotify = true;
	private ImageButton urlButton;
	private boolean cameraConfigured = false;
	private int optionNumber = 0;
	private int currentOption = 0;
	private EditText questionView;
	private ArrayList<EditText> optionTitleViews;
	private ArrayList<byte[]> optionImages;
	private ArrayList<String> optionURLs;
	private ArrayList<Integer> optionTypes;
	private String question;
	private ArrayList<Integer> receivers;
	private Camera.Size previewSize;
	private Camera.Parameters parameters;
	private byte[] lastShownImage;
	private Bounce bounce;
	private DataHolder dataHolder;
	private int imgHeight = 0;

	private ImageButton chooseUrlButton;
	private ImageButton choosePhotoButton;

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

	private void calculateSizes() {
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
		RelativeLayout.LayoutParams bounceParams = (android.widget.RelativeLayout.LayoutParams) bounceLinearView
				.getLayoutParams();
		bounceParams.height = height - width;
		bounceParams.width = width;
		bounceLinearView.setLayoutParams(bounceParams);

		// URL params
		RelativeLayout.LayoutParams URLparams = (RelativeLayout.LayoutParams) URLView
				.getLayoutParams();
		URLparams.height = width;
		URLparams.width = width;
		URLView.setLayoutParams(URLparams);

		// Buttons params
		RelativeLayout.LayoutParams buttonsParams = (RelativeLayout.LayoutParams) buttonsLayout
				.getLayoutParams();
		buttonsParams.height = width;
		buttonsParams.width = width;
		buttonsLayout.setLayoutParams(buttonsParams);

		// preview params
		previewSize = Utils.getBestPreviewSize(width, width, parameters);
		RelativeLayout.LayoutParams previewParams = (android.widget.RelativeLayout.LayoutParams) cameraPreviewView
				.getLayoutParams();
		previewParams.height = (int) (width * 1.0 * (previewSize.width * 1.0 / previewSize.height));
		previewParams.width = width;
		getWindow()
				.setLayout(width, previewParams.height + bounceParams.height);
		// RelativeLayout.LayoutParams buttonParams =
		// (android.widget.RelativeLayout.LayoutParams) takePictureButton
		// .getLayoutParams();
		// buttonParams.setMargins(0, 0, 0, (previewParams.height
		// + bounceParams.height - height - StatusBarHeight));
		// takePictureButton.setLayoutParams(buttonParams);
		cameraPreviewView.setLayoutParams(previewParams);

		imgHeight = bounceParams.height
				- bounceTopLinearView.getLayoutParams().height;
	}

	private void setupPreviewView() {
		webClient = new WebViewClient();

		URLView = (LinearLayout) findViewById(R.id.url_preview_banch);
		webView = (WebView) findViewById(R.id.url_webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(webClient);
		urlEditText = (EditText) findViewById(R.id.url_edittext);

		urlButton = (ImageButton) findViewById(R.id.url_enter);
		urlButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onUrlEnterClick();
			}
		});

		choosePhotoButton = (ImageButton) findViewById(R.id.photo_icon);
		chooseUrlButton = (ImageButton) findViewById(R.id.url_icon);

		cameraPreviewView = (RelativeLayout) findViewById(R.id.camera_preview_banch);
		buttonsLayout = (RelativeLayout) findViewById(R.id.buttons_layout);
		flushButton = (ImageButton) findViewById(R.id.change_flash_icon);
		cameraPreviewSurface = (SurfaceView) findViewById(R.id.camera_preview);

		bounceLinearView = (LinearLayout) findViewById(R.id.created_bounce_view_layout);
		bounceTopLinearView = (LinearLayout) findViewById(R.id.created_bounce_view_layout_top);
		takenPicturesView = (HorizontalScrollView) findViewById(R.id.taken_pictures);
		takenPicturesLinearLayout = (LinearLayout) findViewById(R.id.taken_pictures_linear_layout);
		questionView = (EditText) findViewById(R.id.question);

		cameraPreviewHolder = cameraPreviewSurface.getHolder();
		flushButton.setVisibility(View.VISIBLE);
		takePictureButton = (ImageButton) findViewById(R.id.take_picture_button);

		isFrontCamera = false;
		camera = getCameraInstance();
		cameraPreviewHolder.addCallback(this);
		cameraPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setupCamera();
		calculateSizes();
	}

	private void onUrlEnterClick() {
		String url = urlEditText.getText().toString();
		if (!url.startsWith("http://"))
			url = "http://" + url;
		webView.loadUrl(url);
	}

	private void setupBounceViews() {

		receivers = new ArrayList<Integer>();
		optionTitleViews = new ArrayList<EditText>();
		optionImages = new ArrayList<byte[]>();
		optionURLs = new ArrayList<String>();
		optionTypes = new ArrayList<Integer>();
		takenPicturesLinearLayout.removeAllViews();

		if (bounce.getNumberOfOptions() != null)
			optionNumber = bounce.getNumberOfOptions();
		if (bounce.getReceivers() != null)
			receivers = bounce.getReceivers();
		if (bounce.getQuestion() != null) {
			questionView.setText(bounce.getQuestion());
		}

		currentOption = 0;
		ArrayList<BounceOption> contents = new ArrayList<BounceOption>();
		if (bounce.getOptions() != null) {
			contents = bounce.getOptions();
			for (int i = 0; i < optionNumber; i++) {

				if (contents.get(i).getType() == Consts.CONTENT_TYPE_IMAGE) {
					addOptionImage(contents.get(i).getImage(), contents.get(i)
							.getTitle());
				} else {
					addOptionUrl(contents.get(i).getUrl(), contents.get(i)
							.getTitle());
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bounceit_activity);
		dataHolder = DataHolder.getDataHolder(getApplicationContext());
		setupPreviewView();

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

		onPhotoIconClick(new View(this));
		isFlushOn = false;
		draftNotify = true;

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
		bounce.setSender(dataHolder.getSelf().getUserID());
		bounce.setQuestion(question);
		bounce.setSendAt(new Date(System.currentTimeMillis()));
		bounce.setReceivers(receivers);

		bounce.setNumberOfOptions(optionTitleViews.size());
		bounce.deleteAllOptions();

		for (int i = 0; i < optionTitleViews.size(); i++) {
			BounceOption option = new BounceOption();
			option.setBounceID(bounce.getID());
			option.setImage(optionImages.get(i));
			option.setTitle(optionTitleViews.get(i).getText().toString());
			option.setUrl(optionURLs.get(i));
			option.setOptionNumber(i);
			option.setType(optionTypes.get(i));
			bounce.addOption(option);
		}

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

		if (draftNotify) {
			Toast.makeText(this, "Draft successfully saved..",
					Toast.LENGTH_SHORT).show();
		}

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

	public void onPhotoIconClick(View v) {
		Log.d(TAG, "onPhotoIcon");
		cameraPreviewView.setVisibility(View.VISIBLE);
		cameraPreviewView.bringToFront();
		URLView.setVisibility(View.INVISIBLE);
		buttonsLayout.bringToFront();
		chooseUrlButton.setSelected(false);
		choosePhotoButton.setSelected(true);
	}

	public void onURLClick(View v) {
		Log.d(TAG, "onUrlClick");
		URLView.setVisibility(View.VISIBLE);
		URLView.bringToFront();
		cameraPreviewView.setVisibility(View.INVISIBLE);
		buttonsLayout.bringToFront();
		chooseUrlButton.setSelected(true);
		choosePhotoButton.setSelected(false);
	}

	public void onPasteFromClipboardClick(View v) {
		MyClipboardManager mng = new MyClipboardManager();
		String text = mng.readFromClipboard(getApplicationContext());
		urlEditText.setText(text);
	}

	public void onURLEnterClick(View v) {
		Log.d(TAG, "onUrlEnterClick");
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

	private void onAddUrl() {
		String url = webView.getUrl();
		String title = webView.getTitle();
		addOptionUrl(url, title);
	}

	public void onTakePictureClick(View v) {

		optionNumber++;
		Log.d(TAG, "option number is " + optionNumber);

		if (optionNumber >= 6) {
			Toast.makeText(BounceitActivity.this,
					"Sorry, no more than 5 options! ", Toast.LENGTH_LONG)
					.show();
			return;
		}

		if (URLView.getVisibility() == View.VISIBLE) {
			onAddUrl();
		} else {
			if (lastShownImage != null) {
				onPictureTaken(lastShownImage);
			}
		}
	}

	public void onBackButtonClick(View v) {
		super.onBackPressed();
	}

	public void onSendButtonClick(View v) {
		Intent i = new Intent(this, ContactPicker.class);
		draftNotify = false;
		startActivityForResult(i, 1);
	}

	private void sendToBackendAndFinish() {
		((PicktrApplication) getApplication()).networkService
				.putBounceToSend(bounce);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			ArrayList<String> ids = data.getStringArrayListExtra("chosen_ids");
			receivers = Utils.castToIntArrayFromStringArray(ids);
			draftNotify = false;
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

	private void deleteOptionPressed(int optionNumber) {
		Log.d(TAG, "on delete Option pressed for optionNumber " + optionNumber);
		setupBounce();
		bounce.deleteOption(optionNumber);
		setupBounceViews();
	}

	private void addOptionUrl(String url, String textTitle) {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View optionView = inflater.inflate(
				R.layout.bounce_option_view_layout_url, null);
		EditText optionText = (EditText) optionView
				.findViewById(R.id.option_text);
		optionText.setText(textTitle);

		WebView optionWebview = (WebView) optionView
				.findViewById(R.id.option_webview);
		Utils.setupWebView(optionWebview);
		optionWebview.setWebViewClient(new WebViewClient());
		optionWebview.loadUrl(url);

		ImageButton deleteButton = (ImageButton) optionView
				.findViewById(R.id.option_delete_button);
		deleteButton.setTag(currentOption);
		currentOption++;
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int optionNumber = (Integer) v.getTag();
				deleteOptionPressed(optionNumber);
			}
		});

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				imgHeight, imgHeight);
		optionView.setLayoutParams(params);
		takenPicturesLinearLayout.addView(optionView);
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				takenPicturesView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		}, 100L);

		optionTitleViews.add(optionText);
		optionImages.add(null);
		optionURLs.add(url);
		optionTypes.add(Consts.CONTENT_TYPE_URL);
	}

	private void addOptionImage(byte[] imageData, String textOption) {

		Log.d(TAG, "image data is " + imageData);

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View optionView = inflater.inflate(
				R.layout.bounce_option_view_layout_image, null);
		EditText optionText = (EditText) optionView
				.findViewById(R.id.option_text);
		optionText.setText(textOption);

		ImageView optionImage = (ImageView) optionView
				.findViewById(R.id.option_image);
		Utils.displayImage(imageData, optionImage);

		ImageButton deleteButton = (ImageButton) optionView
				.findViewById(R.id.option_delete_button);
		deleteButton.setTag(currentOption);
		currentOption++;
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int optionNumber = (Integer) v.getTag();
				deleteOptionPressed(optionNumber);
			}
		});

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				imgHeight, imgHeight);
		optionView.setLayoutParams(params);
		takenPicturesLinearLayout.addView(optionView);
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				takenPicturesView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		}, 100L);

		optionTitleViews.add(optionText);
		optionImages.add(imageData);
		optionURLs.add(null);
		optionTypes.add(Consts.CONTENT_TYPE_IMAGE);
	}

	private byte[] saveImage(byte[] pictureData) {
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
		if (isFrontCamera) {
			matrix.postRotate(270);
		} else {
			matrix.postRotate(90);
		}
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height,
				matrix, false);
		Log.d(TAG, "rotated image width: " + resizedBitmap.getWidth()
				+ " image height: " + resizedBitmap.getHeight());
		resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0,
				Math.min(height, width), Math.min(height, width));
		resizedBitmap = Utils.getResizedBitmap(resizedBitmap, 500, 500);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public void onPictureTaken(byte[] pictureData) {
		byte[] imageData = saveImage(pictureData);
		addOptionImage(imageData, optionNumber + ")");
		startPreview();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		lastShownImage = data;
	}

}
