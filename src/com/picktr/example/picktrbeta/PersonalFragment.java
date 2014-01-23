package com.picktr.example.picktrbeta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.picktr.example.definitions.Consts;
import com.picktr.example.helpers.Bounce;
import com.picktr.example.helpers.Contact;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.helpers.Utils;
import com.picktr.example.interfaces.PersonalUpdatedListener;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.content.result.QBFileUploadTaskResult;

public class PersonalFragment extends Fragment implements
		PersonalUpdatedListener {

	private static final String TAG = "PersonalFragment";

	private TextView myPhone;
	private EditText myName;
	private Button saveButton;
	private Contact user;
	private DataHolder dataHolder;
	private ImageView profileImage;
	private ImageView editProfileIcon;
	private Uri outputFileUri;
	private Boolean changedProfileImage;
	Button bounceItButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.personal_fragment, container, false);

		dataHolder = DataHolder.getDataHolder(getActivity()
				.getApplicationContext());
		user = dataHolder.getSelf();

		myPhone = (TextView) rootView.findViewById(R.id.my_phone);
		myPhone.setText(user.getDisplayPhoneNumber());
		myName = (EditText) rootView.findViewById(R.id.my_name);
		myName.setText(user.getName());
		saveButton = (Button) rootView.findViewById(R.id.save_button);

		profileImage = (ImageView) rootView.findViewById(R.id.profile_image);
		editProfileIcon = (ImageView) rootView
				.findViewById(R.id.edit_profile_icon);

		LayoutParams imageParams = profileImage.getLayoutParams();
		imageParams.width = Utils.getDisplaySize(getActivity()).x;
		imageParams.height = Utils.getDisplaySize(getActivity()).x;
		profileImage.setLayoutParams(imageParams);

		if (user.getProfileImage() != null) {
			Bitmap bmp = BitmapFactory.decodeByteArray(user.getProfileImage(),
					0, user.getProfileImage().length);
			profileImage.setImageBitmap(bmp);
		}

		editProfileIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onChangeProfileImageClick();
			}
		});

		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSaveButtonClick();
			}
		});

		changedProfileImage = false;
		dataHolder.registerPersonalListener(this);

		bounceItButton = (Button) rootView.findViewById(R.id.bounceit_button);
		bounceItButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBounceitClick(v);
			}
		});

		return rootView;
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		dataHolder.deregisterPersonalListener(this);
		super.onDestroyView();
	}

	private void startCroppingActivity(Uri picUri) {

		try {

			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(picUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			getActivity().startActivityForResult(cropIntent, Consts.PIC_CROP);
		}
		// respond to users whose devices do not support the crop action
		catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast toast = Toast.makeText(getActivity(), errorMessage,
					Toast.LENGTH_SHORT);
			toast.show();
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivity Result called");
		if (resultCode == Activity.RESULT_OK) {
			Log.d(TAG, "onActivity Result is OK requestCode is " + requestCode);

			if (requestCode == Consts.PIC_CROP) {
				if (data != null) {
					// get the returned data
					Bundle extras = data.getExtras();
					// get the cropped bitmap
					Bitmap selectedBitmap = extras.getParcelable("data");
					if (selectedBitmap != null) {
						profileImage.setImageBitmap(selectedBitmap);
						changedProfileImage = true;
					} else {
						Log.e(TAG, "onActivityResult data is NULL");
					}
				}
			}

			if (requestCode == Consts.YOUR_SELECT_PICTURE_REQUEST_CODE) {
				Log.d(TAG, "onActivity RequestCode is OK");
				final boolean isCamera;
				if (data == null) {
					isCamera = true;
				} else {
					final String action = data.getAction();
					if (action == null) {
						isCamera = false;
					} else {
						isCamera = action
								.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					}
				}
				Uri selectedImageUri;
				if (isCamera) {
					selectedImageUri = outputFileUri;
				} else {
					selectedImageUri = data == null ? null : data.getData();
				}
				if (selectedImageUri != null) {
					Log.d(TAG, "bitmap created:" + selectedImageUri.toString());
					startCroppingActivity(selectedImageUri);
				} else {
					Log.d(TAG, "on activity result URI is null");
				}
			}
		}
	}

	protected void onChangeProfileImageClick() {
		// Determine Uri of camera image to save.
		final File root = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "MyDir" + File.separator);
		root.mkdirs();
		final String fname = "img_" + System.currentTimeMillis() + ".jpg";
		final File sdImageMainDirectory = new File(root, fname);
		outputFileUri = Uri.fromFile(sdImageMainDirectory);

		final List<Intent> cameraIntents = new ArrayList<Intent>();
		final Intent captureIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		final PackageManager packageManager = getActivity().getPackageManager();
		final List<ResolveInfo> listCam = packageManager.queryIntentActivities(
				captureIntent, 0);
		for (ResolveInfo res : listCam) {
			final String packageName = res.activityInfo.packageName;
			final Intent intent = new Intent(captureIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName,
					res.activityInfo.name));
			intent.setPackage(packageName);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			cameraIntents.add(intent);
		}

		// Filesystem.
		final Intent galleryIntent = new Intent();
		galleryIntent.setType("image/*");
		galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
		// Chooser of filesystem options.
		final Intent chooserIntent = Intent.createChooser(galleryIntent,
				"Select Source");
		// Add the camera options.
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
				cameraIntents.toArray(new Parcelable[] {}));

		getActivity().startActivityForResult(chooserIntent,
				Consts.YOUR_SELECT_PICTURE_REQUEST_CODE);
	}

	private void onSaveButtonClick() {
		Log.d(TAG, "onSaveButton clicked!");
		user.setName(myName.getText().toString());

		if (changedProfileImage) {
			Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable())
					.getBitmap();

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] byteArray = stream.toByteArray();

			File root = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "MyDir" + File.separator);
			root.mkdirs();
			String fname = "img_" + System.currentTimeMillis() + ".jpg";
			File sdImageMainDirectory = new File(root, fname);

			try {
				FileOutputStream fos = new FileOutputStream(
						sdImageMainDirectory.getPath());
				fos.write(byteArray);
				fos.close();
			} catch (java.io.IOException e) {
				Log.e("PictureDemo", "Exception in photoCallback", e);
			}

			user.setProfileImage(byteArray);

			QBContent.uploadFileTask(sdImageMainDirectory, false,
					new QBCallback() {

						@Override
						public void onComplete(Result arg0, Object arg1) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onComplete(Result result) {
							// TODO Auto-generated method stub
							// get uploaded file
							QBFileUploadTaskResult fileUploadTaskResult = (QBFileUploadTaskResult) result;
							QBFile qbFile = fileUploadTaskResult.getFile();
							int uploadedFileID = qbFile.getId();
							// Connect image to user
							user.setBlobID(uploadedFileID);
							dataHolder.updateSelf(user);
						}
					});
		} else {
			dataHolder.updateSelf(user);
		}
	}

	private void onBounceitClick(View v) {
		Bounce bounce = new Bounce();
		bounce.setSender(dataHolder.getSelf().getUserID());
		bounce.setSendAt(new Date(System.currentTimeMillis()));
		bounce.setStatus(Consts.BOUNCE_STATUS_DRAFT);
		bounce.setIsFromSelf(Consts.FROM_SELF);
		bounce.setID(dataHolder.addDraftBounce(bounce));
		Intent intent = new Intent(getActivity(), BounceitActivity.class);
		intent.putExtra("id", bounce.getID());
		startActivity(intent);
	}

	@Override
	public void onPersonalDetailsSaved() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(),
						"Personal details successfully saved..",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

}
