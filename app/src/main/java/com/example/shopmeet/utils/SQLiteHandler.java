package com.example.shopmeet.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.shopmeet.adapter.CMessageAdapter;
import com.example.shopmeet.adapter.ListGroupAdapter.ListGroupData;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter.ListMemberData;
import com.example.shopmeet.adapter.ListMemberSelectAdapter.ListMemberSelectData;
import com.example.shopmeet.adapter.ListMessAdapter;
import com.example.shopmeet.adapter.ListMessAdapter.ListMessData;
import com.example.shopmeet.adapter.ListTaskAdapter.ListTaskData;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.CMessageData;
import com.example.shopmeet.model.CSender;
import com.example.shopmeet.model.NoteData;
import com.example.shopmeet.model.StatusData;

public class SQLiteHandler extends SQLiteOpenHelper {
 
    private static final String TAG = SQLiteHandler.class.getSimpleName();
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "shopmeet";

    public static final String KEY_ID = "id";
    // User table name
    public static final String TABLE_USER = "user";
    // User Table Columns names
    public static final String KEY_UID = "staff_id";
    public static final String KEY_UNAME = "staff_uname";
    public static final String KEY_UFNAME = "staff_fname";
    public static final String KEY_ULNAME = "staff_lname";
    public static final String KEY_UEMAIL = "staff_email";
    public static final String KEY_UTEL = "staff_tel";
    public static final String KEY_UADDRESS = "staff_address";
    public static final String KEY_UJOINDATE = "staff_joindate";
    public static final String KEY_UTOKEN = "staff_token";
    public static final String KEY_UAVATAR = "staff_avatar";

    // Conversation table name
    public static final String TABLE_CONV = "conv";
    // Conversation Table Columns names
    public static final String KEY_CV_PARTNER_ID = "conv_pn_id";
    public static final String KEY_CV_PARTNER_FNAME = "conv_pn_fname";
    public static final String KEY_CV_PARTNER_LNAME = "conv_pn_lname";
    public static final String KEY_CV_G_NAME = "conv_g_name";
    public static final String KEY_CV_LAST_MSG = "conv_last_msg";
    public static final String KEY_CV_LAST_AUTHOR = "conv_last_author";
    public static final String KEY_CV_LAST_TIME = "conv_last_time";
    public static final String KEY_CV_UNSEEN = "conv_unseen";
    public static final String KEY_CV_IS_PRIVATE = "conv_is_private";
    public static final String KEY_CV_TYPE = "conv_type";

    // Message table name
    public static final String TABLE_MSG = "msg";
    // Message Table Columns names
    public static final String KEY_MSG_ID = "msg_id";
    public static final String KEY_MSG_CONTENT = "msg_content";
    public static final String KEY_MSG_CREATED = "msg_created";
    public static final String KEY_MSG_AUTHOR_ID = "msg_author_id";
    public static final String KEY_MSG_AUTHOR_NAME = "msg_author_name";
    public static final String KEY_MSG_CONTENT_TYPE = "msg_content_type";
    public static final String KEY_MSG_RECEIVE_ID = "msg_receiver"; // if group chat, this is group_id

    // Staff table name
    public static final String TABLE_STAFF = "staff";
    // Staff Table Columns names
    public static final String KEY_STAFF_ID = "staff_id";
    public static final String KEY_STAFF_UNAME = "staff_uname";
    public static final String KEY_STAFF_FNAME = "staff_fname";
    public static final String KEY_STAFF_LNAME = "staff_lname";
    public static final String KEY_STAFF_AVATAR = "staff_avatar";
    public static final String KEY_STAFF_EMAIL = "staff_email";
    public static final String KEY_STAFF_JOINDATE = "staff_joindate";

    // Group table name
    public static final String TABLE_GROUP = "appgroup";
    // Staff Table Columns names
    public static final String KEY_GROUP_ID = "group_id";
    public static final String KEY_GROUP_NAME = "group_name";
    public static final String KEY_GROUP_CREATED = "group_created";
    public static final String KEY_GROUP_STAFF_ARR = "group_staffstring";
    public static final String KEY_GROUP_AVATAR = "group_avatar";

    // Task table name
    public static final String TABLE_TASK = "task";
    // Task Table Columns names
    public static final String KEY_TASK_ID = "task_id";
    public static final String KEY_TASK_STAFFID = "task_staffid";
    public static final String KEY_TASK_CONTENT = "task_content";
    public static final String KEY_TASK_RP_ID = "task_rp_id";
    public static final String KEY_TASK_RP_FNAME = "task_rp_fname";
    public static final String KEY_TASK_RP_LNAME = "task_rp_lname";
    public static final String KEY_TASK_CREATED = "task_created";
    public static final String KEY_TASK_DEADLINE = "task_deadline";
    public static final String KEY_TASK_STATUS = "task_status";
    public static final String KEY_TASK_COMMENT = "task_comment";
    public static final String KEY_TASK_STAFF_ARR = "task_staff_arr";

