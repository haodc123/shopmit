package com.example.shopmeet.globals;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Constants {
	public static final String APP_NAME = "Shopmeet";
	public static final int APP_VERSION = 1;

	public static final String TAG_FRG_PER = "tag_frg_personal";
	public static final String TAG_FRG_MESS = "tag_frg_message";
	public static final String TAG_FRG_TASK = "tag_frg_task";
	public static final String TAG_FRG_SETTING = "tag_frg_setting";
	public static final String TAG_FRG_CHAT = "tag_frg_chat";
	public static final String TAG_FRG_VCALL = "tag_frg_vcall";
	public static final String TAG_FRG_CALL = "tag_frg_call";
	public static final String TAG_FRG_GROUP = "tag_frg_group";
	public static final String TAG_FRG_NOTE = "tag_frg_note";
	public static final String TAG_FRG_NOTE_EDIT = "tag_frg_nedt";
	public static final String TAG_FRG_CREATE_GROUP = "tag_frg_create_group";
	public static final String TAG_FRG_TASK_DETAIL = "tag_frg_task_detail";
	public static final String TAG_FRG_CREATE_TASK = "tag_frg_create_task";
	public static final String TAG_FRG_TIMELINE = "tag_frg_timeline";
	public static final String TAG_FRG_COMMENT = "tag_frg_comment";
	public static final String TAG_FRG_COMMENT_EDIT = "tag_frg_cedt";
	public static final String TAG_FRG_STATUS_EDIT = "tag_frg_sedt";
	public static final String TAG_FRG_WHOLIKE = "tag_frg_wholike";
	// Tag fro Log
	public static final String TAG_API = "call_api";
	public static final String TAG_RTC = "---RTC---";
	public static final String TAG_LOGIN = "LoginActivity";
	//public static final String TAG_SCH = "ShopChoosing";
	
	public static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

	/*public final static String KEY_SERVER_NIFTY = "2b831dcbc56340e674beadf9a0935c85878a8a3bd116cc5c517280c98c8744d9";
	public final static String KEY_CLIENT_NIFTY = "d89d6ddb8af57cae74ec8b3e3a1da6f8a1643605ffc62ab7dab16419f7ecb11e";
	public final static String KEY_SENDERID_GCM = "741238669846";*/

	public final static String KEY_SERVER_NIFTY = "d0771cf99cb1a2c1758541e3de752d03b6b7e9dc71ca185cef0817edbb36147a";
	public final static String KEY_CLIENT_NIFTY = "9fef31f0790237ee3c114b7f3297190ae11a93fcf62aa99364fcc222bc7e89a7";

	public static int NOTIFICATION_ID = 1584;

	// For APIs
	public static final String URL_HOST = "http://153.121.44.84/";
	public static final String URL_API_LOGIN = URL_HOST + "api/index/login";
	public static final String URL_API_GET_SHOPS = URL_HOST + "api/store/index";
	public static final String URL_API_GET_CONTACT = URL_HOST + "api/group/index";
	public static final String URL_API_GET_STAFFS_BY_GROUP = URL_HOST + "api/group/staff-of-group";
	public static final String URL_API_CREATE_GROUP = URL_HOST + "api/group/add-group";
	public static final String URL_API_LIST_TASK = URL_HOST + "api/task/index";
	public static final String URL_API_DETAIL_TASK = URL_HOST + "api/task/detail";
	public static final String URL_API_UPDATE_TASK = URL_HOST + "api/task/update-task";
	public static final String URL_API_CREATE_TASK = URL_HOST + "api/task/add-task";
	public static final String URL_API_CHAT_LIST_MSG = URL_HOST + "api/message/chat-detail";
	public static final String URL_API_CHAT_LIST_CONVERSATION = URL_HOST + "api/message/list-chat";
	public static final String URL_API_NOTE_LIST = URL_HOST + "api/note/index";
	public static final String URL_API_NOTE_DEL = URL_HOST + "api/note/delete-note";
	public static final String URL_API_NOTE_ADD = URL_HOST + "api/note/add-note";
	public static final String URL_API_NOTE_DETAIL = URL_HOST + "api/note/detail";
	public static final String URL_API_NOTE_UPDATE = URL_HOST + "api/note/edit-note";
	public static final String URL_API_GET_UNSEEN = URL_HOST + "api/message/unseen";
	public static final String URL_API_STATUS_LIST = URL_HOST + "api/timeline/index";
	public static final String URL_API_STATUS_ADD = URL_HOST + "api/timeline/add-timeline";
	public static final String URL_API_STATUS_LIKE = URL_HOST + "api/timeline/add-timeline-like";
	public static final String URL_API_STATUS_DEL = URL_HOST + "api/timeline/delete-timeline";
	public static final String URL_API_STATUS_DETAIL = URL_HOST + "api/timeline/detail";
	public static final String URL_API_STATUS_DETAIL_PAGING = URL_HOST + "api/timeline/comments";
	public static final String URL_API_STATUS_UPDATE = URL_HOST + "api/timeline/edit-timeline";
	public static final String URL_API_COMMENT_ADD = URL_HOST + "api/timeline/add-timeline-comment";
	public static final String URL_API_COMMENT_DEL = URL_HOST + "api/timeline/delete-timeline-comment";
	public static final String URL_API_COMMENT_UPDATE = URL_HOST + "api/timeline/edit-timeline-comment";
	public static final String URL_API_GET_WHOLIKE = URL_HOST + "api/timeline/staff-likes";
	public static final String URL_API_MARK_MISSEDCALL = URL_HOST + "api/call/missed-save";
	public static final String FOLDER_AVATAR = URL_HOST + "shopmeet/images/avatars/";

	// Error - Alert
	public static final String ERR_JSON = "Data received from server has error";
	public static final String ERR_NO_DATA_FROM_SERVER = "Connection has problem or data has error";
	public static final String ERR_LOGIN = "Login error ";
	public static final String ERR_GETTING = "Getting data error ";
	public static final String ALERT_NO_INTERNET = "No internet connection, please active your connection and try again";
	public static final String ALERT_FILL_LOGIN = "Please type email and password";
	public static final String INFORM_WAIT = "Please wait...";

	// Chat
	public static final String URL_FILE_FOLDER_CHAT = "";

	// Convention
	public static final int TYPE_PERSON = 1;
	public static final int TYPE_GROUP = 0;

	public static final String DATETIME_FORMAT = "yyyy-MM-dd hh:mm:ss";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
}
