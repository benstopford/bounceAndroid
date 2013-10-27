package com.example.bouncecloud;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bounceit_activity);
		cameraPreviewView = (SurfaceView) findViewById(R.id.camera_preview);
		takenPicturesView = (HorizontalScrollView) findViewById(R.id.taken_pictures);
		takenPicturesLinearLayout = (LinearLayout) findViewById(R.id.taken_pictures_linear_layout); 
		
		cameraPreviewHolder = cameraPreviewView.getHolder();
		camera = getCameraInstance();
		cameraPreviewHolder.addCallback(this);
		cameraPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
	}
	
	public void onTakePictureClick(View v)
	{
		 camera.takePicture(null, null, this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		initPreview(width, height);
		startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d(TAG, "start Preview called");
		camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPictureTaken(byte[] pictureData, Camera camera) {
		Bitmap bmp = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
        //create a canvas from the bitmap, then display it in surfaceview
		ImageView imageView = new ImageView(this); 
        imageView.setImageBitmap(bmp);
        takenPicturesLinearLayout.addView(imageView);
        startPreview();
	}
	
}