    // Note table name
    public static final String TABLE_NOTE = "appnote";
    // Note Table Columns names
    public static final String KEY_NOTE_ID = "note_id";
    public static final String KEY_NOTE_STAFFID = "note_staffid";
    public static final String KEY_NOTE_STAFF_FNAME = "note_staff_fname";
    public static final String KEY_NOTE_STAFF_LNAME = "note_staff_lname";
    public static final String KEY_NOTE_CREATED = "note_created";
    public static final String KEY_NOTE_LASTMODIFIED = "note_lastmodified";
    public static final String KEY_NOTE_CONTENT = "note_content";
    public static final String KEY_NOTE_GROUP_ID = "note_groupid";

    // status table name
    public static final String TABLE_STATUS = "appstatus";
    // status Table Columns names
    public static final String KEY_STATUS_ID = "status_id";
    public static final String KEY_STATUS_STAFFID = "status_staffid";
    public static final String KEY_STATUS_STAFF_FNAME = "status_staff_fname";
    public static final String KEY_STATUS_STAFF_LNAME = "status_staff_lname";
    public static final String KEY_STATUS_CREATED = "status_created";
    public static final String KEY_STATUS_LASTMODIFIED = "status_lastmodified";
    public static final String KEY_STATUS_CONTENT = "status_content";
    public static final String KEY_STATUS_NUMLIKE = "status_numlike";
    public static final String KEY_STATUS_NUMCOMMENT = "status_numcomment";
    public static final String KEY_STATUS_ISLIKE = "status_islike";

