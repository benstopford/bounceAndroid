package com.example.helpers;

import static com.example.definitions.Consts.BOUNCES_CLASS_NAME;
import static com.example.definitions.Consts.CONTENT_FIELD_NAME;
import static com.example.definitions.Consts.LIKES_CLASS_NAME;
import static com.example.definitions.Consts.LIKE_BOUNCEID_FIELD_NAME;
import static com.example.definitions.Consts.LIKE_BOUNCE_OWNER_FIELD_NAME;
import static com.example.definitions.Consts.LIKE_OPTION_FIELD_NAME;
import static com.example.definitions.Consts.LIKE_SENDER_FIELD_NAME;
import static com.example.definitions.Consts.LIKE_SENDER_LOGIN_FIELD_NAME;
import static com.example.definitions.Consts.MESSAGE_TYPE_BOUNCE;
import static com.example.definitions.Consts.MESSAGE_TYPE_LIKE;
import static com.example.definitions.Consts.NUMBER_OF_OPTIONS_FIELD_NAME;
import static com.example.definitions.Consts.OWNER_FIELD_NAME;
import static com.example.definitions.Consts.RECEIVERS_FIELD_NAME;
import static com.example.definitions.Consts.TYPE_FIELD_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.example.interfaces.BounceListener;
import com.example.interfaces.BouncesListListener;
import com.example.interfaces.ContactListListener;
import com.example.interfaces.LikeListener;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
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

public class DataHolder {
	private String TAG = "DataHolder";
	private static DataHolder dataHolder;
	private QBUser signedInUser;
	private List<QBUser> contacts;
	private static ArrayList<ContactListListener> contactListListeners;
	private static ArrayList<BouncesListListener> bouncesListListeners;
	private HashMap<String, ArrayList<BounceListener>> bounceListeners;
	private HashMap<String, ArrayList<LikeListener>> likeListeners;
	private HashMap<String, ArrayList<Like>> likes;
	private QBCustomObject contactsObject;
	private ArrayList<Bounce> bounces;

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

	public static synchronized DataHolder getDataHolder() {
		if (dataHolder == null) {
			dataHolder = new DataHolder();
		}
		if (contactListListeners == null)
			contactListListeners = new ArrayList<ContactListListener>();
		return dataHolder;
	}

