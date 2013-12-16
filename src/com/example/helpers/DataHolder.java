package com.example.helpers;

import static com.example.definitions.Consts.BOUNCES_CLASS_NAME;
import static com.example.definitions.Consts.LIKES_CLASS_NAME;
import static com.example.definitions.Consts.LIKE_BOUNCEID_FIELD_NAME;
import static com.example.definitions.Consts.LIKE_BOUNCE_OWNER_FIELD_NAME;
import static com.example.definitions.Consts.LIKE_OPTION_FIELD_NAME;
import static com.example.definitions.Consts.LIKE_SENDER_FIELD_NAME;
import static com.example.definitions.Consts.LIKE_SENDER_LOGIN_FIELD_NAME;
import static com.example.definitions.Consts.MESSAGE_TYPE_BOUNCE;
import static com.example.definitions.Consts.MESSAGE_TYPE_LIKE;
import static com.example.definitions.Consts.OWNER_FIELD_NAME;
import static com.example.definitions.Consts.RECEIVERS_FIELD_NAME;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.definitions.Consts;
import com.example.interfaces.BounceListener;
import com.example.interfaces.BouncesListListener;
import com.example.interfaces.ContactListListener;
import com.example.interfaces.LikeListener;
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
	private static ArrayList<Contact> contacts;
	private static ArrayList<ContactListListener> contactListListeners;
	private static ArrayList<BouncesListListener> bouncesListListeners;
	private static HashMap<String, ArrayList<BounceListener>> bounceListeners;
	private static HashMap<String, ArrayList<LikeListener>> likeListeners;
	private static HashMap<String, ArrayList<Like>> likes;
	private static ArrayList<Bounce> bounces;
	private static SharedPreferences sharedPreferences;
	private static boolean sessionState = false;

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

	public void registerBouncesListListener(BouncesListListener listener) {
		if (bouncesListListeners == null)
			bouncesListListeners = new ArrayList<BouncesListListener>();
		bouncesListListeners.add(listener);
	}

	public void registerLikeListener(LikeListener listener, String bounce_id) {
		if (likeListeners == null) {
			likeListeners = new HashMap<String, ArrayList<LikeListener>>();
		}
		if (likeListeners.get(bounce_id) == null) {
			likeListeners.put(bounce_id, new ArrayList<LikeListener>());
		}
		likeListeners.get(bounce_id).add(listener);
	}

	public void registerBounceListener(BounceListener listener, String bounce_id) {
		if (bounceListeners == null) {
			bounceListeners = new HashMap<String, ArrayList<BounceListener>>();
		}
		if (bounceListeners.get(bounce_id) == null) {
			bounceListeners.put(bounce_id, new ArrayList<BounceListener>());
		}
		bounceListeners.get(bounce_id).add(listener);
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
		if (contacts == null) {
			contacts = databaseHandler.getAllContacts();
		}

		if (bounces == null) {
			bounces = databaseHandler.getAllBounces();
		}

		if (sharedPreferences == null) {
			sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, 0);
		}

		context = ctx;
		return dataHolder;
	}

	private void notifyBounceListener(String bounceId) {
		if (bounceListeners == null)
			return;
		if (bounceListeners.get(bounceId) == null)
			return;
		for (int i = 0; i < bounceListeners.get(bounceId).size(); i++) {
			bounceListeners.get(bounceId).get(i)
					.onBounceArrived(getBounceWithId(bounceId));
		}
	}

	private void notifyLikeListener(String bounceId) {
		if (likeListeners == null)
			return;
		if (likeListeners.get(bounceId) == null)
			return;
		for (int i = 0; i < likeListeners.get(bounceId).size(); i++) {
			likeListeners.get(bounceId).get(i)
					.onLikesArrived(likes.get(bounceId));
		}
	}

	public ArrayList<Bounce> getBounces() {
		return bounces;
	}

	public ArrayList<Contact> getContacts() {
		return contacts;
	}

	private void loadBounceContent(Bounce bounce) {
		Log.d(TAG, "started loading Bounce content for " + bounce);

		ArrayList<String> newContents = new ArrayList<String>();

		for (int position = 0; position < bounce.getNumberOfOptions(); position++) {
			Log.d(TAG, "trying for position: " + position);
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
						.getAbsolutePath() + "/BounceCloud-Images";
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
				newContents.add(Uri.fromFile(file).toString());
				Log.d(TAG,
						"Saved file of length " + total + " to "
								+ output.toString() + " with Uri equal to "
								+ Uri.fromFile(file).toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		bounce.setContents(newContents);
		// Log.d(TAG, " the result of update:  " +
		// databaseHandler.updateBounce(bounce));
		bounce.setStatus(Consts.BOUNCE_STATUS_RECEIVED);
		databaseHandler.updateBounce(bounce);
		// bounces.add(bounce);
		notifyBouncesChanged();
		notifyBounceListener(bounce.getBounceId());
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

		int senderId = Integer
				.parseInt(fields.get(OWNER_FIELD_NAME).toString());

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

		final Bounce bounce = new Bounce(senderId, numberOfOptions, types,
				contents, receivers, qbCustomObject.getCustomObjectId(),
				isFromSelf, question, optionTitles,
				qbCustomObject.getCreatedAt(), Consts.BOUNCE_STATUS_LOADING);

		Log.d(TAG, bounce.toString());

		new AsyncTask() {

			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				loadBounceContent(bounce);
				return null;
			}

		}.execute(null, null, null);

		bounces.add(bounce);
		databaseHandler.addBounce(bounce);
		notifyBouncesChanged();
		notifyBounceListener(bounce.getBounceId());
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
					Log.d("User", qbUserResult.getUser().toString());
				} else {
					Log.e("Errors", result.getErrors().toString());
				}
			}
		});
	}

	public String addLikeFromCustomObject(QBCustomObject qbCustomObject) {
		HashMap<String, Object> fields = qbCustomObject.getFields();

		int option = Integer.parseInt(fields.get(LIKE_OPTION_FIELD_NAME)
				.toString());
		int sender_id = Integer.parseInt(fields.get(LIKE_SENDER_FIELD_NAME)
				.toString());

		String bounce_id = (String) fields.get(LIKE_BOUNCEID_FIELD_NAME);

		String sender_login = (String) fields.get(LIKE_SENDER_LOGIN_FIELD_NAME);

		int bounce_owner = Integer.parseInt(fields.get(
				LIKE_BOUNCE_OWNER_FIELD_NAME).toString());

		Like like = new Like(bounce_id, sender_id, sender_login, bounce_owner,
				option);

		if (likes == null) {
			likes = new HashMap<String, ArrayList<Like>>();
		}

		if (likes.get(bounce_id) == null) {
			likes.put(bounce_id, new ArrayList<Like>());
		}
		likes.get(bounce_id).add(like);
		return bounce_id;
	}

	public void sendBouncePushNotification(QBCustomObject qbCustomObject) {

		HashMap<String, Object> fields = qbCustomObject.getFields();
		ArrayList<Integer> receivers = Utils
				.castToIntArrayFromStringArray((ArrayList<String>) fields
						.get(RECEIVERS_FIELD_NAME));
		Log.d(TAG, qbCustomObject.toString());
		Log.d(TAG, qbCustomObject.getCustomObjectId());

		StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
		userIds.addAll(receivers);

		QBEvent event = new QBEvent();
		event.setUserIds(userIds);
		event.setEnvironment(QBEnvironment.DEVELOPMENT);
		event.setNotificationType(QBNotificationType.PUSH);

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("type", MESSAGE_TYPE_BOUNCE);
			jsonObject.put("bounce_id", qbCustomObject.getCustomObjectId()
					.toString());
			jsonObject.put("sender_login", getSelf().getName());
			jsonObject.put("message", "I have a bounce for you");
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

	public void sendLikePushNotification(QBCustomObject qbCustomObject) {

		HashMap<String, Object> fields = qbCustomObject.getFields();

		String bounce_id = (String) fields.get(LIKE_BOUNCEID_FIELD_NAME);
		int option = Integer.parseInt((String) fields
				.get(LIKE_OPTION_FIELD_NAME));
		int bounce_owner = Integer.parseInt((String) fields
				.get(LIKE_BOUNCE_OWNER_FIELD_NAME));

		StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
		userIds.add(bounce_owner);

		QBEvent event = new QBEvent();
		event.setUserIds(userIds);
		event.setEnvironment(QBEnvironment.DEVELOPMENT);
		event.setNotificationType(QBNotificationType.PUSH);

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("type", MESSAGE_TYPE_LIKE);
			jsonObject.put("bounce_id", bounce_id);
			jsonObject.put("option", option);
			jsonObject.put("sender_login", getSelf().getName());
			jsonObject.put("message", "I liked the following option");
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

		QBCustomObject qbCustomObject = new QBCustomObject(BOUNCES_CLASS_NAME);
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
					Log.d("New record: ", qbCustomObject.toString());
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
			Log.d(TAG, "Done loading contents");
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
						Log.d(TAG, qbFileUploadTaskResultq.toString());
						String publicURL = qbFileUploadTaskResultq.getFile()
								.getUid();
						contents.add(publicURL);
						Log.d(TAG, publicURL);
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
		databaseHandler.updateContact(contact);
		notifyContactChanged();
	}

	public void updateProfilePicture(final Contact contact) {
		if (contact.getBlobID() != null) {
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
		Log.d(TAG, "logged in with user phone number" + user.getPhone());
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
					Log.d(TAG, "Successfully updated self");
				} else {
					Log.e(TAG, "FAILED TO UPDATE SELF");
				}
			}
		});
	}

	public void updateSelf(Contact user) {
		databaseHandler.updateSelfContact(user);
		updateSelfToBackend();
		notifyBouncesChanged();
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

	void notifyBouncesChanged() {
		if (bouncesListListeners == null)
			bouncesListListeners = new ArrayList<BouncesListListener>();
		bounces = databaseHandler.getAllBounces();
		for (BouncesListListener listener : bouncesListListeners) {
			listener.onBouncesChanged();
		}
	}

	public int getBouncesSize() {
		return bounces.size();
	}

	public Bounce getBounceAtIndex(int position) {
		return bounces.get(position);
	}

	public int getContactsSize() {
		return contacts.size();
	}

	public Contact getContactAtIndex(int position) {
		return contacts.get(position);
	}

	public void addContact(QBUser user) {
		Contact contact = new Contact(user.getId(), user.getLogin(),
				user.getFullName(), user.getPhone(), user.getFileId(), null,
				user.getUpdatedAt());
		Log.d(TAG, "Adding a contact" + contact.toString());
		if (contacts.contains(contact)) {
			Log.d(TAG, "Contact is already there");
			return;
		}
		contacts.add(contact);
		databaseHandler.addContact(contact);
		updateProfilePicture(contact);
		notifyContactChanged();
	}

	public Contact getContactWithUserId(Integer ID) {
		for (int i = 0; i < contacts.size(); i++)
			if (contacts.get(i).getUserID().equals(ID)) {
				return contacts.get(i);
			}
		return null;
	}

	public void removeContact(Contact contact) {
		databaseHandler.deleteContact(contact);
		contacts.remove(contact);
	}

	public void updateContact(QBUser user) {
		Contact newContact = new Contact(user.getId(), user.getLogin(),
				user.getFullName(), user.getPhone(), user.getFileId(), null,
				user.getUpdatedAt());

		Contact oldContact = getContactWithUserId(user.getId());
		if (oldContact == null
				|| oldContact.getUpdatedAt() != newContact.getUpdatedAt()) {
			if (oldContact != null) {
				removeContact(oldContact);
			}
			addContact(user);
		}

	}

	private void addResult(Result result) {
		Log.d(TAG, result.toString());
		QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
		ArrayList<QBCustomObject> co = coresult.getCustomObjects();

		for (int i = 0; i < co.size(); i++) {
			QBCustomObject bounceObject = co.get(i);
			Log.d(TAG, "bounce object is : " + bounceObject.toString());
			addBounceFromCustomObject(bounceObject);
		}
	}

	public Bounce getBounceWithId(String bounce_id) {

		if (findBounceWithId(bounce_id) == null) {
			QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
			requestBuilder.eq("_id", bounce_id);

			QBCustomObjects.getObjects(BOUNCES_CLASS_NAME, requestBuilder,
					new QBCallback() {

						@Override
						public void onComplete(Result arg0, Object arg1) {

						}

						@Override
						public void onComplete(Result result) {
							addResult(result);
						}
					});

			return null;
		}
		return findBounceWithId(bounce_id);
	}

	private Bounce findBounceWithId(String bounce_id) {

		Log.d(TAG, "started looking for " + bounce_id);

		if (bounces == null) {
			return null;
		} else {
			for (int i = 0; i < bounces.size(); i++) {
				if (bounces.get(i).getBounceId().equals(bounce_id))
					return bounces.get(i);
			}
		}
		return null;
	}

	public void getLikes(String bounce_id) {

		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.eq(LIKE_BOUNCEID_FIELD_NAME, bounce_id);
		if (likes == null) {
			likes = new HashMap<String, ArrayList<Like>>();
		}

		if (likes.get(bounce_id) != null) {
			likes.remove(bounce_id);
		}

		likes.put(bounce_id, new ArrayList<Like>());

		QBCustomObjects.getObjects(LIKES_CLASS_NAME, requestBuilder,
				new QBCallback() {
					@Override
					public void onComplete(Result arg0, Object arg1) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onComplete(Result result) {
						// TODO Auto-generated method stub
						if (result.isSuccess()) {
							QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
							ArrayList<QBCustomObject> co = coresult
									.getCustomObjects();
							String bounce_id = null;
							for (int i = 0; i < co.size(); i++) {
								QBCustomObject likeObject = co.get(i);
								bounce_id = addLikeFromCustomObject(likeObject);
							}
							if (bounce_id != null)
								notifyLikeListener(bounce_id);
						} else {
							Log.e(TAG, "on getting owner bounces"
									+ result.getErrors().toString());
						}

					}
				});

	}

	public void sendLike(Bounce bounce, int position) {
		int sender_id = getSelf().getUserID();
		QBCustomObject qbCustomObject = new QBCustomObject(LIKES_CLASS_NAME);

		HashMap<String, Object> fields = new HashMap<String, Object>();

		fields.put(LIKE_BOUNCEID_FIELD_NAME, bounce.getBounceId());
		fields.put(LIKE_SENDER_FIELD_NAME, sender_id);
		fields.put(LIKE_SENDER_LOGIN_FIELD_NAME, getSelf().getName());
		fields.put(LIKE_OPTION_FIELD_NAME, position);
		fields.put(LIKE_BOUNCE_OWNER_FIELD_NAME, bounce.getSender());

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
		ArrayList<String> userIds = new ArrayList<String>();
		for (int i = 0; i < contacts.size(); i++)
			userIds.add(contacts.get(i).getUserID().toString());

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
						sync();
					} else {
						Log.e(TAG, "failed to create a session with user");
					}
				}
			});
		}
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

}
