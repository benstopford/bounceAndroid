package com.picktr.example.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.picktr.example.definitions.Consts;
import com.picktr.example.interfaces.BouncesListListener;
import com.picktr.example.interfaces.ContactListListener;
import com.picktr.example.interfaces.LikeListener;
import com.picktr.example.interfaces.NewsArrivedListener;
import com.picktr.example.interfaces.PersonalUpdatedListener;
import com.picktr.example.interfaces.SessionCreatedListener;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.model.QBPermissions;
import com.quickblox.module.custom.model.QBPermissionsLevel;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.module.users.result.QBUserResult;

public class DataHolder {
	private static final String TAG = "DataHolder";
	private static final String PREFS_NAME = "BouncePreferences";
	private static DataHolder dataHolder;
	private static DatabaseHandler databaseHandler;
	private static Context context;
	private static ArrayList<NewsArrivedListener> newsListeners;
	private static ArrayList<ContactListListener> contactListListeners;
	private static ArrayList<BouncesListListener> bouncesListListeners;
	private static ArrayList<LikeListener> likeListeners;
	private static ArrayList<SessionCreatedListener> sessionCreatedListeners;
	private static ArrayList<PersonalUpdatedListener> personalListeners;
	private static SharedPreferences sharedPreferences;
	private static boolean sessionState = false;

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public void setRegistered(boolean registered) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("registered", registered);
		editor.commit();
	}

	public boolean isRegistered() {
		boolean registered = sharedPreferences.getBoolean("registered", false);
		return registered;
	}

	public void registerContactListListener(ContactListListener listener) {
		if (contactListListeners == null)
			contactListListeners = new ArrayList<ContactListListener>();
		contactListListeners.add(listener);
	}

	public void deregisterContactListListener(ContactListListener listener) {
		if (contactListListeners == null)
			contactListListeners = new ArrayList<ContactListListener>();
		contactListListeners.remove(listener);
	}

	public void registerPersonalListener(PersonalUpdatedListener listener) {
		if (personalListeners == null)
			personalListeners = new ArrayList<PersonalUpdatedListener>();
		personalListeners.add(listener);
	}

	public void deregisterPersonalListener(PersonalUpdatedListener listener) {
		if (personalListeners == null)
			personalListeners = new ArrayList<PersonalUpdatedListener>();
		personalListeners.remove(listener);
	}

	public void registerNewsListener(NewsArrivedListener listener) {
		if (newsListeners == null)
			newsListeners = new ArrayList<NewsArrivedListener>();
		newsListeners.add(listener);
	}

	public void deregisterNewsListener(NewsArrivedListener listener) {
		if (newsListeners == null)
			newsListeners = new ArrayList<NewsArrivedListener>();
		newsListeners.remove(listener);
	}

	public void registerSessionCreatedListener(SessionCreatedListener listener) {
		if (sessionCreatedListeners == null) {
			sessionCreatedListeners = new ArrayList<SessionCreatedListener>();
		}
		sessionCreatedListeners.add(listener);
	}

	public void deregisterSessionCreatedListener(SessionCreatedListener listener) {
		if (sessionCreatedListeners == null) {
			sessionCreatedListeners = new ArrayList<SessionCreatedListener>();
		}
		sessionCreatedListeners.remove(listener);
	}

	public void registerBouncesListListener(BouncesListListener listener) {
		if (bouncesListListeners == null)
			bouncesListListeners = new ArrayList<BouncesListListener>();
		bouncesListListeners.add(listener);
	}

	public void deregisterBouncesListListener(BouncesListListener listener) {
		if (bouncesListListeners == null)
			bouncesListListeners = new ArrayList<BouncesListListener>();
		bouncesListListeners.remove(listener);
	}

	public void registerLikeListener(LikeListener listener) {
		if (likeListeners == null) {
			likeListeners = new ArrayList<LikeListener>();
		}
		likeListeners.add(listener);
	}

	public void deregisterLikeListener(LikeListener listener) {
		if (likeListeners == null) {
			likeListeners = new ArrayList<LikeListener>();
		}
		likeListeners.remove(listener);
	}

	public static synchronized DataHolder getDataHolder(Context ctx) {
		if (dataHolder == null) {
			dataHolder = new DataHolder();
		}
		if (contactListListeners == null)
			contactListListeners = new ArrayList<ContactListListener>();

		if (databaseHandler == null) {
			databaseHandler = new DatabaseHandler(ctx);
		}
		if (sharedPreferences == null) {
			sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, 0);
		}

		context = ctx;
		return dataHolder;
	}

	private void notifyLikeListener() {
		if (likeListeners == null)
			return;
		for (int i = 0; i < likeListeners.size(); i++) {
			likeListeners.get(i).onLikesChanged();
		}
	}

	private void notifyPersonalListener() {
		if (personalListeners == null)
			return;
		for (int i = 0; i < personalListeners.size(); i++) {
			personalListeners.get(i).onPersonalDetailsSaved();
		}
	}

	public ArrayList<Bounce> getBounces() {
		return databaseHandler.getAllBounces();
	}

	public ArrayList<Contact> getContacts() {
		return databaseHandler.getAllContacts();
	}

	private void loadBounceContent(String bounce_id) {
		Bounce bounce = getBounceWithId(bounce_id);
		if (bounce == null)
			return;

		ArrayList<String> newContents = new ArrayList<String>();

		for (int position = 0; position < bounce.getNumberOfOptions(); position++) {
			try {
				URL url = new URL("http://qbprod.s3.amazonaws.com/"
						+ bounce.getContentAt(position));

				URLConnection connection = url.openConnection();
				connection.connect();
				// this will be useful so that you can show a typical 0-100%
				// progress bar
				int fileLength = connection.getContentLength();
				// download the file
				InputStream input = new BufferedInputStream(url.openStream());

				String fullPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath() + "/BounceCloud-Images/";
				File dir = new File(fullPath);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				File file = new File(fullPath, bounce.getContentAt(position));
				if (file.exists())
					file.delete();
				file.createNewFile();
				OutputStream output = new FileOutputStream(file);

				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					output.write(data, 0, count);
				}
				output.flush();
				output.close();
				input.close();
				newContents.add(fullPath + file.getName().toString());
				// Log.d(TAG,
				// "Saved file of length " + total + " to "
				// + output.toString() + " with Uri equal to "
				// + fullPath + file.getName().toString());
			} catch (Exception e) {
				e.printStackTrace();
				newContents.add(null);
			}
		}
		bounce.setContents(newContents);
		bounce.setStatus(Consts.BOUNCE_STATUS_RECEIVED);
		// Log.d(TAG, "bounce status :" + bounce.getStatus());
		updateBounce(bounce);
		notifyBouncesChanged();
	}

	public void addSeenFromCustomObject(QBCustomObject qbCustomObject) {
		HashMap<String, Object> fields = qbCustomObject.getFields();
		Integer owner = Integer.parseInt((String) fields
				.get(Consts.SEEN_OWNER_FIELD_NAME));

		String bounce_id = (String) fields
				.get(Consts.SEEN_BOUNCE_ID_FIELD_NAME);

		Integer contact_id = Integer.parseInt((String) fields
				.get(Consts.SEEN_CONTACTS_FIELD_NAME));

		Seen seen = new Seen(bounce_id, contact_id);
		databaseHandler.addSeen(seen);
		notifyBouncesChanged();
	}

	public ArrayList<Seen> getAllSeenBy(String bounce_id) {
		return databaseHandler.getAllSeenByForBounce(bounce_id);
	}

	public void addBounceFromCustomObject(QBCustomObject qbCustomObject) {
		Log.d(TAG, "AddBounceFromCustomObject called");
		HashMap<String, Object> fields = qbCustomObject.getFields();

		int numberOfOptions = Integer.parseInt(fields.get(
				Consts.NUMBER_OF_OPTIONS_FIELD_NAME).toString());

		ArrayList<String> contents = (ArrayList<String>) fields
				.get(Consts.CONTENT_FIELD_NAME);

		ArrayList<Integer> types = Utils
				.castToIntArrayFromStringArray((ArrayList<String>) fields
						.get(Consts.TYPE_FIELD_NAME));

		ArrayList<Integer> receivers = Utils
				.castToIntArrayFromStringArray((ArrayList<String>) fields
						.get(Consts.RECEIVERS_FIELD_NAME));

		String question = (String) fields.get(Consts.QUESTION_FIELD_NAME);

		ArrayList<String> optionTitles = (ArrayList<String>) fields
				.get(Consts.OPTION_TITLE_FIELD_NAME);

		int senderId = Integer.parseInt(fields.get(Consts.OWNER_FIELD_NAME)
				.toString());

		int isFromSelf = 0;
		Log.d(TAG, "onAdding a bounce : owner is " + senderId + " and self is "
				+ getSelf().getUserID());
		if (senderId == getSelf().getUserID().intValue()) {
			isFromSelf = 1;
		} else {
			Contact contact = getContactWithUserId(senderId);
			if (contact == null) {
				addContactById(senderId);
			}
		}

		Bounce bounce = new Bounce(senderId, numberOfOptions, types, contents,
				receivers, qbCustomObject.getCustomObjectId(), isFromSelf,
				question, optionTitles, qbCustomObject.getCreatedAt(),
				Consts.BOUNCE_STATUS_LOADING, 0);

		Log.d(TAG, bounce.toString());

		final String bounce_id = bounce.getBounceId();

		databaseHandler.addBounce(bounce);
		notifyBouncesChanged();

		new AsyncTask() {

			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				loadBounceContent(bounce_id);
				return null;
			}

		}.execute(null, null, null);

	}

	private void addContactById(int senderId) {
		QBUsers.getUser(senderId, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					QBUserResult qbUserResult = (QBUserResult) result;
					addContact(qbUserResult.getUser());
					// Log.d("User", qbUserResult.getUser().toString());
				} else {
					// Log.e("Errors", result.getErrors().toString());
				}
			}
		});
	}

	public void addLikeFromCustomObject(QBCustomObject qbCustomObject) {
		HashMap<String, Object> fields = qbCustomObject.getFields();

		int option = Integer.parseInt(fields.get(Consts.LIKE_OPTION_FIELD_NAME)
				.toString());
		int sender_id = Integer.parseInt(fields.get(
				Consts.LIKE_SENDER_FIELD_NAME).toString());
		String bounce_id = (String) fields.get(Consts.LIKE_BOUNCEID_FIELD_NAME);
		String type = (String) fields.get(Consts.LIKE_TYPE_FIELD_NAME);

		Like like = new Like(bounce_id, sender_id, option);

		if (type.equals(Consts.LIKE_TYPE_LIKE)) {
			databaseHandler.addLike(like);
		} else {
			databaseHandler.removeLike(like);
		}
		notifyLikeListener();
	}

	public void sendNews(HashMap<String, Object> fields) {
		QBCustomObject qbCustomObject = new QBCustomObject(
				Consts.NEWS_CLASS_NAME);
		qbCustomObject.setFields(fields);

		QBPermissions permissions = new QBPermissions();
		permissions.setDeletePermission(QBPermissionsLevel.OPEN);
		qbCustomObject.setPermission(permissions);

		QBCustomObjects.createObject(qbCustomObject, new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Result result) {
				// TODO Auto-generated method stub
				if (result.isSuccess()) {
					// Log.d(TAG, "New NEWS RECORD IS CREATED ON THE BACKEND!");
				} else {
					// Log.e(TAG, "Failed to create new NEWS record");
				}
			}
		});

	}

	public void createLikeNewsForBackend(QBCustomObject likeObject) {

		HashMap<String, Object> likeObjectFields = likeObject.getFields();

		int bounce_owner = Integer.parseInt((String) likeObjectFields
				.get(Consts.LIKE_BOUNCE_OWNER_FIELD_NAME));

		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(Consts.NEWS_TYPE_FIELD_NAME, Consts.NEWS_TYPE_LIKE);
		fields.put(Consts.NEWS_OWNER_FIELD_NAME, bounce_owner);
		fields.put(Consts.NEWS_OBJECT_ID_FIELD_NAME,
				likeObject.getCustomObjectId());

		sendNews(fields);
	}

	public void createSeenNewsForBackend(QBCustomObject seenObject) {
		HashMap<String, Object> seenObjectFields = seenObject.getFields();
		int owner = Integer.parseInt((String) seenObjectFields
				.get(Consts.SEEN_OWNER_FIELD_NAME));

		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(Consts.NEWS_TYPE_FIELD_NAME, Consts.NEWS_TYPE_SEEN_BY);
		fields.put(Consts.NEWS_OWNER_FIELD_NAME, owner);
		fields.put(Consts.NEWS_OBJECT_ID_FIELD_NAME,
				seenObject.getCustomObjectId());
		sendNews(fields);
	}

	public void createNewsForBackend(QBCustomObject bounceObject) {
		HashMap<String, Object> bounce_fields = bounceObject.getFields();
		ArrayList<Integer> receivers = Utils
				.castToIntArrayFromStringArray((ArrayList<String>) bounce_fields
						.get(Consts.RECEIVERS_FIELD_NAME));

		for (int i = 0; i < receivers.size(); i++) {
			HashMap<String, Object> fields = new HashMap<String, Object>();
			fields.put(Consts.NEWS_TYPE_FIELD_NAME, Consts.NEWS_TYPE_BOUNCE);
			fields.put(Consts.NEWS_OWNER_FIELD_NAME, receivers.get(i));
			fields.put(Consts.NEWS_OBJECT_ID_FIELD_NAME,
					bounceObject.getCustomObjectId());
			sendNews(fields);
		}

	}

	public void sendBouncePushNotification(QBCustomObject qbCustomObject) {

		HashMap<String, Object> fields = qbCustomObject.getFields();
		ArrayList<Integer> receivers = Utils
				.castToIntArrayFromStringArray((ArrayList<String>) fields
						.get(Consts.RECEIVERS_FIELD_NAME));
		// Log.d(TAG, qbCustomObject.toString());
		// Log.d(TAG, qbCustomObject.getCustomObjectId());

		StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
		userIds.addAll(receivers);

		QBEvent event = new QBEvent();
		event.setUserIds(userIds);
		event.setEnvironment(QBEnvironment.DEVELOPMENT);
		event.setNotificationType(QBNotificationType.PUSH);

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("type", Consts.MESSAGE_TYPE_BOUNCE);
			jsonObject.put("bounce_id", qbCustomObject.getCustomObjectId()
					.toString());
			jsonObject.put("sender_login", getSelf().getLogin());
			jsonObject.put("message", getSelf().getName()
					+ ": I have a bounce for you");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		event.setMessage(jsonObject.toString());

		QBMessages.createEvent(event, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					Log.d(TAG, "BouncePushMessage sent!");
				} else {
					Log.e(TAG, result.getErrors().toString());
				}
			}
		});

	}

	public void sendSeenPushNotification(QBCustomObject qbCustomObject) {
		HashMap<String, Object> fields = qbCustomObject.getFields();
		int owner = Integer.parseInt((String) fields
				.get(Consts.SEEN_OWNER_FIELD_NAME));

		StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
		userIds.add(owner);

		QBEvent event = new QBEvent();
		event.setUserIds(userIds);
		event.setEnvironment(QBEnvironment.DEVELOPMENT);
		event.setNotificationType(QBNotificationType.PUSH);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", Consts.MESSAGE_TYPE_LIKE);
			jsonObject.put("sender_login", getSelf().getLogin());
			jsonObject.put("message", getSelf().getLogin()
					+ ": I have seen your bounce");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		event.setMessage(jsonObject.toString());
		QBMessages.createEvent(event, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					Log.d(TAG, "Seen PushMessage sent!");
				} else {
					Log.e(TAG, result.getErrors().toString());
				}
			}
		});

	}

	public void sendLikePushNotification(QBCustomObject qbCustomObject) {
		HashMap<String, Object> fields = qbCustomObject.getFields();
		String bounce_id = (String) fields.get(Consts.LIKE_BOUNCEID_FIELD_NAME);
		int option = Integer.parseInt((String) fields
				.get(Consts.LIKE_OPTION_FIELD_NAME));
		int bounce_owner = Integer.parseInt((String) fields
				.get(Consts.LIKE_BOUNCE_OWNER_FIELD_NAME));

		StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
		userIds.add(bounce_owner);

		QBEvent event = new QBEvent();
		event.setUserIds(userIds);
		event.setEnvironment(QBEnvironment.DEVELOPMENT);
		event.setNotificationType(QBNotificationType.PUSH);

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("type", Consts.MESSAGE_TYPE_LIKE);
			jsonObject.put("message", getSelf().getDisplayName() + ": I "
					+ (String) fields.get(Consts.LIKE_TYPE_FIELD_NAME)
					+ " one of your images");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		event.setMessage(jsonObject.toString());

		QBMessages.createEvent(event, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					Log.d(TAG, "Like PushMessage sent!");
				} else {
					Log.e(TAG, result.getErrors().toString());
				}
			}
		});

	}

	public void sendBounce(Bounce bounce) {

		ArrayList<String> imageURIs = new ArrayList<String>();
		for (int i = 0; i < bounce.getNumberOfOptions(); i++) {
			imageURIs.add(bounce.getContentAt(i));
		}
		bounce.setStatus(Consts.BOUNCE_STATUS_SENDING);
		updateBounce(bounce);
		ArrayList<String> contents = new ArrayList<String>();
		sendBounce(imageURIs, bounce, contents);
	}

	public void finilizeSendingBounce(final Bounce bounce,
			ArrayList<String> contents) {

		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(Consts.OWNER_FIELD_NAME, bounce.getSender());
		fields.put(Consts.TYPE_FIELD_NAME, bounce.getTypes());
		fields.put(Consts.CONTENT_FIELD_NAME, contents);
		fields.put(Consts.NUMBER_OF_OPTIONS_FIELD_NAME,
				bounce.getNumberOfOptions());
		fields.put(Consts.RECEIVERS_FIELD_NAME, bounce.getReceivers());
		fields.put(Consts.QUESTION_FIELD_NAME, bounce.getQuestion());
		fields.put(Consts.OPTION_TITLE_FIELD_NAME, bounce.getOptionNames());

		QBCustomObject qbCustomObject = new QBCustomObject(
				Consts.BOUNCES_CLASS_NAME);
		qbCustomObject.setFields(fields);

		QBCustomObjects.createObject(qbCustomObject, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					QBCustomObjectResult qbCustomObjectResult = (QBCustomObjectResult) result;
					QBCustomObject qbCustomObject = qbCustomObjectResult
							.getCustomObject();

					bounce.setBounceId(qbCustomObject.getCustomObjectId());
					bounce.setStatus(Consts.BOUNCE_STATUS_SENT);
					bounce.setSendAt(qbCustomObject.getCreatedAt());
					updateBounce(bounce);
					// Log.d("New record: ", qbCustomObject.toString());
					createNewsForBackend(qbCustomObject);
					sendBouncePushNotification(qbCustomObject);
				} else {
					Log.e("Errors", result.getErrors().toString());
				}
			}

		});

	}

	public void sendBounce(final ArrayList<String> imageURIs,
			final Bounce bounce, final ArrayList<String> contents) {
		if (imageURIs.size() == 0) {
			// Log.d(TAG, "Done loading contents");
			finilizeSendingBounce(bounce, contents);
			return;
		} else {
			String uri = imageURIs.get(0);
			Log.d(TAG, "File URI is " + uri);
			imageURIs.remove(0);
			QBContent.uploadFileTask(uri, true, new QBCallback() {

				@Override
				public void onComplete(Result arg0, Object arg1) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onComplete(Result result) {
					// TODO Auto-generated method stub
					if (result.isSuccess()) {
						QBFileUploadTaskResult qbFileUploadTaskResultq = (QBFileUploadTaskResult) result;
						// Log.d(TAG, qbFileUploadTaskResultq.toString());
						String publicURL = qbFileUploadTaskResultq.getFile()
								.getUid();
						contents.add(publicURL);
						// Log.d(TAG, publicURL);
						sendBounce(imageURIs, bounce, contents);
					} else {
						Log.e(TAG, "Failed to send bounce");
					}
				}
			});
		}

	}

	public void setSelfProfileImage(byte[] content) {
		Contact self = getSelf();
		self.setProfileImage(content);
		updateSelf(self);
	}

	public void updateSelfProfilePicture() {
		Contact self = getSelf();
		if (self.getBlobID() != null) {
			QBContent.downloadFileTask(self.getBlobID().intValue(),
					new QBCallback() {

						@Override
						public void onComplete(Result arg0, Object arg1) {

						}

						@Override
						public void onComplete(Result result) {
							QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
							if (result.isSuccess()) {
								// get downloaded file content
								byte[] content = qbFileDownloadResult
										.getContent();
								setSelfProfileImage(content);
							} else {
								Log.e("Errors", result.getErrors().toString());
							}
						}
					});
		}
	}

	public void setProfileImage(Contact contact, byte[] content) {
		contact.setProfileImage(content);
		// Log.d(TAG, "setting profile Picture for " + contact.getID());
		// Log.d(TAG, "profile image is " +
		// contact.getProfileImage().toString());
		databaseHandler.updateContact(contact);
		notifyContactChanged();
		notifyBouncesChanged();
	}

	public void updateProfilePicture(final Contact contact) {
		if (contact.getBlobID() != null) {
			// Log.d(TAG,
			// "updateProfilePicture called for contact "
			// + contact.getID());
			QBContent.downloadFileTask(contact.getBlobID().intValue(),
					new QBCallback() {

						@Override
						public void onComplete(Result arg0, Object arg1) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onComplete(Result result) {
							QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
							if (result.isSuccess()) {
								// get downloaded file content
								byte[] content = qbFileDownloadResult
										.getContent();
								setProfileImage(contact, content);
							} else {
								Log.e("Errors", result.getErrors().toString());
							}
						}
					});
		}
	}

	public void userLogin(QBUser user) {
		// Log.d(TAG, "logged in with user phone number" + user.getPhone());
		Contact contact = new Contact(user.getId(), user.getLogin(),
				user.getFullName(), user.getPhone(), user.getFileId(), null,
				null);
		contact.setPassword(user.getPassword());
		databaseHandler.addSelf(contact);
		updateSelfProfilePicture();
	}

	public void updateSelfToBackend() {
		QBUser user = getSelfUser();
		user.setOldPassword(user.getPassword());
		QBUsers.updateUser(user, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					// Log.d(TAG, "Successfully updated self");
					notifyPersonalListener();
				} else {
					Log.e(TAG, "FAILED TO UPDATE SELF");
				}
			}
		});
	}

	public void updateSelf(Contact user) {
		databaseHandler.updateSelfContact(user);
		notifyBouncesChanged();
		updateSelfToBackend();
	}

	public Contact getSelf() {
		return databaseHandler.getSelfContact();
	}

	void notifyContactChanged() {
		if (contactListListeners == null)
			contactListListeners = new ArrayList<ContactListListener>();
		for (ContactListListener listener : contactListListeners) {
			listener.onContactsChanged();
		}
	}

	private void notifySessionCreated() {
		if (sessionCreatedListeners == null)
			sessionCreatedListeners = new ArrayList<SessionCreatedListener>();
		for (SessionCreatedListener listener : sessionCreatedListeners) {
			listener.onSessionWithUserCreated();
		}
	}

	void notifyBouncesChanged() {
		if (bouncesListListeners == null)
			bouncesListListeners = new ArrayList<BouncesListListener>();
		for (BouncesListListener listener : bouncesListListeners) {
			listener.onBouncesChanged();
		}
	}

	public void addContact(QBUser user) {
		Contact contact = new Contact(user.getId(), user.getLogin(),
				user.getFullName(), user.getPhone(), user.getFileId(), null,
				user.getUpdatedAt());

		if (user.getLogin().equals(getSelf().getLogin())) {
			Log.d(TAG, "trying to add myself");
			return;
		}

		Log.d(TAG,
				"Adding a contact with phone number" + contact.getPhoneNumber());
		if (databaseHandler.getContactWithUserId(contact.getUserID()) != null) {
			Log.d(TAG, "Contact is already there");
			return;
		}
		contact.setID(databaseHandler.addContact(contact));
		updateProfilePicture(contact);
		notifyContactChanged();
	}

	public void updateContact(Contact contact) {
		databaseHandler.updateContact(contact);
		updateProfilePicture(contact);
		notifyContactChanged();
	}

	public Contact getContactWithUserId(Integer userID) {
		return databaseHandler.getContactWithUserId(userID);
	}

	public void removeContact(Contact contact) {
		databaseHandler.deleteContact(contact);
	}

	public void updateContact(QBUser user) {
		Contact newContact = new Contact(user.getId(), user.getLogin(),
				user.getFullName(), user.getPhone(), user.getFileId(), null,
				user.getUpdatedAt());

		Contact oldContact = getContactWithUserId(user.getId());
		if (oldContact == null
				|| !oldContact.getUpdatedAt().equals(newContact.getUpdatedAt())) {
			newContact.setID(oldContact.getID());
			updateContact(newContact);
		}

	}

	public void loadLikewithID(String like_id) {
		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.eq("_id", like_id);

		QBCustomObjects.getObjects(Consts.LIKES_CLASS_NAME, requestBuilder,
				new QBCallback() {

					@Override
					public void onComplete(Result arg0, Object arg1) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onComplete(Result result) {
						QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
						ArrayList<QBCustomObject> co = coresult
								.getCustomObjects();

						for (int i = 0; i < co.size(); i++) {
							QBCustomObject likeObject = co.get(i);
							addLikeFromCustomObject(likeObject);
						}
					}
				});
	}

	public void loadSeenBywithID(String seenBy_id) {
		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.eq("_id", seenBy_id);

		QBCustomObjects.getObjects(Consts.SEEN_CLASS_NAME, requestBuilder,
				new QBCallback() {

					@Override
					public void onComplete(Result arg0, Object arg1) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onComplete(Result result) {
						QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
						ArrayList<QBCustomObject> co = coresult
								.getCustomObjects();

						for (int i = 0; i < co.size(); i++) {
							QBCustomObject seenObject = co.get(i);
							addSeenFromCustomObject(seenObject);
						}
					}
				});
	}

	public Bounce loadBouncewithID(String bounce_id) {
		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.eq("_id", bounce_id);

		QBCustomObjects.getObjects(Consts.BOUNCES_CLASS_NAME, requestBuilder,
				new QBCallback() {

					@Override
					public void onComplete(Result arg0, Object arg1) {

					}

					@Override
					public void onComplete(Result result) {
						QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
						ArrayList<QBCustomObject> co = coresult
								.getCustomObjects();

						for (int i = 0; i < co.size(); i++) {
							QBCustomObject bounceObject = co.get(i);
							// Log.d(TAG,
							// "bounce object is : "
							// + bounceObject.toString());
							addBounceFromCustomObject(bounceObject);
						}
					}
				});

		return null;

	}

	public Bounce getBounceWithId(String bounce_id) {
		return findBounceWithId(bounce_id);
	}

	private Bounce findBounceWithId(String bounce_id) {
		return databaseHandler.getBounceWithBounceID(bounce_id);
	}

	public ArrayList<Like> getLikesForBounce(String bounce_id) {
		return databaseHandler.getAllLikesForBounce(bounce_id);
	}

	public Boolean getIsLikedBySelf(String bounceId, int optionNumber) {
		Like like = new Like(bounceId, getSelf().getUserID(), optionNumber);

		if (databaseHandler.getLike(like) == null)
			return false;
		else
			return true;
	}

	public ArrayList<Like> getAllLikes(String bounceId, int optionNumber) {
		return databaseHandler.getAllLikesForBounceAndOptionNumber(bounceId,
				optionNumber);
	}

	public void sendLike(Bounce bounce, int position) {

		String type;

		Like like = new Like(bounce.getBounceId(), getSelf().getUserID(),
				position);

		if (databaseHandler.getLike(like) == null) {
			type = Consts.LIKE_TYPE_LIKE;
			databaseHandler.addLike(like);
		} else {
			type = Consts.LIKE_TYPE_DISLIKE;
			databaseHandler.removeLike(like);
		}

		notifyLikeListener();

		QBCustomObject qbCustomObject = new QBCustomObject(
				Consts.LIKES_CLASS_NAME);
		HashMap<String, Object> fields = new HashMap<String, Object>();

		fields.put(Consts.LIKE_BOUNCEID_FIELD_NAME, like.getBounceId());
		fields.put(Consts.LIKE_SENDER_FIELD_NAME, like.getSenderId());
		fields.put(Consts.LIKE_OPTION_FIELD_NAME, position);
		fields.put(Consts.LIKE_BOUNCE_OWNER_FIELD_NAME, bounce.getSender());
		fields.put(Consts.LIKE_TYPE_FIELD_NAME, type);

		qbCustomObject.setFields(fields);

		QBCustomObjects.createObject(qbCustomObject, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					QBCustomObjectResult qbCustomObjectResult = (QBCustomObjectResult) result;
					QBCustomObject qbCustomObject = qbCustomObjectResult
							.getCustomObject();
					Log.d("New record: ", qbCustomObject.toString());
					createLikeNewsForBackend(qbCustomObject);
					sendLikePushNotification(qbCustomObject);
				} else {
					Log.e("Errors", result.getErrors().toString());
				}
			}

		});

	}

	public void sync() {
		loadContactsFromPhoneBase();
		updateExistingContacts();
	}

	public void updateExistingContacts() {
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		ArrayList<Contact> contacts = databaseHandler.getAllContacts();
		for (int i = 0; i < contacts.size(); i++)
			userIds.add(contacts.get(i).getUserID());

		QBUsers.getUsersByIDs(userIds, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					QBUserPagedResult results = (QBUserPagedResult) result;
					ArrayList<QBUser> users = results.getUsers();
					for (QBUser user : users) {
						updateContact(user);
					}
				} else {
					Log.e(TAG, "Error on adding contacts"
							+ result.getErrors().toString());
				}
			}
		});
	}

	public void addContactByPhone(String phoneNumber) {
		Collection<String> usersPhoneNumbers = new ArrayList<String>();
		usersPhoneNumbers.add(phoneNumber);
		addContactByPhones(usersPhoneNumbers);
	}

	public void addContactByPhones(Collection<String> usersPhoneNumbers) {

		QBUsers.getUsersByPhoneNumbers(usersPhoneNumbers, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {

			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					QBUserPagedResult results = (QBUserPagedResult) result;
					ArrayList<QBUser> users = results.getUsers();
					for (QBUser user : users) {
						addContact(user);
					}
				} else {
					Log.e(TAG, "Error on adding contacts"
							+ result.getErrors().toString());
				}
			}
		});
	}

	public void loadContactsFromPhoneBase() {

		ContentResolver contResv = context.getContentResolver();

		Cursor cursor = contResv.query(ContactsContract.Contacts.CONTENT_URI,
				null, null, null, null);
		if (cursor.moveToFirst()) {
			ArrayList<String> contactPhones = new ArrayList<String>();
			do {
				String id = cursor.getString(cursor
						.getColumnIndex(ContactsContract.Contacts._ID));

				if (Integer
						.parseInt(cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = contResv.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						String contactNumber = pCur
								.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						// addContactByPhone(contactNumber);
						contactNumber = Utils
								.standartPhoneNumber(contactNumber);
						if (contactNumber != null)
							contactPhones.add(contactNumber);
						// break;
					}
					pCur.close();
				}

			} while (cursor.moveToNext());
			Log.d(TAG, "all contacts got from phone " + contactPhones);
			addContactByPhones(contactPhones);
		}

	}

	public QBUser getSelfUser() {
		Contact selfContact = getSelf();
		if (selfContact == null)
			return null;
		QBUser user = new QBUser(selfContact.getUserID());
		user.setLogin(selfContact.getLogin());
		user.setPassword(selfContact.getPassword());
		user.setFullName(selfContact.getName());
		user.setPhone(selfContact.getPhoneNumber());
		user.setFileId(selfContact.getBlobID());
		Log.d(TAG, "selfContact password is " + selfContact.getPassword());
		return user;
	}

	public void setSession(boolean isSessionMade) {
		sessionState = isSessionMade;
	}

	public boolean getSessionState() {
		return sessionState;
	}

	public void createSession() {
		QBUser user = getSelfUser();

		Log.d(TAG, "Trying to create a session with user " + user);

		if (user == null) {
			QBAuth.createSession(new QBCallback() {

				@Override
				public void onComplete(Result arg0, Object arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onComplete(Result result) {
					// TODO Auto-generated method stub
					if (result.isSuccess()) {
						Log.d(TAG, "created a session without user");
					} else {
						Log.e(TAG, "failed to create a session without user");
					}
				}
			});
		} else {
			QBAuth.createSession(user, new QBCallback() {

				@Override
				public void onComplete(Result arg0, Object arg1) {

				}

				@Override
				public void onComplete(Result result) {
					if (result.isSuccess()) {
						Log.d(TAG, "created a session with a user");
						notifySessionCreated();
						sync();
					} else {
						Log.e(TAG, "failed to create a session with user");
					}
				}

			});
		}
	}

	public void addNewsFromCustomObject(QBCustomObject newsObject) {

		if (databaseHandler.findNews(newsObject.getCustomObjectId()) != null) {
			return;
		} else {
			databaseHandler.addNews(newsObject.getCustomObjectId());
		}

		HashMap<String, Object> fields = newsObject.getFields();

		String type = (String) fields.get(Consts.NEWS_TYPE_FIELD_NAME);
		String object_id = (String) fields
				.get(Consts.NEWS_OBJECT_ID_FIELD_NAME);

		if (type != null && type.equals(Consts.NEWS_TYPE_BOUNCE)) {
			loadBouncewithID(object_id);
		}

		if (type != null && type.equals(Consts.NEWS_TYPE_SEEN_BY)) {
			loadSeenBywithID(object_id);
		}

		if (type != null && type.equals(Consts.NEWS_TYPE_LIKE)) {
			loadLikewithID(object_id);
		}

	}

	public void deleteObject(QBCustomObject customObject) {
		QBCustomObjects.deleteObject(customObject, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					Log.d(TAG, "object successfully deleted !!!");
				} else {
					Log.e(TAG, "Failed to delete object");
				}
			}
		});
	}

	public void updateNews() {

		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.in(Consts.NEWS_OWNER_FIELD_NAME, getSelf().getUserID());

		requestBuilder.setPagesLimit(1000);
		requestBuilder.sortDesc("ID");

		QBCustomObjects.getObjects(Consts.NEWS_CLASS_NAME, requestBuilder,
				new QBCallback() {

					@Override
					public void onComplete(Result arg0, Object arg1) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onComplete(Result result) {
						QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
						ArrayList<QBCustomObject> co = coresult
								.getCustomObjects();
						Log.d(TAG, "received" + co.size() + " news.");
						for (int i = 0; i < co.size(); i++) {
							QBCustomObject newsObject = co.get(i);
							addNewsFromCustomObject(newsObject);
							deleteObject(newsObject);
						}
					}
				});

	}

	public Bounce getBounceWithInternalId(long bounceID) {
		return databaseHandler.getBounce(bounceID);
	}

	public long addDraftBounce(Bounce bounce) {
		long bounce_id = databaseHandler.addBounce(bounce);
		notifyBouncesChanged();
		return bounce_id;
	}

	public void updateBounce(Bounce bounce) {
		databaseHandler.updateBounce(bounce);
		notifyBouncesChanged();
	}

	public void removeBounceWithInternalID(Integer ID) {
		databaseHandler.deleteBounceWithID(ID);
		notifyBouncesChanged();
	}

	public void sendIsSeenMessage(Bounce bounce) {
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(Consts.SEEN_OWNER_FIELD_NAME, bounce.getSender());
		fields.put(Consts.SEEN_BOUNCE_ID_FIELD_NAME, bounce.getBounceId());
		fields.put(Consts.SEEN_CONTACTS_FIELD_NAME, getSelf().getUserID());

		QBCustomObject qbCustomObject = new QBCustomObject(
				Consts.SEEN_CLASS_NAME);
		qbCustomObject.setFields(fields);
		QBCustomObjects.createObject(qbCustomObject, new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Result result) {
				// TODO Auto-generated method stub
				if (result.isSuccess()) {
					Log.d(TAG, "New SEEN record on the backend");
					QBCustomObjectResult qbCustomObjectResult = (QBCustomObjectResult) result;
					QBCustomObject qbCustomObject = qbCustomObjectResult
							.getCustomObject();
					createSeenNewsForBackend(qbCustomObject);
					sendSeenPushNotification(qbCustomObject);
				} else {
					Log.e(TAG, "Failed to create new SEEN record");
				}
			}
		});
	}

	public void updateNewsListeners() {
		// updateNews();
		if (newsListeners == null)
			return;
		for (int i = 0; i < newsListeners.size(); i++) {
			newsListeners.get(i).onNewNews();
		}
	}

}
