package com.example.shopmeet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopmeet.fragments.Frg_Mess;
import com.example.shopmeet.fragments.Frg_Personal;
import com.example.shopmeet.fragments.Frg_Setting;
import com.example.shopmeet.fragments.Frg_Task;
import com.example.shopmeet.fragments.Frg_Timeline;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.CMessageData;
import com.example.shopmeet.receiver.NetworkStateReceiver;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.SQLiteHandler;
import com.nifty.cloud.mb.core.NCMB;
/*import com.nifty.cloud.mb.NCMB;
import com.nifty.cloud.mb.NCMBAnalytics;
import com.nifty.cloud.mb.NCMBException;
import com.nifty.cloud.mb.NCMBInstallation;
import com.nifty.cloud.mb.NCMBPush;
import com.nifty.cloud.mb.NCMBQuery;
import com.nifty.cloud.mb.RegistrationCallback;*/

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.constant.Response;
import jp.chat.video.android.listeners.IOListener;
import jp.chat.video.android.manager.IOManager;
import jp.chat.video.android.manager.ViewManager;
import jp.chat.video.android.models.Grpaudio;
import jp.chat.video.android.socket.SMException;

public class MainActivity extends Activity implements IOListener {

    // Action bar
    private TextView tvTitle;
    public static Button bt_m_back;
    // Icon tab + indicator
    private LinearLayout ll_tab_per, ll_tab_mess, ll_tab_timeline, ll_tab_task, ll_tab_menu2;
    public static ImageView img_tab_per_idc, img_tab_mess_idc, img_tab_timeline_idc, img_tab_task_idc, img_tab_menu2_idc, img_tab_mess;
    public static LinearLayout ll_indicator, ll_icon_tab;
    // Action bar chat
    private TextView tv_c_name;
    private Button bt_c_back, bt_c_menu;

    public static FrameLayout fragment_container;

    private SQLiteHandler db;
    private CallAPIHandler call;
    private HashMap<String, String> staffSaved;
    private HashMap<String, String> groupSaved;

    // RTC variable
    private int callDirection = 0;
    private final static int CALL_DIRECTION_SEND = 1;
    private final static int CALL_DIRECTION_RECEIVE = 2;
    private String callStatus = "";
    private final static String CALL_STATUS_DIALING = "Dialing...";
    private final static String CALL_STATUS_CALLING = "Calling...";
    private final static String CALL_STATUS_INCOMING_CALL = "Incoming Call...";
    private final static long CALL_WAITING_TIMEOUT = 30 * 1000;
    /**
     * isResumeWhileCalling là biến để xác định app đã bị pause - resume trong khi call hay chưa
     * Nếu đã bị (ví dụ khi đang call, user nhấn giữ nút menu, sau đó vào lại app) --> ngăn hàm onResume ở các Fragment
     */
    public static int isResumeWhileCalling = 0;

    private View rtcView, view_bg;
    private TextView tv_rtc_name, tv_rtc_status;
    private GLSurfaceView vsv;
    private static AlertDialog alertDialog;
    // private View btnStop, btnStop2;
    private Button btn_rtc_decline, btn_rtc_endcall, btn_rtc_answer;
    private Button btn_rtc_sound, btn_rtc_video, btn_rtc_camera;
    private boolean call_video;
    private Ringtone mRingtone = null;
    public static LocalRtcListener mLocalRtcListener;
    public interface LocalRtcListener {
        void onLocalReceive_private_message(JSONObject data);
        void onLocalReceive_group_message(JSONObject data);
        void onLocalNetworkChange();
        void onLocalOn_user_online(JSONObject data);
    }
    private String callPartnerID;
    private String callPartnerName;
    private String callPartnerEmail;
    // End RTC variable

