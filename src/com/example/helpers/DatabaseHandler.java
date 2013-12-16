package com.example.helpers;

import static com.example.helpers.Utils.convertArrayOfIntsToString;
import static com.example.helpers.Utils.convertArrayOfStringToString;
import static com.example.helpers.Utils.convertStringToArrayOfInt;
import static com.example.helpers.Utils.convertStringToArrayOfString;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	private static final String TAG = "DatabaseHandler";

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "databaseManager";

	// Table names
	private static final String TABLE_CONTACTS = "contacts";
	private static final String TABLE_BOUNCES = "bounces";
	private static final String TABLE_PERSONAL = "personal";

	// Personal Table Columns names
	private static final String PERSONAL_KEY_ID = "id";
	private static final String PERSONAL_KEY_USER_ID = "user_id";
	private static final String PERSONAL_KEY_LOGIN = "login";
	private static final String PERSONAL_KEY_NAME = "name";
	private static final String PERSONAL_KEY_PHONE_NUMBER = "phone_number";
	private static final String PERSONAL_KEY_PASSWORD = "password";
	private static final String PERSONAL_KEY_BLOBID = "blobid";
	private static final String PERSONAL_KEY_PROFILE_IMAGE = "profile_image";

	// Contacts Table Columns names
	private static final String CONTACTS_KEY_ID = "id";
	private static final String CONTACTS_KEY_USER_ID = "user_id";
	private static final String CONTACTS_KEY_LOGIN = "login";
	private static final String CONTACTS_KEY_NAME = "name";
	private static final String CONTACTS_KEY_PHONE_NUMBER = "phone_number";
	private static final String CONTACTS_KEY_BLOBID = "blobid";
	private static final String CONTACTS_KEY_PROFILE_IMAGE = "profile_image";
	private static final String CONTACTS_KEY_LAST_UPDATED_AT = "last_updated_at";

	// Bounces Table Columns names
	private static final String BOUNCES_KEY_ID = "id";
	private static final String BOUNCES_KEY_BOUNCE_ID = "bounce_id";
	private static final String BOUNCES_KEY_SENDER_ID = "sender_id";
	private static final String BOUNCES_KEY_NUMBER_OF_OPTIONS = "number_of_options";
	private static final String BOUNCES_KEY_TYPES = "types";
	private static final String BOUNCES_KEY_CONTENTS = "contents";
	private static final String BOUNCES_KEY_RECEIVERS = "receivers";
	private static final String BOUNCES_KEY_ISFROMSELF = "is_from_self";
	private static final String BOUNCES_KEY_QUESTION = "question";
	private static final String BOUNCES_KEY_OPTION_TITLES = "option_titles";
	private static final String BOUNCES_KEY_SEND_AT = "send_at";
	private static final String BOUNCES_KEY_STATUS = "status";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "OnCreate called");
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
				+ CONTACTS_KEY_ID + " INTEGER PRIMARY KEY,"
				+ CONTACTS_KEY_USER_ID + " TEXT," + CONTACTS_KEY_LOGIN
				+ " TEXT," + CONTACTS_KEY_NAME + " TEXT,"
				+ CONTACTS_KEY_PHONE_NUMBER + " TEXT," + CONTACTS_KEY_BLOBID
				+ " INTEGER," + CONTACTS_KEY_PROFILE_IMAGE + " BLOB,"
				+ CONTACTS_KEY_LAST_UPDATED_AT + " INTEGER" + ")";

		db.execSQL(CREATE_CONTACTS_TABLE);

		String CREATE_PERSONAL_TABLE = "CREATE TABLE " + TABLE_PERSONAL + "("
				+ PERSONAL_KEY_ID + " INTEGER PRIMARY KEY,"
				+ PERSONAL_KEY_USER_ID + " TEXT," + PERSONAL_KEY_LOGIN
				+ " TEXT," + PERSONAL_KEY_NAME + " TEXT,"
				+ PERSONAL_KEY_PHONE_NUMBER + " TEXT," + PERSONAL_KEY_PASSWORD
				+ " TEXT," + PERSONAL_KEY_BLOBID + " INTEGER,"
				+ PERSONAL_KEY_PROFILE_IMAGE + " BLOB" + ")";
		db.execSQL(CREATE_PERSONAL_TABLE);

		String CREATE_BOUNCES_TABLE = "CREATE TABLE " + TABLE_BOUNCES + "("
				+ BOUNCES_KEY_ID + " INTEGER PRIMARY KEY,"
				+ BOUNCES_KEY_BOUNCE_ID + " TEXT," + BOUNCES_KEY_SENDER_ID
				+ " INTEGER," + BOUNCES_KEY_NUMBER_OF_OPTIONS + " INTEGER,"
				+ BOUNCES_KEY_TYPES + " TEXT," + BOUNCES_KEY_CONTENTS
				+ " TEXT," + BOUNCES_KEY_RECEIVERS + " TEXT,"
				+ BOUNCES_KEY_ISFROMSELF + " INTEGER," + BOUNCES_KEY_QUESTION
				+ " TEXT," + BOUNCES_KEY_OPTION_TITLES + " TEXT,"
				+ BOUNCES_KEY_SEND_AT + " INTEGER," + BOUNCES_KEY_STATUS
				+ " TEXT" + ")";

		db.execSQL(CREATE_BOUNCES_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOUNCES);
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONAL);
		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new contact
	public void addSelf(Contact contact) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(PERSONAL_KEY_USER_ID, contact.getUserID());
		values.put(PERSONAL_KEY_LOGIN, contact.getLogin());
		values.put(PERSONAL_KEY_NAME, contact.getName());
		values.put(PERSONAL_KEY_PHONE_NUMBER, contact.getPhoneNumber());
		values.put(PERSONAL_KEY_PASSWORD, contact.getPassword());
		values.put(PERSONAL_KEY_BLOBID, contact.getBlobID());
		values.put(PERSONAL_KEY_PROFILE_IMAGE, contact.getProfileImage());

		// Inserting Row
		db.insert(TABLE_PERSONAL, null, values);
		db.close(); // Closing database connection
	}

	public Contact getSelfContact() {
		String selectQuery = "SELECT  * FROM " + TABLE_PERSONAL;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor != null && cursor.moveToFirst()) {
			Contact contact = new Contact(
					Integer.parseInt(cursor.getString(0)), cursor.getInt(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getString(4), cursor.getInt(6), cursor.getBlob(7),
					null);
			contact.setPassword(cursor.getString(5));
			return contact;
		}
		return null;
	}

	public int updateSelfContact(Contact contact) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(PERSONAL_KEY_USER_ID, contact.getUserID());
		values.put(PERSONAL_KEY_LOGIN, contact.getLogin());
		values.put(PERSONAL_KEY_NAME, contact.getName());
		values.put(PERSONAL_KEY_PHONE_NUMBER, contact.getPhoneNumber());
		values.put(PERSONAL_KEY_PASSWORD, contact.getPassword());
		values.put(PERSONAL_KEY_BLOBID, contact.getBlobID());
		values.put(PERSONAL_KEY_PROFILE_IMAGE, contact.getProfileImage());

		// updating row
		return db.update(TABLE_PERSONAL, values, PERSONAL_KEY_ID + " = ?",
				new String[] { String.valueOf(contact.getID()) });
	}

	// Adding new contact
	void addContact(Contact contact) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(CONTACTS_KEY_USER_ID, contact.getUserID());
		values.put(CONTACTS_KEY_LOGIN, contact.getLogin()); // Contact Login
		values.put(CONTACTS_KEY_NAME, contact.getName()); // Contact Name
		values.put(CONTACTS_KEY_PHONE_NUMBER, contact.getPhoneNumber()); // ContactPhone
		values.put(CONTACTS_KEY_BLOBID, contact.getBlobID()); // ContactImageID
		values.put(CONTACTS_KEY_PROFILE_IMAGE, contact.getProfileImage()); // ContactProfileImage
		values.put(CONTACTS_KEY_LAST_UPDATED_AT, contact.getUpdatedAt()
				.getTime()); // "Last time it was updated"

		// Inserting Row
		db.insert(TABLE_CONTACTS, null, values);
		db.close(); // Closing database connection
	}

	// Getting single contact
	Contact getContact(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_CONTACTS, new String[] {
				CONTACTS_KEY_ID, CONTACTS_KEY_USER_ID, CONTACTS_KEY_LOGIN,
				CONTACTS_KEY_NAME, CONTACTS_KEY_PHONE_NUMBER,
				CONTACTS_KEY_BLOBID, CONTACTS_KEY_PROFILE_IMAGE,
				CONTACTS_KEY_LAST_UPDATED_AT }, CONTACTS_KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			Contact contact = new Contact(
					Integer.parseInt(cursor.getString(0)), cursor.getInt(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getString(4), cursor.getInt(5), cursor.getBlob(6),
					new Date(cursor.getLong(7)));
			return contact;
		}
		// return contact
		return null;
	}

	// Getting All Contacts
	public ArrayList<Contact> getAllContacts() {
		ArrayList<Contact> contactList = new ArrayList<Contact>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Contact contact = new Contact();
				contact.setID(Integer.parseInt(cursor.getString(0)));
				contact.setUserID(cursor.getInt(1));
				contact.setLogin(cursor.getString(2));
				contact.setName(cursor.getString(3));
				contact.setPhoneNumber(cursor.getString(4));
				contact.setBlobID(cursor.getInt(5));
				contact.setProfileImage(cursor.getBlob(6));
				contact.setUpdatedAt(new Date(cursor.getLong(7)));
				// Adding contact to list
				contactList.add(contact);
			} while (cursor.moveToNext());
		}

		// return contact list
		return contactList;
	}

	// Updating single contact
	public int updateContact(Contact contact) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(CONTACTS_KEY_USER_ID, contact.getUserID());
		values.put(CONTACTS_KEY_LOGIN, contact.getLogin());
		values.put(CONTACTS_KEY_NAME, contact.getName());
		values.put(CONTACTS_KEY_PHONE_NUMBER, contact.getPhoneNumber());
		values.put(CONTACTS_KEY_BLOBID, contact.getBlobID());
		values.put(CONTACTS_KEY_PROFILE_IMAGE, contact.getProfileImage());
		values.put(CONTACTS_KEY_LAST_UPDATED_AT, contact.getUpdatedAt()
				.getTime());

		// updating row
		return db.update(TABLE_CONTACTS, values, CONTACTS_KEY_ID + " = ?",
				new String[] { String.valueOf(contact.getID()) });
	}

	// Deleting single contact
	public void deleteContact(Contact contact) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CONTACTS, CONTACTS_KEY_ID + " = ?",
				new String[] { String.valueOf(contact.getID()) });
		db.close();
	}

	// Getting contacts Count
	public int getContactsCount() {
		String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}

	// Adding new bounce
	long addBounce(Bounce bounce) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(BOUNCES_KEY_BOUNCE_ID, bounce.getBounceId());
		values.put(BOUNCES_KEY_SENDER_ID, bounce.getSender());
		values.put(BOUNCES_KEY_NUMBER_OF_OPTIONS, bounce.getNumberOfOptions());
		values.put(BOUNCES_KEY_TYPES,
				convertArrayOfIntsToString(bounce.getTypes()));
		values.put(BOUNCES_KEY_CONTENTS,
				convertArrayOfStringToString(bounce.getContents()));
		values.put(BOUNCES_KEY_RECEIVERS,
				convertArrayOfIntsToString(bounce.getReceivers()));
		values.put(BOUNCES_KEY_ISFROMSELF, bounce.getIsFromSelf());
		values.put(BOUNCES_KEY_QUESTION, bounce.getQuestion());
		values.put(BOUNCES_KEY_OPTION_TITLES,
				convertArrayOfStringToString(bounce.getOptionNames()));

		values.put(BOUNCES_KEY_SEND_AT, bounce.getSendAt().getTime());
		values.put(BOUNCES_KEY_STATUS, bounce.getStatus());

		// Inserting Row
		long res = db.insert(TABLE_BOUNCES, null, values);
		db.close(); // Closing database connection
		return res;
	}

	// Getting single bounce
	Bounce getBounce(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_BOUNCES, new String[] { BOUNCES_KEY_ID,
				BOUNCES_KEY_BOUNCE_ID, BOUNCES_KEY_SENDER_ID,
				BOUNCES_KEY_NUMBER_OF_OPTIONS, BOUNCES_KEY_TYPES,
				BOUNCES_KEY_CONTENTS, BOUNCES_KEY_RECEIVERS,
				BOUNCES_KEY_ISFROMSELF, BOUNCES_KEY_QUESTION,
				BOUNCES_KEY_OPTION_TITLES, BOUNCES_KEY_SEND_AT,
				BOUNCES_KEY_STATUS }, BOUNCES_KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor == null)
			return null;

		if (cursor != null)
			cursor.moveToFirst();

		Log.d(TAG, "getting bounce with id : " + id + " and cursor.size = "
				+ cursor.getCount());

		Bounce bounce = new Bounce(cursor.getInt(2),// sender_id
				cursor.getInt(3),// number of options
				convertStringToArrayOfInt(cursor.getString(4)), // types
				convertStringToArrayOfString(cursor.getString(5)), // contents
				convertStringToArrayOfInt(cursor.getString(6)), // receivers
				cursor.getString(1), // bounce_id
				cursor.getLong(0), // ID
				cursor.getInt(7), // isFromSelf
				cursor.getString(8), // question
				convertStringToArrayOfString(cursor.getString(9)), // optionTitles
				new Date(cursor.getLong(10)), // sentAt
				cursor.getString(11) // status
		);
		return bounce;
	}

	// Getting All Contacts
	public ArrayList<Bounce> getAllBounces() {
		ArrayList<Bounce> bounceList = new ArrayList<Bounce>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_BOUNCES + " ORDER BY "
				+ BOUNCES_KEY_SEND_AT + " DESC";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Bounce bounce = new Bounce(cursor.getInt(2),// sender_id
						cursor.getInt(3),// number of options
						convertStringToArrayOfInt(cursor.getString(4)), // types
						convertStringToArrayOfString(cursor.getString(5)), // contents
						convertStringToArrayOfInt(cursor.getString(6)), // receivers
						cursor.getString(1), // bounce_id
						cursor.getLong(0), // ID
						cursor.getInt(7), // isFromSelf
						cursor.getString(8), // question
						convertStringToArrayOfString(cursor.getString(9)), // OptionTitles
						new Date(cursor.getLong(10)), // sentAt
						cursor.getString(11) // status
				);
				bounceList.add(bounce);
			} while (cursor.moveToNext());
		}

		// return contact list
		return bounceList;
	}

	// Updating single contact
	public int updateBounce(Bounce bounce) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(BOUNCES_KEY_BOUNCE_ID, bounce.getBounceId());
		values.put(BOUNCES_KEY_SENDER_ID, bounce.getSender());
		values.put(BOUNCES_KEY_NUMBER_OF_OPTIONS, bounce.getNumberOfOptions());
		values.put(BOUNCES_KEY_TYPES,
				convertArrayOfIntsToString(bounce.getTypes()));
		values.put(BOUNCES_KEY_CONTENTS,
				convertArrayOfStringToString(bounce.getContents()));
		values.put(BOUNCES_KEY_RECEIVERS,
				convertArrayOfIntsToString(bounce.getReceivers()));
		values.put(BOUNCES_KEY_ISFROMSELF, bounce.getIsFromSelf());
		values.put(BOUNCES_KEY_QUESTION, bounce.getQuestion());
		values.put(BOUNCES_KEY_OPTION_TITLES,
				convertArrayOfStringToString(bounce.getOptionNames()));
		values.put(BOUNCES_KEY_SEND_AT, bounce.getSendAt().getTime());
		values.put(BOUNCES_KEY_STATUS, bounce.getStatus());

		// updating row
		return db.update(TABLE_BOUNCES, values, BOUNCES_KEY_ID + " = ?",
				new String[] { String.valueOf(bounce.getID()) });
	}

	// Deleting single contact
	public void deleteBounce(Bounce bounce) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_BOUNCES, BOUNCES_KEY_ID + " = ?",
				new String[] { String.valueOf(bounce.getID()) });
		db.close();
	}

	public void deleteBounceWithID(Integer id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_BOUNCES, BOUNCES_KEY_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}

	// Getting contacts Count
	public int getBouncesCount() {
		String countQuery = "SELECT  * FROM " + TABLE_BOUNCES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();
		// return count
		return cursor.getCount();
	}

}