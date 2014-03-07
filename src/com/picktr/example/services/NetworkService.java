package com.picktr.example.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;

import com.picktr.example.definitions.Consts;
import com.picktr.example.helpers.Bounce;
import com.picktr.example.helpers.BounceOption;
import com.picktr.example.helpers.Contact;
import com.picktr.example.helpers.DataHolder;
import com.picktr.example.helpers.Like;
import com.picktr.example.helpers.Utils;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.auth.QBAuth;
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

public class NetworkService extends Service {

	private static final String TAG = "NetworkService";
	private final IBinder mBinder = new NetworkBinder();

	private Handler sessionHandler;
	private Runnable createSessionRunnable;
	private Handler updateHandler;
	private Runnable updatingRunnable;
	private Handler sendingHandler;
	private Runnable sendingRunnable;
	private ConnectivityManager conManager = null;
	private DataHolder dataHolder;
	private ArrayList<Long> pendingCards;
	private ArrayList<Long> sendingCards;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	public boolean isNetworkConnected() {
		if (conManager == null)
			return false;
		if (conManager.getActiveNetworkInfo() == null)
			return false;
		return conManager.getActiveNetworkInfo().isConnected();
	}

	private void sync() {
		loadContactsFromPhoneBase();
		updateExistingContacts();
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
						dataHolder.addContact(user);
					}
				} else {
					Log.e(TAG, "Error on adding contacts"
							+ result.getErrors().toString());
				}
			}
		});
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
							dataHolder.addLikeFromCustomObject(likeObject);
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
							dataHolder.addSeenFromCustomObject(seenObject);
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
							dataHolder.addBounceFromCustomObject(bounceObject);
						}
					}
				});

		return null;
	}

	public void loadContactsFromPhoneBase() {
		ContentResolver contResv = getContentResolver();
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

	public void createSession() {
		if (!isNetworkConnected())
			return;
		QBUser user = dataHolder.getSelfUser();
		Log.d(TAG, "Trying to create a session with user " + user);
		if (user == null) {
			QBAuth.createSession(new QBCallback() {
				@Override
				public void onComplete(Result arg0, Object arg1) {
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

	public void updateExistingContacts() {
		ArrayList<Integer> userIds = new ArrayList<Integer>();
		ArrayList<Contact> contacts = dataHolder.getAllContacts();
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
						dataHolder.updateContact(user);
					}
				} else {
					Log.e(TAG, "Error on adding contacts"
							+ result.getErrors().toString());
				}
			}
		});
	}

	public void addNewsFromCustomObject(QBCustomObject newsObject) {
		if (dataHolder.findNews(newsObject.getCustomObjectId()) != null) {
			return;
		} else {
			dataHolder.addNews(newsObject.getCustomObjectId());
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
		if (dataHolder.getSelf() == null)
			return;
		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.in(Consts.NEWS_OWNER_FIELD_NAME, dataHolder.getSelf()
				.getUserID());
		requestBuilder.setPagesLimit(1000);
		requestBuilder.sortDesc("ID");

		QBCustomObjects.getObjects(Consts.NEWS_CLASS_NAME, requestBuilder,
				new QBCallback() {
					@Override
					public void onComplete(Result arg0, Object arg1) {
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

	private void checkUpdates() {
		updateNews();
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
			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					// Log.d(TAG, "New NEWS RECORD IS CREATED ON THE BACKEND!");
				} else {
					Log.e(TAG, "Failed to create new NEWS record");
				}
			}
		});

	}

	public void createNewBounceNewsForBackend(QBCustomObject bounceObject) {
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
			jsonObject.put("sender_login", dataHolder.getSelf().getLogin());
			jsonObject.put("message", dataHolder.getSelf().getName()
					+ ": I have a bounce for you");
		} catch (JSONException e) {
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

	private void sendBounceToQB(final Bounce bounce) {
		Log.d(TAG, "Trying to actuall send a bounce!");
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(Consts.OWNER_FIELD_NAME, bounce.getSender());
		ArrayList<BounceOption> options = bounce.getOptions();
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<String> contents = new ArrayList<String>();
		ArrayList<Integer> types = new ArrayList<Integer>();
		for (int i = 0; i < options.size(); i++) {
			titles.add(options.get(i).getTitle());

			if (options.get(i).getType() == Consts.CONTENT_TYPE_IMAGE) {
				contents.add(Base64.encodeToString(options.get(i).getImage(),
						Base64.NO_WRAP));
			} else if (options.get(i).getType() == Consts.CONTENT_TYPE_URL) {
				contents.add(options.get(i).getUrl());
			}
			types.add(options.get(i).getType());
		}

		fields.put(Consts.TYPE_FIELD_NAME, types);
		fields.put(Consts.CONTENT_FIELD_NAME, contents);
		fields.put(Consts.NUMBER_OF_OPTIONS_FIELD_NAME,
				bounce.getNumberOfOptions());
		fields.put(Consts.RECEIVERS_FIELD_NAME, bounce.getReceivers());
		fields.put(Consts.QUESTION_FIELD_NAME, bounce.getQuestion());
		fields.put(Consts.OPTION_TITLE_FIELD_NAME, titles);

		QBCustomObject qbCustomObject = new QBCustomObject(
				Consts.BOUNCES_CLASS_NAME);
		qbCustomObject.setFields(fields);
		QBCustomObjects.createObject(qbCustomObject, new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {
			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					sendingCards.remove(bounce.getID());
					pendingCards.remove(bounce.getID());

					QBCustomObjectResult qbCustomObjectResult = (QBCustomObjectResult) result;
					QBCustomObject qbCustomObject = qbCustomObjectResult
							.getCustomObject();
					bounce.setQBID(qbCustomObject.getCustomObjectId());
					bounce.setStatus(Consts.BOUNCE_STATUS_SENT);
					bounce.setSendAt(qbCustomObject.getCreatedAt());
					dataHolder.updateBounce(bounce);
					createNewBounceNewsForBackend(qbCustomObject);
					sendBouncePushNotification(qbCustomObject);
				} else {
					Log.e("Errors", result.getErrors().toString());
					sendingCards.remove(bounce.getID());
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
			jsonObject.put("sender_login", dataHolder.getSelf().getLogin());
			jsonObject.put("message", dataHolder.getSelf().getLogin()
					+ ": I have seen your bounce");
		} catch (JSONException e) {
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
			jsonObject.put("message", dataHolder.getSelf().getDisplayName()
					+ ": I " + (String) fields.get(Consts.LIKE_TYPE_FIELD_NAME)
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

	public void sendIsSeenMessage(Bounce bounce) {
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(Consts.SEEN_OWNER_FIELD_NAME, bounce.getSender());
		fields.put(Consts.SEEN_BOUNCE_ID_FIELD_NAME, bounce.getQBID());
		fields.put(Consts.SEEN_CONTACTS_FIELD_NAME, dataHolder.getSelf()
				.getUserID());

		QBCustomObject qbCustomObject = new QBCustomObject(
				Consts.SEEN_CLASS_NAME);
		qbCustomObject.setFields(fields);
		QBCustomObjects.createObject(qbCustomObject, new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {
			}

			@Override
			public void onComplete(Result result) {
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

	public void sendLike(Bounce bounce, int position) {

		String type;

		Like like = new Like(bounce.getQBID(),
				dataHolder.getSelf().getUserID(), position);

		if (dataHolder.getLike(like) == null) {
			type = Consts.LIKE_TYPE_LIKE;
			dataHolder.addLike(like);
		} else {
			type = Consts.LIKE_TYPE_DISLIKE;
			dataHolder.removeLike(like);
		}

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

	private void sendBounce(final Bounce bounce) {
		if (!pendingCards.contains(bounce.getID())) {
			pendingCards.add(bounce.getID());
		}
		if (sendingCards.contains(bounce.getID())) {
			return;
		}
		sendingCards.add(bounce.getID());
		if (!bounce.getStatus().equals(Consts.BOUNCE_STATUS_SENDING)) {
			bounce.setStatus(Consts.BOUNCE_STATUS_SENDING);
		}
		dataHolder.updateBounce(bounce);

		sendBounceToQB(bounce);

	}

	private void sendPending() {
		Log.d(TAG,
				"trying to send Pending cards size is " + pendingCards.size());
		for (long id : pendingCards) {
			Bounce bounce = dataHolder.getBounceWithInternalId(id);
			sendBounce(bounce);
		}
	}

	public void putBounceToSend(final Bounce bounce) {
		bounce.setStatus(Consts.BOUNCE_STATUS_PENDING);
		dataHolder.updateBounce(bounce);
		pendingCards.add(bounce.getID());
	}

	private void createPendingList() {
		pendingCards = new ArrayList<Long>();
		sendingCards = new ArrayList<Long>();

		List<Bounce> bounces = dataHolder.getBounces();
		for (Bounce bounce : bounces) {
			if (bounce.getStatus().equals(Consts.BOUNCE_STATUS_SENDING)
					|| bounce.getStatus().equals(Consts.BOUNCE_STATUS_PENDING)) {
				pendingCards.add(bounce.getID());
				if (!bounce.getStatus().equals(Consts.BOUNCE_STATUS_PENDING)) {
					bounce.setStatus(Consts.BOUNCE_STATUS_PENDING);
					dataHolder.updateBounce(bounce);
				}
			}
		}
	}

	@Override
	public void onCreate() {
		sessionHandler = new Handler();
		updateHandler = new Handler();
		sendingHandler = new Handler();
		conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		dataHolder = DataHolder.getDataHolder(getApplicationContext());

		createSessionRunnable = new Runnable() {
			@Override
			public void run() {
				createSession();
				sessionHandler.postDelayed(createSessionRunnable, 3600000);
			}
		};
		updatingRunnable = new Runnable() {
			@Override
			public void run() {
				checkUpdates();
				updateHandler.postDelayed(updatingRunnable, 3000);
			}
		};

		createPendingList();

		sendingRunnable = new Runnable() {
			@Override
			public void run() {
				sendPending();
				sendingHandler.postDelayed(sendingRunnable, 3000);
			}
		};

		createSessionRunnable.run();
		updatingRunnable.run();
		sendingRunnable.run();

		super.onCreate();
	}

	public class NetworkBinder extends Binder {
		public NetworkService getService() {
			return NetworkService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

}