    private NetworkStateReceiver mNetworkStateCallback;
    @Override
    public void onAttachFragment(Fragment frg) {
        super.onAttachFragment(frg);
        try {
            mLocalRtcListener = (LocalRtcListener) frg;
        } catch (ClassCastException e) {
            throw new ClassCastException(frg.toString() + " must implement LocalRtcListener");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setInitView();

        initVariables();
        setInitFragments();
        //setInitNifty();
        this.activateRtc();
        this.setInitIOManager();
    }

    private synchronized void activateRtc() {
        vsv = (GLSurfaceView) findViewById(R.id.glview_call);
        ViewManager.setupRtcView(this.vsv);
    }
    private synchronized void setInitIOManager() {
        String http = getString(R.string.host);
        IOManager.instance(getApplicationContext(),
                vsv,
                MainActivity.this,
                Integer.parseInt(Variables.userID),
                http,
                Variables.userFName + " " + Variables.userLName,
                Variables.userEmail);
    }
    @Override
    public void on_user_online(JSONObject msg) {
        Log.d(Constants.TAG_RTC, msg.toString());
        final JSONObject data = msg;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLocalRtcListener.onLocalOn_user_online(data);
                if (callStatus.equalsIgnoreCase(CALL_STATUS_INCOMING_CALL)) {
                    try {
                        String user_id = data.getString("user_id");
                        String status = data.getString("status");
                        if (callPartnerID.equalsIgnoreCase(user_id) && status.equalsIgnoreCase("off")) {
                            onDecline();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Log.e(Constants.TAG_API, data.toString() + Constants.ERR_JSON + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onReceive_private_message(JSONObject msg) {
        Log.d(Constants.TAG_RTC, msg.toString());
        final JSONObject data = msg;
        if(!validateSender(data))
            return;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    /*if (Variables.curFrg == Constants.TAG_FRG_CHAT) {
                        if (frgChat == null)
                            frgChat = (Frg_Chat) getFragmentManager().findFragmentByTag(Constants.TAG_FRG_CHAT);
                        //frgChat.updatePrivateMsg(content, created, type, uniqueId, last_id, saved_id, author_id, friend_id, data.toString());
                        frgChat.updatePrivateMsg2(data);
                    }*/
                if (Variables.curFrg == Constants.TAG_FRG_CHAT || Variables.curFrg == Constants.TAG_FRG_MESS) {
                    mLocalRtcListener.onLocalReceive_private_message(data);
                } else {
                    try {
                        String type = data.getString("type"); // Content type
                        if (type.equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG))
                            Functions.toastString(Functions.getResString(getBaseContext(), R.string.alert_message_received), getBaseContext());
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Log.e(Constants.TAG_API, data.toString() + Constants.ERR_JSON + e.getMessage());
                    }
                    prepareGetUnSeen(Variables.userToken);
                }
            }
        });
    }

    @Override
    public void onReceive_group_message(JSONObject msg) {
        Log.d(Constants.TAG_RTC, msg.toString());
        final JSONObject data = msg;
        if(!validateGroup(data))
            return;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Variables.curFrg == Constants.TAG_FRG_CHAT || Variables.curFrg == Constants.TAG_FRG_MESS) {
                    mLocalRtcListener.onLocalReceive_group_message(data);
                } else {
                    try {
                        String type = data.getString("type"); // Content type
                        if (type.equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG))
                            Functions.toastString(Functions.getResString(getBaseContext(), R.string.alert_message_received), getBaseContext());
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Log.e(Constants.TAG_API, data.toString() + Constants.ERR_JSON + e.getMessage());
                    }
                    prepareGetUnSeen(Variables.userToken);
                }
            }
        });
    }
    public boolean validateSender(JSONObject jo) {
        try {
            String author_id = String.valueOf(jo.getInt("author_id"));
            if (author_id.equalsIgnoreCase(Variables.userID)) {
                return true;
            }
            staffSaved.clear();
            staffSaved = db.getStaffById(author_id);
            if (!staffSaved.isEmpty()) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG_RTC, jo.toString() + Constants.ERR_JSON + e.getMessage());
        }
        return false;
    }

    public boolean validateGroup(JSONObject jo) {
        try {
            String group_id = String.valueOf(jo.getInt("group_id"));
            groupSaved.clear();
            groupSaved = db.getGroupDetails(group_id);
            if (!groupSaved.isEmpty()) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG_RTC, jo.toString() + Constants.ERR_JSON + e.getMessage());
        }
        return false;
    }

    @Override
    public void onAudioGroupOpened(int i) {

    }

    @Override
    public void onAudioGroupClosed(int i) {

    }

    @Override
    public void onUserJoinGroup(int i, List<Grpaudio> list) {

    }

    @Override
    public void onUserLeaveGroup(int i, int i1, String s, long l) {

    }

    @Override
    public synchronized void onUserNotAnswer(int userID) {
        final int user_id = userID;
        Log.e(Constants.TAG_RTC, "onUserNotAnswer=>" + user_id);


        this.hide_alert();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), "Not anwser", Toast.LENGTH_LONG).show();
            }
        });
        prepareMissedCall(Variables.userToken, callPartnerID);
        callDirection = 0;
        callStatus = "";
    }
    private void prepareMissedCall(String token, String partner_id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("id_to", partner_id);
        new RequestServerMissedCall().execute(Constants.URL_API_MARK_MISSEDCALL, call.createQueryStringForParameters(params));
    }
    private class RequestServerMissedCall extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            // Making a request to url and getting response
            String jsonStr = call.requestPOST(arg0[0], arg0[1]);

            Log.d("---Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                return jsonStr;
            } else {
                Log.e(Constants.TAG_API, Constants.ERR_NO_DATA_FROM_SERVER);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonStr) {
            super.onPostExecute(jsonStr);
            // Dismiss the progress dialog
            if (jsonStr == null) {
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, getBaseContext());
                return;
            }
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    Log.d("---", "saved missed call");

                } else {
                    JSONObject jOObjects = jObj.getJSONObject("objects");
                    int is_work = jOObjects.getInt("is_work");
                    if (is_work == 0) {

                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_GETTING + errorMsg);
                    }

                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
            }
        }
    }
    @Override
    public synchronized void onCallRejectByUser(JSONObject msg) {
        Log.e(Constants.TAG_RTC, "onCallRejectByUser=>" + msg.toString());

        this.hide_alert();
        showRmsg("onCallRejectByUser", msg);
        callDirection = 0;
        callStatus = "";
        showTabScreenFromWorker(0);
        prepareMissedCall(Variables.userToken, callPartnerID);
    }

    @Override
    public synchronized void onCallAcceptedByUser(JSONObject msg) {
        Log.e(Constants.TAG_RTC, "onCallAcceptedByUser=>" + msg.toString());

        this.hide_alert();
        showRmsg("onCallAcceptedByUser", msg);
        callDirection = CALL_DIRECTION_SEND;
        callStatus = CALL_STATUS_CALLING;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view_bg.setVisibility(View.GONE);
                tv_rtc_status.setText(callStatus);
                setVisibleToggleButton(true);
            }
        });
    }

    @Override
    public synchronized void onReceiveHangDownFromUser(JSONObject msg) {
        Log.e(Constants.TAG_RTC, "onReceiveHangDownFromUser=>" + msg.toString());

        this.hide_alert();
        showRmsg("onReceiveHangDownFromUser", msg);
        callDirection = 0;
        callStatus = "";
        showTabScreenFromWorker(0);
    }


    @Override
    public synchronized void onReceiveCallCancel(JSONObject msg, boolean accepted) {
        Log.e(Constants.TAG_RTC, "onReceiveCallCancel=>" + msg.toString());

        this.hide_alert();
        showRmsg("onReceiveCallCancel", msg);
        if (accepted){
            IOManager.instance(ctx()).stop_camera();
        }
        callDirection = 0;
        callStatus = "";
        showTabScreenFromWorker(0);
    }

    @Override
    public synchronized void onReceiveCallEnd(JSONObject msg, boolean accepted) {
        Log.e(Constants.TAG_RTC, "onReceiveCallEnd=>" + msg.toString());

        this.hide_alert();
        showRmsg("onReceiveCallEnd", msg);
        if (accepted){
            IOManager.instance(ctx()).stop_camera();
        }
        callDirection = 0;
        callStatus = "";
    }

    @Override
    public void on_seen_message(JSONObject msg) {
        //showRmsg("seen_message", msg);
    }

    @Override
    public synchronized void onCallingError(JSONObject msgs) {
        Log.e(Constants.TAG_RTC, "onCallingError=>" + msgs.toString());
        this.showRmsg("onCallingError", msgs);
        IOManager.instance(this).stop_camera();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActionBar().show();
            }
        });
        onEndCall();
        callDirection = 0;
        callStatus = "";
    }
    @Override
    public synchronized void onInComingCall(JSONObject msg) {
        Log.e(Constants.TAG_RTC, "onInComingCall=>" + msg.toString());
        if (!Functions.isAppIsInBackground(getBaseContext()))
            alert_calling_from_other(msg);
    }
    private void alert_calling_from_other ( final JSONObject jsonObject ){
        hide_alert();

        int authorID = 0;
        String authorName = "";
        try{
            authorID = jsonObject.getInt("author_id");
            authorName = jsonObject.getString("author_name");
        }catch (JSONException ex){
            ex.printStackTrace();
            return;
        }
        final int author_id = authorID;
        final String author_name = authorName;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Calling from: " + author_name);
                builder.setMessage("Cancel or Accept ? ");
                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            boolean video = jsonObject.getBoolean("open_camera");

                            call_video = video;

                            change_mode(video);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        *//*btnStop.setEnabled(false);
                        btnStop2.setEnabled(true);*//*

                        // "denied" , "stopped", "accepted"
                        IOManager.instance(ctx()).response_calling(jsonObject, Response.ACCEPTED);
                        callDirection = CALL_DIRECTION_RECEIVE;
                        callStatus = CALL_STATUS_CALLING;
                        if (call_video) {
                            Variables.prevFrg = Variables.curFrg;
                            Variables.curFrg = Constants.TAG_FRG_VCALL;
                        } else {
                            Variables.prevFrg = Variables.curFrg;
                            Variables.curFrg = Constants.TAG_FRG_CALL;
                        }
                        tv_rtc_name.setText(author_name);
                        tv_rtc_status.setText(callStatus);
                    }
                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IOManager.instance(ctx()).response_calling(jsonObject, Response.DENIED);
                    }
                });
                alertDialog = builder.create();
                if (!isFinishing()) {
                    alertDialog.show();
                }*/

                final Uri uriAlarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + getPackageName() + "/raw/cowbell");
                mRingtone = RingtoneManager.getRingtone(getBaseContext(), uriAlarmSound);
                mRingtone.play();

                view_bg.setVisibility(View.VISIBLE);
                boolean video = false;

                try {
                    video = jsonObject.getBoolean("open_camera");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                getActionBar().hide();
                rtcView.setVisibility(View.VISIBLE);
                fragment_container.setVisibility(View.GONE);
                btn_rtc_answer.setVisibility(View.VISIBLE);
                btn_rtc_decline.setVisibility(View.VISIBLE);
                btn_rtc_endcall.setVisibility(View.GONE);
                setVisibleToggleButton(false);

                callPartnerID = String.valueOf(author_id);
                callPartnerName = author_name;
                callPartnerEmail = "";
                callStatus = CALL_STATUS_INCOMING_CALL;
                callDirection = CALL_DIRECTION_RECEIVE;
                tv_rtc_name.setText(author_name);
                tv_rtc_status.setText(callStatus);
                change_mode(video);

                final boolean aVideo = video;
                btn_rtc_decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDecline();
                    }
                });
                btn_rtc_answer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAnswer(aVideo, jsonObject, author_name);
                    }
                });

            }
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(CALL_WAITING_TIMEOUT);
                } catch (InterruptedException e) {
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callStatus.equalsIgnoreCase(CALL_STATUS_INCOMING_CALL)) {
                            onDecline();
                            Functions.toastString("Not answer", getBaseContext());
                        }
                    }
                });
            }
        };
        thread.start(); //start the thread

    }
    public void onDecline() {
        mRingtone.stop();
        setDisplayCall(0, "", "", "");
        callStatus = "";
        callDirection = 0;
        view_bg.setVisibility(View.GONE);
        btn_rtc_answer.setVisibility(View.GONE);
        btn_rtc_decline.setVisibility(View.GONE);
        btn_rtc_endcall.setVisibility(View.VISIBLE);
    }
    public void onAnswer(boolean video, JSONObject jsonObject, String author_name) {
        mRingtone.stop();
        call_video = video;
        view_bg.setVisibility(View.GONE);
        btn_rtc_answer.setVisibility(View.GONE);
        btn_rtc_decline.setVisibility(View.GONE);
        btn_rtc_endcall.setVisibility(View.VISIBLE);
        setVisibleToggleButton(true);
        IOManager.instance(ctx()).response_calling(jsonObject, Response.ACCEPTED);
        callDirection = CALL_DIRECTION_RECEIVE;
        callStatus = CALL_STATUS_CALLING;
        if (call_video) {
            Variables.prevFrg = Variables.curFrg;
            Variables.curFrg = Constants.TAG_FRG_VCALL;
        } else {
            Variables.prevFrg = Variables.curFrg;
            Variables.curFrg = Constants.TAG_FRG_CALL;
        }
        tv_rtc_name.setText(author_name);
        tv_rtc_status.setText(callStatus);
    }
    private synchronized void showRmsg (String action , JSONObject msgs){
        Log.e("Test", "action=>" + action);
        Log.e("Test", "msg=>" + msgs);
        final String aaction = action;
        final String amsgs = msgs.toString();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Functions.toastString(aaction + amsgs, getBaseContext());
            }
        });
    }


    @Override
    public void onGetLastMessage(JSONObject jsonObject) {

    }

    @Override
    public synchronized void onMediaFailure(SMException e) {
        Log.e(Constants.TAG_RTC, "onMediaFailure");
        hide_alert();
        showTabScreenFromWorker(0);
    }

    @Override
    public synchronized void onMediaDisconnected() {
        Log.e(Constants.TAG_RTC, "onMediaDisconnected");
        this.hide_alert();
        this.showTabScreenFromWorker(0);
    }

    @Override
    public synchronized void onMediaOpened(boolean isGroupCall) {
        Log.e(Constants.TAG_RTC, "onMediaOpened");
        if (!isGroupCall) {
            showTabScreenFromWorker(1);
        }
    }
    @Override
    public synchronized void onMediaSuccess(JSONObject data) {
        Log.e(Constants.TAG_RTC, "onMediaSuccess=>" + data.toString());

        showTabScreenFromWorker(1);
    }
    @Override
    public synchronized void onMediaStopped() {
        hide_alert();
        getBackToTabScreen();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*btnStop.setEnabled(false);
                btnStop2.setEnabled(true);*/
            }
        });
    }

    @Override
    public synchronized void onMediaStatusChanged(final String status) {
        Log.e(Constants.TAG_RTC, "onMediaStatusChanged=>" + status.toString());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alert("Status => " + status);
            }
        });
    }

    private void startAudioCall() {
        change_mode(false); // disable video button
        show_start_call(false);
        /*this.btnStop.setEnabled(true);
        this.btnStop2.setEnabled(false);*/
        this.call_video = false;
    }
    private void startVideoCall() {
        change_mode(true); // disable video button
        show_start_call(true);
        /*this.btnStop.setEnabled(true);
        this.btnStop2.setEnabled(false);*/
        this.call_video = true;
    }
    private void change_mode (boolean is_video){
        btn_rtc_camera.setEnabled(is_video);
        btn_rtc_video.setEnabled(is_video);
        btn_rtc_sound.setEnabled(is_video);
        if (is_video) {
            btn_rtc_camera.setBackgroundResource(R.drawable.ic_camera);
            btn_rtc_video.setBackgroundResource(R.drawable.ic_video);
            btn_rtc_sound.setBackgroundResource(R.drawable.ic_sound);
        } else {
            btn_rtc_camera.setBackgroundResource(R.drawable.ic_camera0);
            btn_rtc_video.setBackgroundResource(R.drawable.ic_video0);
            btn_rtc_sound.setBackgroundResource(R.drawable.ic_sound0);
        }
    }
    private void setVisibleToggleButton (boolean isShow) {
        if (isShow) {
            btn_rtc_camera.setVisibility(View.VISIBLE);
            /*btn_rtc_video.setVisibility(View.VISIBLE);
            btn_rtc_sound.setVisibility(View.VISIBLE);*/
        } else {
            btn_rtc_camera.setVisibility(View.GONE);
            /*btn_rtc_video.setVisibility(View.GONE);
            btn_rtc_sound.setVisibility(View.GONE);*/
        }
    }
    private synchronized void show_start_call(final  boolean has_video){
        alert_mCalling(Integer.parseInt(callPartnerID), callPartnerName);
        IOManager.instance(ctx()).send_calling(Variables.userFName + " " + Variables.userLName, has_video, Integer.parseInt(callPartnerID));
    }
    private synchronized void alert_mCalling ( int otherID, String otherName){
        this.hide_alert();
        final int other_id = otherID;
        final String other_name = otherName;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Calling to Staff_ID: " + other_id + "(" + other_name + ")");
                builder.setMessage("Cancel or End call ? ");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IOManager.instance(getApplicationContext()).cancel_calling(Variables.userFName + " " + Variables.userLName, call_video, other_id, false);
                        *//*btnStop.setEnabled(true);
                        btnStop2.setEnabled(true);*//*
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();*/

                callDirection = CALL_DIRECTION_SEND;
                callStatus = CALL_STATUS_DIALING;
                view_bg.setVisibility(View.VISIBLE);
                tv_rtc_name.setText(other_name);
                tv_rtc_status.setText(callStatus);
                setVisibleToggleButton(false);

            }
        });
    }


    private synchronized Context ctx(){
        return this.getApplicationContext();
    }
    private synchronized void hide_alert(){
        if (this.alertDialog != null){
            if (this.alertDialog.isShowing()){
                this.alertDialog.dismiss();
                this.alertDialog = null;
            }
        }
    }
    private synchronized void alert (String msg){
        hide_alert();
        //Functions.toastString(msg, this);
        Log.d(Constants.TAG_RTC, msg);
    }
    private void setInitFragments() {
        // TODO Auto-generated method stub
        if (findViewById(R.id.fragment_container) != null) {

            Frg_Personal personalFragment = new Frg_Personal();
            personalFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, personalFragment, Constants.TAG_FRG_PER).commit();

            Variables.curFrg = Constants.TAG_FRG_PER;
            setIndicator(Variables.curFrg);

        }
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        db = new SQLiteHandler(getApplicationContext());
        staffSaved = new HashMap<String, String>();
        groupSaved = new HashMap<String, String>();
        call = new CallAPIHandler();
    }

    public void setInitView() {
        setCustomActionBar(Variables.userFName + " " + Variables.userLName);
        fragment_container = (FrameLayout)findViewById(R.id.fragment_container);

        // RTC view
        rtcView = findViewById(R.id.video);
        view_bg = findViewById(R.id.view_bg);
        tv_rtc_name = (TextView)findViewById(R.id.tv_rtc_name);
        tv_rtc_status = (TextView)findViewById(R.id.tv_rtc_status);
        btn_rtc_decline = (Button)findViewById(R.id.btn_rtc_decline);
        btn_rtc_endcall = (Button)findViewById(R.id.btn_rtc_endcall);
        btn_rtc_answer = (Button)findViewById(R.id.btn_rtc_answer);
        btn_rtc_sound = (Button)findViewById(R.id.btn_rtc_sound);
        btn_rtc_video = (Button)findViewById(R.id.btn_rtc_video);
        btn_rtc_camera = (Button)findViewById(R.id.btn_rtc_camera);
        /*btnStop =  findViewById(R.id.btnStop);
        btnStop2 =  findViewById(R.id.btnStop2);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStop();
            }
        });
        btnStop2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStop2();
            }
        });*/
        btn_rtc_endcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEndCall();
            }
        });
        btn_rtc_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCamera();
            }
        });
        btn_rtc_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSound();
            }
        });
        btn_rtc_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVideo();
            }
        });
        // End RTC view
        setDisplayCall(0, "", "", "");
    }
    public void onClickStop() {
        IOManager.instance(ctx()).end_calling(Variables.userFName + " " + Variables.userLName, this.call_video, Integer.parseInt(callPartnerID), false);
        getBackToTabScreen();
        /*this.btnStop.setEnabled(true);
        this.btnStop2.setEnabled(true);*/
    }
    public void onClickStop2() {
        IOManager.instance(ctx()).stopCallFromUser();
        getBackToTabScreen();
        /*this.btnStop.setEnabled(true);
        this.btnStop2.setEnabled(true);*/
    }
    public void onEndCall() {
        if (callStatus.equalsIgnoreCase(CALL_STATUS_CALLING)) {
            if (callDirection == CALL_DIRECTION_SEND) {
                IOManager.instance(ctx()).end_calling(Variables.userFName + " " + Variables.userLName, this.call_video, Integer.parseInt(callPartnerID), false);
                getBackToTabScreen();
            } else if (callDirection == CALL_DIRECTION_RECEIVE) {
                IOManager.instance(ctx()).stopCallFromUser();
                getBackToTabScreen();
            }
        } else if (callStatus.equalsIgnoreCase(CALL_STATUS_DIALING)) {
            IOManager.instance(getApplicationContext()).cancel_calling(Variables.userFName + " " + Variables.userLName, call_video,
                    Integer.parseInt(callPartnerID), false);
            getBackToTabScreen();
            prepareMissedCall(Variables.userToken, callPartnerID);
        }
    }

    public void onClickCamera() {
        IOManager.instance(ctx()).switchCamera();
    }
    public void onClickSound() {
        IOManager.instance(ctx()).toggleSound();
    }
    public void onClickVideo() {
        IOManager.instance(ctx()).toggleVideo();
    }

    public void setCustomActionBar(String title) {
        // TODO Auto-generated method stub
        Functions.setActionBarHeight(Functions.dpToPx(94), this);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
        getActionBar().setCustomView(R.layout.actionbar_main);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        bt_m_back = (Button)findViewById(R.id.bt_m_back);
        bt_m_back.setVisibility(View.GONE);
        bt_m_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        ll_icon_tab = (LinearLayout)findViewById(R.id.ll_icon_tab);
        ll_indicator = (LinearLayout)findViewById(R.id.ll_indicator);
        ll_tab_per = (LinearLayout)findViewById(R.id.ll_tab_per);
        ll_tab_mess = (LinearLayout)findViewById(R.id.ll_tab_mess);
        ll_tab_timeline = (LinearLayout)findViewById(R.id.ll_tab_timeline);
        ll_tab_task = (LinearLayout)findViewById(R.id.ll_tab_task);
        ll_tab_menu2 = (LinearLayout)findViewById(R.id.ll_tab_menu2);
        img_tab_per_idc = (ImageView)findViewById(R.id.img_tab_per_idc);
        img_tab_mess_idc = (ImageView)findViewById(R.id.img_tab_mess_idc);
        img_tab_timeline_idc = (ImageView)findViewById(R.id.img_tab_timeline_idc);
        img_tab_task_idc = (ImageView)findViewById(R.id.img_tab_task_idc);
        img_tab_menu2_idc = (ImageView)findViewById(R.id.img_tab_menu2_idc);
        img_tab_mess = (ImageView)findViewById(R.id.img_tab_mess);
        ll_tab_per.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_PER);
            }
        });
        ll_tab_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_MESS);
            }
        });
        ll_tab_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_TIMELINE);
            }
        });
        ll_tab_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_TASK);
            }
        });
        ll_tab_menu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_SETTING);
            }
        });

        setUnseenCountTab(Variables.numConversationUnSeen);
    }
    public void onBack() {
        getFragmentManager().popBackStack();
    }
    public void setCustomActionBarChat(String title) {
        // TODO Auto-generated method stub
        Functions.setActionBarHeight(Functions.dpToPx(44), this);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
        getActionBar().setCustomView(R.layout.actionbar_chat);
        tv_c_name = (TextView)findViewById(R.id.tv_c_name);
        bt_c_back = (Button)findViewById(R.id.bt_c_back);
        bt_c_menu = (Button)findViewById(R.id.bt_c_menu);
        bt_c_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        tv_c_name.setText(title);

    }
    public void setABTitle(String s) {
        tvTitle.setText(s);
    }
    public void setDisplayCall(final int isCall, final String id, final String name, final String email) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCall == 1) { // audio call
                    getActionBar().hide();
                    rtcView.setVisibility(View.VISIBLE);
                    fragment_container.setVisibility(View.GONE);
                    callPartnerID = id;
                    callPartnerName = name;
                    callPartnerEmail = email;
                    startAudioCall();
                } else if (isCall == 2) { // video call
                    getActionBar().hide();
                    rtcView.setVisibility(View.VISIBLE);
                    fragment_container.setVisibility(View.GONE);
                    callPartnerID = id;
                    callPartnerName = name;
                    callPartnerEmail = email;
                    startVideoCall();
                } else {
                    getActionBar().show();
                    isResumeWhileCalling = 0;
                    rtcView.setVisibility(View.GONE);
                    fragment_container.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private synchronized void showTabScreenFromWorker(final int position){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (position) {
                    case 0:
                        rtcView.setVisibility(View.GONE);
                        fragment_container.setVisibility(View.VISIBLE);
                        isResumeWhileCalling = 0;
                        view_bg.setVisibility(View.GONE);
                        getActionBar().show();
                        break;
                    case 1:
                        rtcView.setVisibility(View.VISIBLE);
                        fragment_container.setVisibility(View.GONE);
                        getActionBar().hide();
                        break;
                    case 2:
                        break;
                }
            }
        });
    }
    public void openTab(String tag) {
        switch (tag) {
            case Constants.TAG_FRG_MESS:
                bt_m_back.setVisibility(View.GONE);
                if (Variables.curFrg == Constants.TAG_FRG_MESS) {
                    // if current fragment is Frg_Mess, do nothing
                } else {
                    getFragmentManager().popBackStack();
                    Frg_Mess stFrg = new Frg_Mess();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, stFrg, Constants.TAG_FRG_MESS)
                            .addToBackStack(null)
                            .commit();

                    Variables.curFrg = Constants.TAG_FRG_MESS;
                    setIndicator(Variables.curFrg);
                }
                break;
            case Constants.TAG_FRG_TIMELINE:
                bt_m_back.setVisibility(View.GONE);
                if (Variables.curFrg == Constants.TAG_FRG_TIMELINE) {
                    // if current fragment is Frg_Timeline, do nothing
                } else {
                    getFragmentManager().popBackStack();
                    Frg_Timeline stFrg = new Frg_Timeline();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, stFrg, Constants.TAG_FRG_TIMELINE)
                            .addToBackStack(null)
                            .commit();

                    Variables.curFrg = Constants.TAG_FRG_TIMELINE;
                    setIndicator(Variables.curFrg);
                }
                break;
            case Constants.TAG_FRG_TASK:
                bt_m_back.setVisibility(View.GONE);
                if (Variables.curFrg == Constants.TAG_FRG_TASK) {
                    // if current fragment is Frg_Task, do nothing
                } else {
                    Frg_Task tFrg = new Frg_Task();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, tFrg, Constants.TAG_FRG_TASK)
                            .addToBackStack(null)
                            .commit();

                    Variables.curFrg = Constants.TAG_FRG_TASK;
                    setIndicator(Variables.curFrg);
                }
                break;
            case Constants.TAG_FRG_SETTING:
                bt_m_back.setVisibility(View.GONE);
                if (Variables.curFrg == Constants.TAG_FRG_SETTING) {
                    // if current fragment is SETTING, do nothing
                } else {
                    getFragmentManager().popBackStack();
                    Frg_Setting stFrg = new Frg_Setting();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, stFrg, Constants.TAG_FRG_SETTING)
                            .addToBackStack(null)
                            .commit();

                    Variables.curFrg = Constants.TAG_FRG_SETTING;
                    setIndicator(Variables.curFrg);
                }
                break;
            default:
                bt_m_back.setVisibility(View.GONE);
                if (Variables.curFrg == Constants.TAG_FRG_PER) {
                    // if current fragment is Frg_Personal, do nothing
                } else {
                    getFragmentManager().popBackStack();
                    Frg_Personal stFrg = new Frg_Personal();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, stFrg, Constants.TAG_FRG_PER)
                            .addToBackStack(null)
                            .commit();

                    Variables.curFrg = Constants.TAG_FRG_PER;
                    setIndicator(Variables.curFrg);
                }
                break;
        }
    }
    public static void setBackButton(String tag) {
        switch (tag) {
            case Constants.TAG_FRG_GROUP:
                bt_m_back.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void prepareGetUnSeen(String token) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        new RequestServerUnSeen().execute(Constants.URL_API_GET_UNSEEN, call.createQueryStringForParameters(params));
    }
    private class RequestServerUnSeen extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            // Making a request to url and getting response
            String jsonStr = call.requestPOST(arg0[0], arg0[1]);

            Log.d("---Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                return jsonStr;
            } else {
                Log.e(Constants.TAG_API, Constants.ERR_NO_DATA_FROM_SERVER);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonStr) {
            super.onPostExecute(jsonStr);
            // Dismiss the progress dialog
            if (jsonStr == null) {
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, getBaseContext());
                return;
            }
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    JSONObject jOObjects = jObj.getJSONObject("objects");

                    Variables.numConversationUnSeen = jOObjects.getInt("unseen");
                    MainActivity.setUnseenCountTab(Variables.numConversationUnSeen);

                } else {
                    JSONObject jOObjects = jObj.getJSONObject("objects");
                    int is_work = jOObjects.getInt("is_work");
                    if (is_work == 0) {

                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_GETTING + errorMsg);
                    }

                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
            }
        }
    }
    public static void setUnseenCountTab(int unSeenCount) {
        if (unSeenCount == 0) {
            img_tab_mess.setBackgroundResource(R.drawable.ic_mess);
            return;
        }
        switch (unSeenCount) {
            case 1:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_01);
                break;
            case 2:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_02);
                break;
            case 3:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_03);
                break;
            case 4:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_04);
                break;
            case 5:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_05);
                break;
            case 6:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_06);
                break;
            case 7:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_07);
                break;
            case 8:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_08);
                break;
            case 9:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_09);
                break;
            case 10:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_10);
                break;
            default:
                img_tab_mess.setBackgroundResource(R.drawable.ic_mess_10p);
                break;
        }
    }
    /*private void setCustomActionBar(String title) {
        // TODO Auto-generated method stub
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
        getActionBar().setCustomView(R.layout.actionbar_main);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        imgAdd = (ImageView)findViewById(R.id.imgAdd);
    	
    	*//*getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
    	getActionBar().setTitle(title);
    	getActionBar().setHomeButtonEnabled(false);
    	getActionBar().setDisplayHomeAsUpEnabled(false);*//*
    }*/

    public static void setIndicator(String tag) {
        switch (tag) {
            case Constants.TAG_FRG_MESS:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.VISIBLE);
                img_tab_timeline_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.INVISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
            case Constants.TAG_FRG_TIMELINE:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_timeline_idc.setVisibility(View.VISIBLE);
                img_tab_task_idc.setVisibility(View.INVISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
            case Constants.TAG_FRG_TASK:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_timeline_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.VISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
            case Constants.TAG_FRG_TASK_DETAIL:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_timeline_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.VISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
            case Constants.TAG_FRG_CREATE_TASK:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_timeline_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.VISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
            case Constants.TAG_FRG_SETTING:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_timeline_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.INVISIBLE);
                img_tab_menu2_idc.setVisibility(View.VISIBLE);
                break;
            default:
                img_tab_per_idc.setVisibility(View.VISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_timeline_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.INVISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
        }
    }

    /*private void setInitNifty() {
        // TODO Auto-generated method stub
        Variables.deviceID = Functions.getDeviceID(this);
        Variables.androidVersion = android.os.Build.VERSION.RELEASE;
        Variables.deviceName = android.os.Build.MANUFACTURER + android.os.Build.PRODUCT;
        NCMB.initialize(this, Constants.KEY_SERVER_NIFTY, Constants.KEY_CLIENT_NIFTY);
        NCMBPush.setDefaultPushCallback(this, MainActivity.class);

        final NCMBInstallation installation = NCMBInstallation.getCurrentInstallation();
        installation.getRegistrationIdInBackground(Constants.KEY_SENDERID_GCM, new RegistrationCallback(){
            @Override
            public void done(NCMBException e) {
                NCMBInstallation prevInstallation = null;
                if (e == null) {
                    try {
                        NCMBQuery<NCMBInstallation> query = NCMBInstallation
                                .getQuery();

                        query.whereEqualTo("device_id", Variables.deviceID);
                        prevInstallation = query.getFirst();
                        if (prevInstallation != null) {
                            if (!prevInstallation
                                    .getString("device_id")
                                    .trim()
                                    .equals(Variables.deviceID
                                            .trim())) {}
                        }
                        if (prevInstallation == null) {
                            installation.put("device_id",
                                    Variables.deviceID.trim());
                            //installation.put("channels", Constants.NIFTY_CHANNEL);
                            installation.put("osversion", Variables.androidVersion.trim());
                            installation.put("device_name", Variables.deviceName.trim());
                            installation.save();
                        }
                    } catch (NCMBException le) {}
                } else {
                    e.printStackTrace();
                }
            }
        });
        // プッシュ開封登録の実施
        NCMBAnalytics.trackAppOpened(getIntent());
    }*/

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

    @SuppressWarnings("static-access")
    @Override
    public void onBackPressed() {
        //if (getFragmentManager().getBackStackEntryCount() > 0) {
        // get Back
        try {
            Thread.currentThread().sleep(100);
            if (Variables.curFrg.equalsIgnoreCase(Constants.TAG_FRG_CALL) ||
                    Variables.curFrg.equalsIgnoreCase(Constants.TAG_FRG_VCALL)) {
                lauchAlertBackDialog();
            } else if (Variables.curFrg.equalsIgnoreCase(Constants.TAG_FRG_PER)) {
                finish();
            } else if (Variables.curFrg.equalsIgnoreCase(Constants.TAG_FRG_CHAT)) {
                prepareGetUnSeen(Variables.userToken);
                super.onBackPressed();
            } else {
                //getFragmentManager().popBackStack();
                super.onBackPressed();
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //} else {

        //}
    }
    private void lauchAlertBackDialog() {
        // TODO Auto-generated method stub
        AlertDialog.Builder dl_send = new AlertDialog.Builder(this);
        dl_send.setTitle(Functions.getResString(this, R.string.app_name));
        dl_send.setMessage(Functions.getResString(this, R.string.alert_call_title_back))
                .setPositiveButton(Functions.getResString(this, R.string.bt_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onEndCall();
                    }
                })
                .setNegativeButton(Functions.getResString(this, R.string.bt_no), null);

        AlertDialog dialog = dl_send.create();
        dialog.show();
    }
    public void getBackToTabScreen() {
        setDisplayCall(0, "", "", "");
        Functions.setActionBarHeight(Functions.dpToPx(94), this);
        Variables.curFrg = Variables.prevFrg;
        Variables.prevFrg = "";
        isResumeWhileCalling = 0;
        view_bg.setVisibility(View.GONE);
    }
    @Override
    protected void onResume() {
        IOManager.instance(this).auto_connected();
        super.onResume();
        initNetworkChecking();
        if (Variables.curFrg.equalsIgnoreCase(Constants.TAG_FRG_CALL) || Variables.curFrg.equalsIgnoreCase(Constants.TAG_FRG_VCALL)) {
            isResumeWhileCalling = 1;
        }

    }
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        unregisterReceiver(mNetworkStateCallback);
        isResumeWhileCalling = 0;
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initNetworkChecking() {
        // TODO Auto-generated method stub
        mNetworkStateCallback = new NetworkStateReceiver();
        mNetworkStateCallback.setNetworkStateListener(new NetworkStateReceiver.NetworkStateListener() {
            @Override
            public void onNetChange(int status) {
                // TODO Auto-generated method stub
                Variables.networkState = status;
                mLocalRtcListener.onLocalNetworkChange();
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(mNetworkStateCallback, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            /*case R.id.action_logout:
                lauchAlertLogoutDialog();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