    private SQLiteDatabase db;

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableUser(db);
        createTableConv(db);
        createTableMsg(db);
        createTableStaff(db);
        createTableGroup(db);
        createTableTask(db);
        createTableNote(db);
        createTableStatus(db);
        Log.d(TAG, "Database tables created");
    }
    private void createTableUser(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_UID + " TEXT,"
                + KEY_UNAME + " TEXT,"
                + KEY_UFNAME + " TEXT,"
                + KEY_ULNAME + " TEXT,"
                + KEY_UEMAIL + " TEXT,"
                + KEY_UTEL + " TEXT,"
                + KEY_UADDRESS + " TEXT,"
                + KEY_UJOINDATE + " TEXT,"
                + KEY_UTOKEN + " TEXT,"
                + KEY_UAVATAR + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);
    }
    private void createTableConv(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_CONV_TABLE = "CREATE TABLE " + TABLE_CONV + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_CV_PARTNER_ID + " TEXT,"
                + KEY_CV_PARTNER_FNAME + " TEXT,"
                + KEY_CV_PARTNER_LNAME + " TEXT,"
                + KEY_CV_G_NAME + " TEXT,"
                + KEY_CV_LAST_MSG + " TEXT,"
                + KEY_CV_LAST_AUTHOR + " TEXT,"
                + KEY_CV_LAST_TIME + " TEXT,"
                + KEY_CV_UNSEEN + " TEXT,"
                + KEY_CV_IS_PRIVATE + " TEXT,"
                + KEY_CV_TYPE + " TEXT" + ")";
        db.execSQL(CREATE_CONV_TABLE);
    }
    private void createTableMsg(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_MSG_TABLE = "CREATE TABLE " + TABLE_MSG + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_MSG_ID + " TEXT,"
                + KEY_MSG_CONTENT + " TEXT,"
                + KEY_MSG_CREATED + " TEXT,"
                + KEY_MSG_AUTHOR_ID + " TEXT,"
                + KEY_MSG_AUTHOR_NAME + " TEXT,"
                + KEY_MSG_CONTENT_TYPE + " TEXT,"
                + KEY_MSG_RECEIVE_ID + " TEXT" + ")";
        db.execSQL(CREATE_MSG_TABLE);
    }
    private void createTableStaff(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_STAFF_TABLE = "CREATE TABLE " + TABLE_STAFF + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_STAFF_ID + " TEXT,"
                + KEY_STAFF_UNAME + " TEXT,"
                + KEY_STAFF_FNAME + " TEXT,"
                + KEY_STAFF_LNAME + " TEXT,"
                + KEY_STAFF_AVATAR + " TEXT,"
                + KEY_STAFF_EMAIL + " TEXT,"
                + KEY_STAFF_JOINDATE + " TEXT" + ")";
        db.execSQL(CREATE_STAFF_TABLE);
    }
    private void createTableGroup(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_GROUP_TABLE = "CREATE TABLE " + TABLE_GROUP + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_GROUP_ID + " TEXT,"
                + KEY_GROUP_NAME + " TEXT,"
                + KEY_GROUP_CREATED + " TEXT,"
                + KEY_GROUP_STAFF_ARR + " TEXT,"
                + KEY_GROUP_AVATAR + " TEXT" + ")";
        db.execSQL(CREATE_GROUP_TABLE);
    }
    private void createTableTask(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_TASK_TABLE = "CREATE TABLE " + TABLE_TASK + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TASK_ID + " TEXT,"
                + KEY_TASK_STAFFID + " TEXT,"
                + KEY_TASK_CONTENT + " TEXT,"
                + KEY_TASK_RP_ID + " TEXT,"
                + KEY_TASK_RP_FNAME + " TEXT,"
                + KEY_TASK_RP_LNAME + " TEXT,"
                + KEY_TASK_CREATED + " TEXT,"
                + KEY_TASK_DEADLINE + " TEXT,"
                + KEY_TASK_STATUS + " TEXT,"
                + KEY_TASK_COMMENT + " TEXT,"
                + KEY_TASK_STAFF_ARR + " TEXT" + ")";
        db.execSQL(CREATE_TASK_TABLE);
    }
    private void createTableNote(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_NOTE_TABLE = "CREATE TABLE " + TABLE_NOTE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NOTE_ID + " TEXT,"
                + KEY_NOTE_STAFFID + " TEXT,"
                + KEY_NOTE_STAFF_FNAME + " TEXT,"
                + KEY_NOTE_STAFF_LNAME + " TEXT,"
                + KEY_NOTE_CREATED + " TEXT,"
                + KEY_NOTE_LASTMODIFIED + " TEXT,"
                + KEY_NOTE_CONTENT + " TEXT,"
                + KEY_NOTE_GROUP_ID + " TEXT" + ")";
        db.execSQL(CREATE_NOTE_TABLE);
    }
    private void createTableStatus(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_STATUS_TABLE = "CREATE TABLE " + TABLE_STATUS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_STATUS_ID + " TEXT,"
                + KEY_STATUS_STAFFID + " TEXT,"
                + KEY_STATUS_STAFF_FNAME + " TEXT,"
                + KEY_STATUS_STAFF_LNAME + " TEXT,"
                + KEY_STATUS_CREATED + " TEXT,"
                + KEY_STATUS_LASTMODIFIED + " TEXT,"
                + KEY_STATUS_CONTENT + " TEXT,"
                + KEY_STATUS_NUMLIKE + " TEXT,"
                + KEY_STATUS_NUMCOMMENT + " TEXT,"
                + KEY_STATUS_ISLIKE + " TEXT" + ")";
        db.execSQL(CREATE_STATUS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * ---------- USER -----------
     * */
    public void addUser(String uid, String username, String fname, String lname, String email, String tel,
                        String address, String joindate, String token, String avatar) {
        db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_UID, uid); // ID
        values.put(KEY_UNAME, username);
        values.put(KEY_UFNAME, fname);
        values.put(KEY_ULNAME, lname);
        values.put(KEY_UEMAIL, email);
        values.put(KEY_UTEL, tel);
        values.put(KEY_UADDRESS, address);
        values.put(KEY_UJOINDATE, joindate);
        values.put(KEY_UTOKEN, token);
        values.put(KEY_UAVATAR, avatar);
 
        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection
 
        Log.d(TAG, "New user inserted into sqlite: " + id);
    }
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_USER;

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(KEY_UID, cursor.getString(1));
            user.put(KEY_UNAME, cursor.getString(2));
            user.put(KEY_UFNAME, cursor.getString(3));
            user.put(KEY_ULNAME, cursor.getString(4));
            user.put(KEY_UEMAIL, cursor.getString(5));
            user.put(KEY_UTEL, cursor.getString(6));
            user.put(KEY_UADDRESS, cursor.getString(7));
            user.put(KEY_UJOINDATE, cursor.getString(8));
            user.put(KEY_UTOKEN, cursor.getString(9));
            user.put(KEY_UAVATAR, cursor.getString(10));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * ---------- CONV ----------
     */
    public void addConv(String pn_id, String pn_fname, String pn_lname, String g_name,
                        String last_msg, String last_author, String last_time, String unseen, String isPrivate, String type) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CV_PARTNER_ID, pn_id);
        values.put(KEY_CV_PARTNER_FNAME, pn_fname);
        values.put(KEY_CV_PARTNER_LNAME, pn_lname);
        values.put(KEY_CV_G_NAME, g_name);
        values.put(KEY_CV_LAST_MSG, last_msg);
        values.put(KEY_CV_LAST_AUTHOR, last_author);
        values.put(KEY_CV_LAST_TIME, last_time);
        values.put(KEY_CV_UNSEEN, unseen);
        values.put(KEY_CV_IS_PRIVATE, isPrivate);
        values.put(KEY_CV_TYPE, type);

        // Inserting Row
        db.insert(TABLE_CONV, null, values);
        Log.d(TAG, "New conv inserted into sqlite ");

        db.close(); // Closing database connection
    }
    public int getListConv(List<ListMessData> TDList, List<ListMessData> PList) {
        // Select All Query
        int numConversationUnSeen = 0;
        String selectQuery = "SELECT  * FROM " + TABLE_CONV;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ListMessData mess = new ListMessData();

                mess.setPartner_id(cursor.getString(1));
                mess.setPartner_fname(cursor.getString(2));
                mess.setPartner_lname(cursor.getString(3));
                mess.setG_name(cursor.getString(4));
                mess.setLast_mess(cursor.getString(5));
                mess.setAuthorid_last_mess(cursor.getString(6));
                mess.setDate_last_mess(cursor.getString(7));
                mess.setPartner_avatar("");
                mess.setUn_seem(Integer.parseInt(cursor.getString(8)));
                mess.setIsPrivate(Integer.parseInt(cursor.getString(9)));
                mess.setType(cursor.getString(10));
                // Adding to list
                if (Functions.getPeriod(cursor.getString(7), "yyyy-MM-dd hh:mm:ss").equalsIgnoreCase("today"))
                    TDList.add(mess);
                else
                    PList.add(mess);
                if (Integer.parseInt(cursor.getString(8)) > 0)
                    numConversationUnSeen++;
            } while (cursor.moveToNext());
        }
        return numConversationUnSeen;
    }
    /*public void updateConv(String last_msg, String last_time, String isRead, String conv_id) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CV_LAST_MSG, last_msg);
        values.put(KEY_CV_LAST_TIME, last_time);
        values.put(KEY_CV_IS_READ, isRead);

        db.update(TABLE_CONV, values, KEY_CV_ID + " = ?",
                new String[]{conv_id});
        Log.d(TAG, "Conv is updated into sqlite, conv_id: " + conv_id);
        db.close(); // Closing database connection
    }
    public HashMap<String, String> getConvDetails(String staff_id1, String staff_id2) {
        HashMap<String, String> conv = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM "+TABLE_CONV+" WHERE "+KEY_CV_STAFF1+" = "+staff_id1+" AND "+KEY_CV_STAFF2+" = "+staff_id2+
                " OR "+ KEY_CV_STAFF1 +" = "+ staff_id2+" AND "+ KEY_CV_STAFF2 +" = "+ staff_id1;

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            conv.put(KEY_CV_ID, cursor.getString(1));
            conv.put(KEY_CV_STAFF1, cursor.getString(2));
            conv.put(KEY_CV_STAFF2, cursor.getString(3));
            conv.put(KEY_CV_IS_PRIVATE, cursor.getString(4));
            conv.put(KEY_CV_LAST_MSG, cursor.getString(5));
            conv.put(KEY_CV_LAST_TIME, cursor.getString(6));
            conv.put(KEY_CV_IS_READ, cursor.getString(7));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching conv from Sqlite: " + conv.toString());

        return conv;
    }*/

    /**
     * ------------ MSG --------------
     */
    public void addMsg(String msg_id, String msg_content, String msg_created,
                       String msg_author_id, String msg_author_name, String msg_content_type, String msg_receive_id) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MSG_ID, msg_id);
        //if (msg_content_type.equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG))
            //values.put(KEY_MSG_CONTENT, Functions.decodeBase64(msg_content));
        //else
            values.put(KEY_MSG_CONTENT, msg_content);
        values.put(KEY_MSG_CREATED, msg_created);
        values.put(KEY_MSG_AUTHOR_ID, msg_author_id);
        values.put(KEY_MSG_AUTHOR_NAME, msg_author_name);
        values.put(KEY_MSG_CONTENT_TYPE, msg_content_type);
        values.put(KEY_MSG_RECEIVE_ID, msg_receive_id);

        // Inserting Row
        long id = db.insert(TABLE_MSG, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New msg inserted into sqlite: " + id);
    }

    public void getAllMsgOfConv(String partner_id, List<CMessageData> msgList) {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MSG + " WHERE "+KEY_MSG_AUTHOR_ID+" = "+partner_id +
                " OR " + KEY_MSG_RECEIVE_ID + " = " + partner_id + " ORDER BY " + KEY_MSG_CREATED + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                CMessageData msg = new CMessageData();
                msg.setId(cursor.getString(1));
                //if (cursor.getString(6).equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG))
                    //msg.setMessage(Functions.decodeBase64(cursor.getString(2)));
                //else
                    msg.setMessage(cursor.getString(2));
                msg.setCreatedAt(cursor.getString(3));
                CSender sender = new CSender(cursor.getString(4), cursor.getString(5), "");
                msg.setContentType(cursor.getString(6));
                msg.setSender(sender);
                // Adding to list
                msgList.add(msg);
            } while (cursor.moveToNext());
        }
    }
    public void delMsgByPartnerId(String partner_id) {
        db.delete(TABLE_MSG, KEY_MSG_AUTHOR_ID + " = " + partner_id + " OR " + KEY_MSG_RECEIVE_ID + " = " + partner_id, null);
    }

    /**
     *  ----------- STAFF --------------
     */
    public void addStaff(String staff_id, String staff_uname, String staff_fname,
                         String staff_lname, String staff_avatar, String staff_email, String staff_joindate) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STAFF_ID, staff_id);
        values.put(KEY_STAFF_UNAME, staff_uname);
        values.put(KEY_STAFF_FNAME, staff_fname);
        values.put(KEY_STAFF_LNAME, staff_lname);
        values.put(KEY_STAFF_AVATAR, staff_avatar);
        values.put(KEY_STAFF_EMAIL, staff_email);
        values.put(KEY_STAFF_JOINDATE, staff_joindate);

        // Inserting Row
        long id = db.insert(TABLE_STAFF, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New staff inserted into sqlite: " + id);
    }
    public HashMap<String, String> getStaffById(String staff_id) {
        HashMap<String, String> conv = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM "+TABLE_STAFF+" WHERE "+KEY_STAFF_ID+" = "+staff_id;

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            conv.put(KEY_STAFF_ID, cursor.getString(1));
            conv.put(KEY_STAFF_UNAME, cursor.getString(2));
            conv.put(KEY_STAFF_FNAME, cursor.getString(3));
            conv.put(KEY_STAFF_LNAME, cursor.getString(4));
            conv.put(KEY_STAFF_AVATAR, cursor.getString(5));
            conv.put(KEY_STAFF_EMAIL, cursor.getString(6));
            conv.put(KEY_STAFF_JOINDATE, cursor.getString(7));
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Fetching staff from Sqlite: " + conv.toString());

        return conv;
    }
    public void getAllStaff(List<ListMemberData> mbList) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_STAFF;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ListMemberData mb = new ListMemberData();
                mb.setMb_id(cursor.getString(1));
                mb.setMb_uname(cursor.getString(2));
                mb.setMb_fname(cursor.getString(3));
                mb.setMb_lname(cursor.getString(4));
                mb.setUrl_avatar(cursor.getString(5));
                mb.setMb_email(cursor.getString(6));
                mb.setMb_joindate(cursor.getString(7));
                mb.setIsDisplay(1);
                // Adding to list
                mbList.add(mb);
            } while (cursor.moveToNext());
        }
    }
    public void getSomeStaff(List<ListMemberData> mbList, String staff_arr) {
        // Select All Query
        staff_arr = staff_arr.replace(",", "', '");
        staff_arr += "'";
        staff_arr = "'"+staff_arr;
        String selectQuery = "SELECT * FROM " + TABLE_STAFF + " WHERE " + KEY_STAFF_ID + " IN (" + staff_arr + ")";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ListMemberData mb = new ListMemberData();
                mb.setMb_id(cursor.getString(1));
                mb.setMb_uname(cursor.getString(2));
                mb.setMb_fname(cursor.getString(3));
                mb.setMb_lname(cursor.getString(4));
                mb.setUrl_avatar(cursor.getString(5));
                mb.setMb_email(cursor.getString(6));
                mb.setMb_joindate(cursor.getString(7));
                mb.setIsDisplay(1);
                // Adding to list
                mbList.add(mb);
            } while (cursor.moveToNext());
        }
    }

    /**
     * ---------------- GROUP --------------
     */
    public void getAllGroup(List<ListGroupData> gList) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_GROUP;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ListGroupData g = new ListGroupData();
                g.setGroup_id(cursor.getString(1));
                g.setGroup_name(cursor.getString(2));
                g.setGroup_create_at(cursor.getString(3));
                g.setStaffs_string(cursor.getString(4));
                g.setGroup_avatar(cursor.getString(5));
                g.setIsDisplay(1);
                // Adding to list
                gList.add(g);
            } while (cursor.moveToNext());
        }
    }
    public HashMap<String, String> getGroupDetails(String group_id) {
        HashMap<String, String> conv = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM "+TABLE_GROUP+" WHERE "+KEY_GROUP_ID+" = "+group_id;

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            conv.put(KEY_GROUP_ID, cursor.getString(1));
            conv.put(KEY_GROUP_NAME, cursor.getString(2));
            conv.put(KEY_GROUP_CREATED, cursor.getString(3));
            conv.put(KEY_GROUP_STAFF_ARR, cursor.getString(4));
            conv.put(KEY_GROUP_AVATAR, cursor.getString(5));
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Fetching group from Sqlite: " + conv.toString());

        return conv;
    }
    public void addGroup(String group_id, String group_uname, String group_created, String staffstring, String group_avatar) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_GROUP_ID, group_id);
        values.put(KEY_GROUP_NAME, group_uname);
        values.put(KEY_GROUP_CREATED, group_created);
        values.put(KEY_GROUP_STAFF_ARR, staffstring);
        values.put(KEY_GROUP_AVATAR, group_avatar);
        // Inserting Row
        long id = db.insert(TABLE_GROUP, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New group inserted into sqlite: " + id);
    }
    public void addGroupIfNotExist(String group_id, String group_uname, String group_created, String staffstring, String group_avatar) {

        db = this.getWritableDatabase();
        // get all group to check if group_id is already exist
        int id = 0; // this is id of group_id if already exist
        String selectQuery = "SELECT * FROM " + TABLE_GROUP;
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows
        if (cursor.moveToFirst()) {
            do {
                String group_id_saved = cursor.getString(1);
                if ((group_id_saved.equalsIgnoreCase(group_id))) {
                    id = Integer.parseInt(group_id_saved);
                    break;
                }
            } while (cursor.moveToNext());
        }

        ContentValues values = new ContentValues();
        if (id == 0) { // not exist
            values.put(KEY_GROUP_ID, group_id);
            values.put(KEY_GROUP_NAME, group_uname);
            values.put(KEY_GROUP_CREATED, group_created);
            values.put(KEY_GROUP_STAFF_ARR, staffstring);
            values.put(KEY_GROUP_AVATAR, group_avatar);
            // Inserting Row
            db.insert(TABLE_GROUP, null, values);

            Log.d(TAG, "New group inserted into sqlite: " + id);

        } else { // already exist, update
            values.put(KEY_GROUP_NAME, group_uname);
            values.put(KEY_GROUP_CREATED, group_created);
            values.put(KEY_GROUP_STAFF_ARR, staffstring);
            values.put(KEY_GROUP_AVATAR, group_avatar);

            db.update(TABLE_GROUP, values, KEY_ID + " = ?",
                    new String[]{String.valueOf(id)});
            Log.d(TAG, "Group is updated into sqlite: " + id);
        }
        db.close(); // Closing database connection
    }

    /**
     * Delete group if it NOT in list from @group_arr
     * @param group_arr: ex: 12, 14, 2  --> list group_id
     */
    public void delSomeGroup(String group_arr) {
        group_arr = group_arr.replace(",", "', '");
        group_arr += "'";
        group_arr = "'"+group_arr;
        db.delete(TABLE_GROUP, KEY_GROUP_ID + " NOT IN (" + group_arr + ")", null);
    }
    public void updateGroup(String group_id, String staff_arr) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_GROUP_STAFF_ARR, staff_arr);

        db.update(TABLE_GROUP, values, KEY_GROUP_ID + " = ?",
                new String[]{String.valueOf(group_id)});
        Log.d(TAG, "Group is updated into sqlite: " + group_id);

        db.close(); // Closing database connection
    }

    /**
     * ----------------- TASK ------------------
     */
    public void addTask(String task_id, String task_staff_id, String task_content, String task_require_person_id,
                        String task_require_person_fname, String task_require_person_lname, String task_create_at,
                        String task_deadline, String task_status, String comment, String staff_arr) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_ID, task_id);
        values.put(KEY_TASK_STAFFID, task_staff_id);
        values.put(KEY_TASK_CONTENT, task_content);
        values.put(KEY_TASK_RP_ID, task_require_person_id);
        values.put(KEY_TASK_RP_FNAME, task_require_person_fname);
        values.put(KEY_TASK_RP_LNAME, task_require_person_lname);
        values.put(KEY_TASK_CREATED, task_create_at);
        values.put(KEY_TASK_DEADLINE, task_deadline);
        values.put(KEY_TASK_STATUS, task_status);
        values.put(KEY_TASK_COMMENT, comment);
        values.put(KEY_TASK_STAFF_ARR, staff_arr);

        // Inserting Row
        long id = db.insert(TABLE_TASK, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New task inserted into sqlite: " + id);
    }
    public void updateTask(String task_staff_id, String status, String comment, String staff_arr) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_TASK_STATUS, status);
        values.put(KEY_TASK_COMMENT, comment);
        values.put(KEY_TASK_STAFF_ARR, staff_arr);

        db.update(TABLE_TASK, values, KEY_TASK_STAFFID + " = ?",
                new String[]{String.valueOf(task_staff_id)});
        Log.d(TAG, "Task is updated into sqlite: " + task_staff_id);

        db.close(); // Closing database connection
    }
    public void updateTask2(String task_staff_id, String status, String comment) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_TASK_STATUS, status);
        values.put(KEY_TASK_COMMENT, comment);

        db.update(TABLE_TASK, values, KEY_TASK_STAFFID + " = ?",
                new String[]{String.valueOf(task_staff_id)});
        Log.d(TAG, "Task is updated into sqlite: " + task_staff_id);

        db.close(); // Closing database connection
    }
    public void getPeriodTask(List<ListTaskData> tList, String period) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TASK;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if (period.equalsIgnoreCase("today")) {
                    if (!Functions.getPeriod(cursor.getString(7), "yyyy-MM-dd hh:mm:ss").equalsIgnoreCase("today")) {
                        return;
                    }
                } else if (period.equalsIgnoreCase("week")) {
                    if (!Functions.getPeriod(cursor.getString(7), "yyyy-MM-dd hh:mm:ss").equalsIgnoreCase("today") &&
                            !Functions.getPeriod(cursor.getString(7), "yyyy-MM-dd hh:mm:ss").equalsIgnoreCase("week")) {
                        return;
                    }
                }
                ListTaskData t = new ListTaskData();
                t.setTask_id(cursor.getString(1));
                t.setTask_staff_id(cursor.getString(2));
                t.setTask_content(cursor.getString(3));
                t.setTask_require_person_id(cursor.getString(4));
                t.setTask_require_person_fname(cursor.getString(5));
                t.setTask_require_person_lname(cursor.getString(6));
                t.setTask_create_at(cursor.getString(7));
                t.setTask_deadline(cursor.getString(8));
                t.setTask_status(Integer.parseInt(cursor.getString(9)));
                // Adding to list
                tList.add(t);
            } while (cursor.moveToNext());
        }
    }
    public HashMap<String, String> getTaskDetails(String task_staff_id) {
        HashMap<String, String> task = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_TASK + " WHERE " + KEY_TASK_STAFFID + " = " + task_staff_id;

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            task.put(KEY_TASK_ID, cursor.getString(1));
            task.put(KEY_TASK_STAFFID, cursor.getString(2));
            task.put(KEY_TASK_CONTENT, cursor.getString(3));
            task.put(KEY_TASK_RP_ID, cursor.getString(4));
            task.put(KEY_TASK_RP_FNAME, cursor.getString(5));
            task.put(KEY_TASK_RP_LNAME, cursor.getString(6));
            task.put(KEY_TASK_CREATED, cursor.getString(7));
            task.put(KEY_TASK_DEADLINE, cursor.getString(8));
            task.put(KEY_TASK_STATUS, cursor.getString(9));
            task.put(KEY_TASK_COMMENT, cursor.getString(10));
            task.put(KEY_TASK_STAFF_ARR, cursor.getString(11));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching task from Sqlite: " + task.toString());

        return task;
    }

    /**
     * ----------------- NOTE ------------------
     */
    public void addNote(String note_id, String staff_id, String staff_fname, String staff_lname,
                        String note_created, String note_lastmodified, String note_content, String group_id) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_ID, note_id);
        values.put(KEY_NOTE_STAFFID, staff_id);
        values.put(KEY_NOTE_STAFF_FNAME, staff_fname);
        values.put(KEY_NOTE_STAFF_LNAME, staff_lname);
        values.put(KEY_NOTE_CREATED, note_created);
        values.put(KEY_NOTE_LASTMODIFIED, note_lastmodified);
        values.put(KEY_NOTE_CONTENT, note_content);
        values.put(KEY_NOTE_GROUP_ID, group_id);

        // Inserting Row
        long id = db.insert(TABLE_NOTE, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New note inserted into sqlite: " + id);
    }
    public void getAllNote(List<NoteData> nList) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NOTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NoteData n = new NoteData();
                n.setNote_id(cursor.getString(1));
                n.setStaff_id(cursor.getString(2));
                n.setStaff_fname(cursor.getString(3));
                n.setStaff_lname(cursor.getString(4));
                n.setStaff_avatar("");
                n.setNote_created(cursor.getString(5));
                n.setNote_modified(cursor.getString(6));
                n.setNote_content(cursor.getString(7));
                n.setIsDisplay(1);
                // Adding to list
                nList.add(n);
            } while (cursor.moveToNext());
        }
    }
    public void getNoteByGroup(List<NoteData> nList, String group_id) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NOTE + " WHERE " + KEY_NOTE_GROUP_ID + " = " + group_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NoteData n = new NoteData();
                n.setNote_id(cursor.getString(1));
                n.setStaff_id(cursor.getString(2));
                n.setStaff_fname(cursor.getString(3));
                n.setStaff_lname(cursor.getString(4));
                n.setStaff_avatar("");
                n.setNote_created(cursor.getString(5));
                n.setNote_modified(cursor.getString(6));
                n.setNote_content(cursor.getString(7));
                n.setIsDisplay(1);
                // Adding to list
                nList.add(n);
            } while (cursor.moveToNext());
        }
    }
    public HashMap<String, String> getNoteDetails(String note_id) {
        HashMap<String, String> note = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTE + " WHERE " + KEY_NOTE_ID + " = " + note_id;

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            note.put(KEY_NOTE_ID, cursor.getString(1));
            note.put(KEY_NOTE_STAFFID, cursor.getString(2));
            note.put(KEY_NOTE_STAFF_FNAME, cursor.getString(3));
            note.put(KEY_NOTE_STAFF_LNAME, cursor.getString(4));
            note.put(KEY_NOTE_CREATED, cursor.getString(5));
            note.put(KEY_NOTE_LASTMODIFIED, cursor.getString(6));
            note.put(KEY_NOTE_CONTENT, cursor.getString(7));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching note from Sqlite: " + note.toString());

        return note;
    }
    public void delNoteById(String note_id) {
        db.delete(TABLE_NOTE, KEY_NOTE_ID + " = " + note_id, null);
    }
    public void updateNote(String note_id, String content) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NOTE_CONTENT, content);

        db.update(TABLE_NOTE, values, KEY_NOTE_ID + " = ?",
                new String[]{String.valueOf(note_id)});
        Log.d(TAG, "Note is updated into sqlite: " + note_id);

        db.close(); // Closing database connection
    }

    /**
     * ----------------- STATUS ------------------
     */
    public void addStatus(String status_id, String staff_id, String staff_fname, String staff_lname,
                          String status_created, String status_lastmodified, String status_content,
                          String numlike, String numcomment, String is_like) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STATUS_ID, status_id);
        values.put(KEY_STATUS_STAFFID, staff_id);
        values.put(KEY_STATUS_STAFF_FNAME, staff_fname);
        values.put(KEY_STATUS_STAFF_LNAME, staff_lname);
        values.put(KEY_STATUS_CREATED, status_created);
        values.put(KEY_STATUS_LASTMODIFIED, status_lastmodified);
        values.put(KEY_STATUS_CONTENT, status_content);
        values.put(KEY_STATUS_NUMLIKE, numlike);
        values.put(KEY_STATUS_NUMCOMMENT, numcomment);
        values.put(KEY_STATUS_ISLIKE, is_like);

        // Inserting Row
        long id = db.insert(TABLE_STATUS, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New status inserted into sqlite: " + id);
    }
    public void getAllStatus(List<StatusData> sList) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_STATUS + " ORDER BY " + KEY_STATUS_CREATED + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                StatusData n = new StatusData();
                n.setStatus_id(cursor.getString(1));
                n.setStaff_id(cursor.getString(2));
                n.setStaff_fname(cursor.getString(3));
                n.setStaff_lname(cursor.getString(4));
                n.setStaff_avatar("");
                n.setStatus_created(cursor.getString(5));
                n.setStatus_modified(cursor.getString(6));
                n.setStatus_content(cursor.getString(7));
                n.setNumLike(Integer.parseInt(cursor.getString(8)));
                n.setNumComment(Integer.parseInt(cursor.getString(9)));
                n.setIsLike(Integer.parseInt(cursor.getString(10)));
                n.setIsDisplay(1);
                // Adding to list
                sList.add(n);
            } while (cursor.moveToNext());
        }
    }
    public void updateStatusLike(String status_id, String like) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_STATUS_ISLIKE, like);

        db.update(TABLE_STATUS, values, KEY_STATUS_ID + " = ?",
                new String[]{String.valueOf(status_id)});
        Log.d(TAG, "Status is updated into sqlite: " + status_id);

        db.close(); // Closing database connection
    }
    public HashMap<String, String> getStatusDetails(String status_id) {
        HashMap<String, String> note = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_STATUS + " WHERE " + KEY_STATUS_ID + " = " + status_id;

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            note.put(KEY_STATUS_ID, cursor.getString(1));
            note.put(KEY_STATUS_STAFFID, cursor.getString(2));
            note.put(KEY_STATUS_STAFF_FNAME, cursor.getString(3));
            note.put(KEY_STATUS_STAFF_LNAME, cursor.getString(4));
            note.put(KEY_STATUS_CREATED, cursor.getString(5));
            note.put(KEY_STATUS_LASTMODIFIED, cursor.getString(6));
            note.put(KEY_STATUS_CONTENT, cursor.getString(7));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching status from Sqlite: " + note.toString());

        return note;
    }
    public void delStatusById(String status_id) {
        db.delete(TABLE_STATUS, KEY_STATUS_ID + " = " + status_id, null);
    }



    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();
 
        Log.d(TAG, "Deleted all user info from sqlite");
    }
    public void deleteStaffs() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_STAFF, null, null);
        db.close();

        Log.d(TAG, "Deleted all staff info from sqlite");
    }
    public void deleteGroups() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_GROUP, null, null);
        db.close();

        Log.d(TAG, "Deleted all group info from sqlite");
    }
    public void deleteConv() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_CONV, null, null);
        db.close();

        Log.d(TAG, "Deleted all staff info from sqlite");
    }
    public void deleteMsg() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_MSG, null, null);
        db.close();

        Log.d(TAG, "Deleted all group info from sqlite");
    }
    public void deleteTask() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_TASK, null, null);
        db.close();

        Log.d(TAG, "Deleted all task info from sqlite");
    }
    public void deleteNote() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_NOTE, null, null);
        db.close();

        Log.d(TAG, "Deleted all note info from sqlite");
    }
    public void deleteStatus() {
        db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_STATUS, null, null);
        db.close();

        Log.d(TAG, "Deleted all status info from sqlite");
    }

}
