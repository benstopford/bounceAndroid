package com.picktr.example.helpers;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.picktr.example.definitions.Consts;
import com.picktr.example.interfaces.BouncesListListener;
import com.picktr.example.interfaces.ContactListListener;
import com.picktr.example.interfaces.LikeListener;
import com.picktr.example.interfaces.PersonalUpdatedListener;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

public class DataHolder {
	private static final String TAG = "DataHolder";
	private static DataHolder dataHolder;
	private static DatabaseHandler databaseHandler;
	private static ArrayList<ContactListListener> contactListListeners;
	private static ArrayList<BouncesListListener> bouncesListListeners;
	private static ArrayList<LikeListener> likeListeners;
	private static ArrayList<PersonalUpdatedListener> personalListeners;

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
		return dataHolder;
	}

	public void notifyLikeListener() {
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

		Bounce bounce = new Bounce();
		bounce.setSender(senderId);
		bounce.setNumberOfOptions(numberOfOptions);
		bounce.setReceivers(receivers);
		bounce.setQBID(qbCustomObject.getCustomObjectId());
		bounce.setIsFromSelf(isFromSelf);
		bounce.setQuestion(question);
		bounce.setSendAt(qbCustomObject.getCreatedAt());
		bounce.setStatus(Consts.BOUNCE_STATUS_RECEIVED);
		bounce.setIsSeen(0);

		for (int i = 0; i < numberOfOptions; i++) {
			BounceOption option = new BounceOption();
			option.setTitle(optionTitles.get(i));
			option.setImage(Base64.decode(contents.get(i), Base64.NO_WRAP));
			option.setOptionNumber(i);
			option.setType(types.get(i));
			bounce.addOption(option);
		}
		Log.d(TAG, "on adding bounce " + bounce.getNumberOfOptions() + " "
				+ bounce.getOptions().size());
		databaseHandler.addBounce(bounce);
		notifyBouncesChanged();
	}

	private void addContactById(int senderId) {
		QBUsers.getUser(senderId, new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {
			}

			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					QBUserResult qbUserResult = (QBUserResult) result;
					addContact(qbUserResult.getUser());
				} else {
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

	public Like getLike(Like like) {
		return databaseHandler.getLike(like);
	}

	public void addLike(Like like) {
		databaseHandler.addLike(like);
		notifyLikeListener();
	}

	public void removeLike(Like like) {
		databaseHandler.removeLike(like);
		notifyLikeListener();
	}

	public Integer findNews(String customObjectId) {
		return databaseHandler.findNews(customObjectId);
	}

	public void addNews(String customObjectId) {
		databaseHandler.addNews(customObjectId);
	}

	public ArrayList<Contact> getAllContacts() {
		return databaseHandler.getAllContacts();
	}

}
