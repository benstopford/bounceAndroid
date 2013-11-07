package com.example.bouncecloud;

import static com.example.definitions.Consts.CONTENT_TYPE_IMAGE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.helpers.DataHolder;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileUploadTaskResult;

@SuppressLint("NewApi")
public class BounceitActivity extends Activity implements
		SurfaceHolder.Callback, PictureCallback {

	String TAG = "Bounceit Activity";
	SurfaceView cameraPreviewView;
	SurfaceHolder cameraPreviewHolder;
	Camera camera;
	HorizontalScrollView takenPicturesView;

	LinearLayout takenPicturesLinearLayout;
	Boolean inPreview = false;
	private boolean cameraConfigured = false;
	Button takePicture;
	private int optionNumber = 0;

	private ArrayList<Uri> imageURIs;

	private int numberOfOptions;
	private ArrayList<String> contents;
	private ArrayList<Integer> types;
	private ArrayList<Integer> receivers;

	private Camera.Size getBestPreviewSize(int width, int height,
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

	private void initPreview(int width, int height) {
		if (camera != null && cameraPreviewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(cameraPreviewHolder);
			} catch (Throwable t) {
				Log.e(TAG, "PREVIEW Display setup failed");
			}

			if (!cameraConfigured) {

				Camera.Parameters parameters = camera.getParameters();
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

				Camera.Size size = getBestPreviewSize(width, height, parameters);

				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);
					camera.setParameters(parameters);
					cameraConfigured = true;
				}
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.startPreview();
			inPreview = true;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (inPreview) {
			camera.stopPreview();
		}

		camera.release();
		camera = null;
		inPreview = false;

		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (camera == null) {
			camera = getCameraInstance();
		}
		super.onResume();
	}

	@SuppressLint("NewApi")
	private Camera getCameraInstance() {
		Camera c = null;
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
				c = Camera.open(i);
				return c;
			}
		}

		c = Camera.open();
		return c;
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bounceit_activity);
		cameraPreviewView = (SurfaceView) findViewById(R.id.camera_preview);
		
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = getDisplaySize(display); 
		int width = size.x;
		int height = size.y;
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height/2);
		
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM); 
		
		cameraPreviewView.setLayoutParams(params); 
		
		takenPicturesView = (HorizontalScrollView) findViewById(R.id.taken_pictures);
		takenPicturesLinearLayout = (LinearLayout) findViewById(R.id.taken_pictures_linear_layout);
		imageURIs = new ArrayList<Uri>();
		takePicture = (Button) findViewById(R.id.take_picture_button);
		cameraPreviewHolder = cameraPreviewView.getHolder();
		camera = getCameraInstance();
		cameraPreviewHolder.addCallback(this);
		cameraPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		receivers = new ArrayList<Integer>();
		contents = new ArrayList<String>();
		types = new ArrayList<Integer>();

	}

	public void onTakePictureClick(View v) {
		camera.takePicture(null, null, this);
	}

	public void onSendButtonClick(View v) {
		Intent i = new Intent(this, ContactPicker.class);
		startActivityForResult(i, 1);
	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
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

	private void sendToBackend() {
		numberOfOptions = types.size();
		int selfId = DataHolder.getDataHolder().getSignInUserId().getId();
		DataHolder.getDataHolder().sendBounce(selfId, numberOfOptions, types,
				contents, receivers);
	}

	private void sendToBackendAndFinish() {

		if (imageURIs.size() == 0) {
			sendToBackend();
			finish();
		} else {
			Uri uri = imageURIs.get(0);
			imageURIs.remove(0);
			File file = new File(getRealPathFromURI(getApplicationContext(),
					uri));
			QBContent.uploadFileTask(file, true, new QBCallback() {

				@Override
				public void onComplete(Result arg0, Object arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onComplete(Result result) {
					// TODO Auto-generated method stub
					if (result.isSuccess()) {
						QBFileUploadTaskResult qbFileUploadTaskResultq = (QBFileUploadTaskResult) result;
						Log.d(TAG, qbFileUploadTaskResultq.toString());
						String publicURL = qbFileUploadTaskResultq.getFile()
								.getUid();
						contents.add(publicURL);
						types.add(CONTENT_TYPE_IMAGE);
						Log.d(TAG, publicURL);
						sendToBackendAndFinish();
					} else {

					}
				}
			});
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Integer k = data.getIntExtra("chosen_id", 0);
			Log.d(TAG, k.toString());
			receivers.add(data.getIntExtra("chosen_id", 0));
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
		initPreview(height, width);
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

	@Override
	public void onPictureTaken(byte[] pictureData, Camera camera) {

		optionNumber++;

		Log.d(TAG, "option number is " + optionNumber);

		if (optionNumber >= 6) {
			Toast.makeText(BounceitActivity.this,
					"Sorry, no more than 5 options! ", Toast.LENGTH_LONG)
					.show();
		} else {
			Bitmap bmp = BitmapFactory.decodeByteArray(pictureData, 0,
					pictureData.length);

			Config config = bmp.getConfig();
			Bitmap targetBitmap = Bitmap.createBitmap(bmp.getHeight(),
					bmp.getWidth(), config);
			Canvas canvas = new Canvas(targetBitmap);
			Matrix matrix = new Matrix();
			matrix.setRotate(270, bmp.getWidth() / 2, bmp.getHeight() / 2);
			canvas.drawBitmap(bmp, matrix, new Paint());
			bmp = targetBitmap;

			// create a canvas from the bitmap, then display it in surfaceview
			ImageView imageView = new ImageView(this);
			imageView.setImageBitmap(bmp);

			WindowManager wm = (WindowManager) this
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			
			Point size = getDisplaySize(display);
			int width = size.x;
			int height = size.y;

			imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(width / 2, height / 3));
			takenPicturesLinearLayout.addView(imageView);

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					takenPicturesView
							.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
				}
			}, 100L);

			Uri uriTarget = getContentResolver().insert(
					Media.EXTERNAL_CONTENT_URI, new ContentValues());

			OutputStream imageFileOS;
			try {
				imageFileOS = getContentResolver().openOutputStream(uriTarget);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS);
				// imageFileOS.write(pictureData);
				imageFileOS.flush();
				imageFileOS.close();
				Toast.makeText(BounceitActivity.this,
						"Image saved: " + uriTarget.getPath(),
						Toast.LENGTH_LONG).show();
				imageURIs.add(uriTarget);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		startPreview();
	}

}