	private ArrayList<Integer> castToIntFromString(ArrayList<String> arrayList) {
		ArrayList<Integer> array = new ArrayList<Integer>();

		for (int index = 0; index < arrayList.size(); index++)
			array.add(Integer.parseInt(arrayList.get(index)));

		return array;
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

	public void addBounceFromCustomObject(QBCustomObject qbCustomObject) {
		HashMap<String, Object> fields = qbCustomObject.getFields();

		int numberOfOptions = Integer.parseInt(fields.get(
				NUMBER_OF_OPTIONS_FIELD_NAME).toString());
		ArrayList<String> contents = (ArrayList<String>) fields
				.get(CONTENT_FIELD_NAME);
		ArrayList<Integer> types = castToIntFromString((ArrayList<String>) fields
				.get(TYPE_FIELD_NAME));
		ArrayList<Integer> receivers = castToIntFromString((ArrayList<String>) fields
				.get(RECEIVERS_FIELD_NAME));

		int senderId = Integer
				.parseInt(fields.get(OWNER_FIELD_NAME).toString());

		Bounce bounce = new Bounce(senderId, numberOfOptions, types, contents,
				receivers, qbCustomObject.getCustomObjectId());
		Log.d(TAG, bounce.toString());

		if (bounces == null) {
			bounces = new ArrayList<Bounce>();
		}
		bounces.add(bounce);
		notifyBouncesChanged();
		notifyBounceListener(bounce.getBounceId());
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
		ArrayList<Integer> receivers = castToIntFromString((ArrayList<String>) fields
				.get(RECEIVERS_FIELD_NAME));
		Log.d(TAG, qbCustomObject.toString());
		Log.d(TAG, qbCustomObject.getCustomObjectId());

		StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
		userIds.add(receivers.get(0));

		QBEvent event = new QBEvent();
		event.setUserIds(userIds);
		event.setEnvironment(QBEnvironment.DEVELOPMENT);
		event.setNotificationType(QBNotificationType.PUSH);

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("type", MESSAGE_TYPE_BOUNCE);
			jsonObject.put("bounce_id", qbCustomObject.getCustomObjectId()
					.toString());
			jsonObject.put("sender_login", signedInUser.getLogin());
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
			jsonObject.put("sender_login", signedInUser.getLogin());
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

	public void sendBounce(int selfId, int numberOfOptions,
			ArrayList<Integer> types, ArrayList<String> contents,
			ArrayList<Integer> receivers) {

		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(OWNER_FIELD_NAME, selfId);
		fields.put(TYPE_FIELD_NAME, types);
		fields.put(CONTENT_FIELD_NAME, contents);
		fields.put(NUMBER_OF_OPTIONS_FIELD_NAME, numberOfOptions);
		fields.put(RECEIVERS_FIELD_NAME, receivers);

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
					addBounceFromCustomObject(qbCustomObject);
					Log.d("New record: ", qbCustomObject.toString());
					sendBouncePushNotification(qbCustomObject);
				} else {
					Log.e("Errors", result.getErrors().toString());
				}
			}

		});

	}

	public void userLogin(QBUser signInUser) {
		setSignInUser(signInUser);
		loadContacts();
		loadBounces();
	}

	public void setSignInUser(QBUser signInUser) {
		this.signedInUser = signInUser;
	}

	public QBUser getSignInUserId() {
		return signedInUser;
	}

	private void addUsers(ArrayList<String> ss) {
		QBUsers.getUsersByIDs(ss, new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onComplete(Result result) {
				// TODO Auto-generated method stub
				if (result.isSuccess()) {
					QBUserPagedResult results = (QBUserPagedResult) result;
					ArrayList<QBUser> users = results.getUsers();
					contacts = users;
					notifyContactChanged();
				}
			}
		});
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
		for (BouncesListListener listener : bouncesListListeners) {
			listener.onBouncesChanged();
		}
	}

	public int getBouncesSize() {
		if (bounces == null) {
			bounces = new ArrayList<Bounce>();
		}
		return bounces.size();
	}

	public Bounce getBounceAtIndex(int position) {
		return bounces.get(position);
	}

	public int getContactsSize() {
		if (contacts == null) {
			contacts = new ArrayList<QBUser>();
		}
		return contacts.size();
	}

	public QBUser getContactAtIndex(int position) {
		return contacts.get(position);
	}

	public void addContact(QBUser user) {
		if (contactsObject == null) {
			return;
		}
		if (contacts == null) {
			contacts = new ArrayList<QBUser>();
		}
		if (contacts.contains(user)) {
			Log.d("TAG", "trying to add a contact which already exists");
			return;
		}

		contacts.add(user);
		notifyContactChanged();

		HashMap<String, Object> fields = contactsObject.getFields();
		ArrayList<String> ss = (ArrayList<String>) fields.get("list");
		if (ss == null) {
			ss = new ArrayList<String>();
		}

		ss.add(user.getId().toString());

		fields.remove("list"); 
		fields.put("list", ss);
		contactsObject.setFields(fields);

		QBCustomObjects.updateObject(contactsObject, new QBCallback() {

			@Override
			public void onComplete(Result arg0, Object arg1) {

			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					Log.d(TAG, "Contacts object was updated");
				} else {
					Log.e(TAG, "Contacts object was NOT UPDATED!!! ERROR");
				}
			}
		});

	}

	private void addResult(Result result) {
		Log.d(TAG, result.toString());
		QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
		ArrayList<QBCustomObject> co = coresult.getCustomObjects();

		for (int i = 0; i < co.size(); i++) {
			QBCustomObject bounceObject = co.get(i);
			addBounceFromCustomObject(bounceObject);
		}
	}

	public void loadBounces() {

		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.eq("owner", signedInUser.getId());

		QBCustomObjects.getObjects(BOUNCES_CLASS_NAME, requestBuilder,
				new QBCallback() {
					@Override
					public void onComplete(Result arg0, Object arg1) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onComplete(Result result) {
						// TODO Auto-generated method stub
						if (result.isSuccess()) {
							addResult(result);
						} else {
							Log.e(TAG, "on getting owner bounces"
									+ result.getErrors().toString());
						}

					}
				});

		requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.in("receivers", signedInUser.getId());
		QBCustomObjects.getObjects(BOUNCES_CLASS_NAME, requestBuilder,
				new QBCallback() {
					@Override
					public void onComplete(Result arg0, Object arg1) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onComplete(Result result) {
						// TODO Auto-generated method stub
						if (result.isSuccess()) {
							addResult(result);
						} else {
							Log.e(TAG, "on getting receiver bounces"
									+ result.getErrors().toString());
						}
					}
				});
	}

	private void createNewContactObject() {
		QBCustomObject qbCustomObject = new QBCustomObject("Contacts");
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put("owner", signedInUser.getId());
		ArrayList<String> ss = new ArrayList<String>();
		fields.put("list", ss);
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
					Log.d("New contact record: ", qbCustomObject.toString());
					contactsObject = qbCustomObject;
				}
			}
		});

	}

	public void loadContacts() {
		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.eq("owner", signedInUser.getId());

		QBCustomObjects.getObjects("Contacts", requestBuilder,
				new QBCallback() {

					@Override
					public void onComplete(Result result, Object arg1) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onComplete(Result result) {
						// TODO Auto-generated method stub
						if (result.isSuccess()) {
							QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
							ArrayList<QBCustomObject> co = coresult
									.getCustomObjects();
							Log.d(TAG, "On Loading Contacts" + co.size());

							if (co.size() == 0) {
								createNewContactObject();
							}

							for (int i = 0; i < co.size(); i++) {
								contactsObject = co.get(i);
								Object a = co.get(i).getFields().get("list");
								ArrayList<String> ss = (ArrayList<String>) a;
								addUsers(ss);
							}
						} else {
							Log.e("Errors", "on Loading Contacts"
									+ result.getErrors().toString());
						}
					}
				});
	}

	public Bounce getBounceWithId(String bounce_id) {

		ArrayList<String> ids = new ArrayList<String>();
		ids.add(bounce_id);

		if (findBounceWithId(bounce_id) == null) {
			QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
			requestBuilder.eq("_id", bounce_id);

			QBCustomObjects.getObjects(BOUNCES_CLASS_NAME, requestBuilder,
					new QBCallback() {

						@Override
						public void onComplete(Result arg0, Object arg1) {
							// TODO Auto-generated method stub

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

		int sender_id = signedInUser.getId();
		QBCustomObject qbCustomObject = new QBCustomObject(LIKES_CLASS_NAME);

		HashMap<String, Object> fields = new HashMap<String, Object>();

		fields.put(LIKE_BOUNCEID_FIELD_NAME, bounce.getBounceId());
		fields.put(LIKE_SENDER_FIELD_NAME, sender_id);
		fields.put(LIKE_SENDER_LOGIN_FIELD_NAME, signedInUser.getLogin());
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

}
