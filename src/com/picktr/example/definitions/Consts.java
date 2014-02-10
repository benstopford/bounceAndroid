package com.picktr.example.definitions;

public interface Consts {

	public static final String APP_ID = "4595";
	public static final String AUTH_KEY = "X2kkJRY4G7kCTGY";
	public static final String AUTH_SECRET = "7GRyabp5Tzuz8uh";

	public static final float LIKE_BUTTON_OPACITY = (float) (0.2 * 250.0);

	public static final int CONTENT_TYPE_IMAGE = 0;

	public static final String BOUNCES_CLASS_NAME = "Bounces";

	public static final String OWNER_FIELD_NAME = "owner";
	public static final String TYPE_FIELD_NAME = "type";
	public static final String CONTENT_FIELD_NAME = "content";
	public static final String NUMBER_OF_OPTIONS_FIELD_NAME = "number_of_options";
	public static final String RECEIVERS_FIELD_NAME = "receivers";
	public static final String QUESTION_FIELD_NAME = "question";
	public static final String OPTION_TITLE_FIELD_NAME = "option_title";

	public static final String MESSAGE_TYPE_BOUNCE = "message_type_bounce";
	public static final String MESSAGE_TYPE_LIKE = "message_type_like";
	public static final String MESSAGE_TYPE_SEEN = "message_type_seen";

	public static final String LIKES_CLASS_NAME = "Likes";
	public static final String LIKE_SENDER_FIELD_NAME = "sender_id";
	public static final String LIKE_BOUNCEID_FIELD_NAME = "bounce_id";
	public static final String LIKE_OPTION_FIELD_NAME = "option";
	public static final String LIKE_BOUNCE_OWNER_FIELD_NAME = "bounce_owner";
	public static final String LIKE_TYPE_FIELD_NAME = "type";

	public static final String LIKE_TYPE_LIKE = "like";
	public static final String LIKE_TYPE_DISLIKE = "dislike";

	public static final String BOUNCE_STATUS_DRAFT = "draft";
	public static final String BOUNCE_STATUS_SENT = "sent";
	public static final String BOUNCE_STATUS_RECEIVED = "received";
	public static final String BOUNCE_STATUS_SENDING = "sending";
	public static final String BOUNCE_STATUS_PENDING = "pending";
	public static final String BOUNCE_STATUS_LOADING = "loading";

	public static final String NEWS_CLASS_NAME = "News";
	public static final String NEWS_OWNER_FIELD_NAME = "owner";
	public static final String NEWS_TYPE_FIELD_NAME = "type";
	public static final String NEWS_OBJECT_ID_FIELD_NAME = "object_id";

	public static final String NEWS_TYPE_BOUNCE = "new_bounce";
	public static final String NEWS_TYPE_LIKE = "new_like";
	public static final String NEWS_TYPE_SEEN_BY = "seen_by";

	public static final String SEEN_CLASS_NAME = "SeenBy";
	public static final String SEEN_OWNER_FIELD_NAME = "owner";
	public static final String SEEN_BOUNCE_ID_FIELD_NAME = "bounce_id";
	public static final String SEEN_CONTACTS_FIELD_NAME = "contact";

	public static final int YOUR_SELECT_PICTURE_REQUEST_CODE = 0;
	public static final int PIC_CROP = 1;

	public static final int DRAFT = 1;
	public static final int NOT_DRAFT = 0;

	public static final int FROM_SELF = 1;
	public static final int NOT_FROM_SELF = 0;
	public static final String PERMISSIONS = "permissions";

}
