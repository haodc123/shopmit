package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.CMessageAdapter;
import com.example.shopmeet.adapter.ListGroupAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.CMessageData;
import com.example.shopmeet.model.CSender;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.NetworkUtil;
import com.example.shopmeet.utils.SQLiteHandler;
import com.example.shopmeet.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.manager.IOManager;

/**
 * Created by UserPC on 6/20/2016.
 */
public class Frg_Chat extends Fragment implements MainActivity.LocalRtcListener {

    // Search bar
    private EditText edt_c_search;
    private Button bt_c_search;

    private EditText edt_c_mess;
    private Button bt_c_send;
    private RecyclerView recyclerView;
    private List<CMessageData> msgList;
    private CMessageAdapter msgAdapter;
    private Uri fileUri = null;

    private int chatType = Constants.TYPE_PERSON; // default
    private String partner_id;
    private String partner_name;

    // Id message tạm để đánh dấu trong lúc gửi lên server và chờ response để update, id tạm này là timestamp ngay lúc gửi ở client
    private String uniqueMsgId;

    private CallAPIHandler call;
    private SQLiteHandler db;
    private ProgressDialog pDialog;
    private SessionManager session;
    private HashMap<String, String> convSaved;
    private int isAlreadyLoadFromServer;

    private int saved_id_global;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_chat, container, false);
        MainActivity.mLocalRtcListener = this;
        setHasOptionsMenu(true);
        getBundle();
        setInitView(v);
        initVariables();

        setData();

        return v;
    }
    private void getBundle() {
        Bundle b = this.getArguments();
        if (b != null) {
            chatType = b.getInt("chatType"); // group or private
            partner_id = b.getString("partner_id"); // In case of group chat, this is group id
            partner_name = b.getString("partner_name"); // In case of group chat, this is group name
        }
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        call = new CallAPIHandler();
        db = new SQLiteHandler(getActivity());
        isAlreadyLoadFromServer = 0;
    }
    private void setInitView(View v) {
        ((MainActivity)getActivity()).setCustomActionBarChat(Functions.getResString(getActivity(), R.string.frg_chat_title)+" "+partner_name);

        recyclerView = (RecyclerView)v.findViewById(R.id.recycler_view);
        edt_c_mess = (EditText)v.findViewById(R.id.edt_c_mess);
        bt_c_send = (Button)v.findViewById(R.id.bt_c_send);
        edt_c_search = (EditText)v.findViewById(R.id.edt_c_search);
        bt_c_search = (Button)v.findViewById(R.id.bt_c_search);

        bt_c_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearch(edt_c_search.getText().toString());
            }
        });
        bt_c_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Functions.hasConnection(getActivity())) {
                    Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
                } else {
                    onSendMessage(edt_c_mess.getText().toString());
                }
            }
        });
        edt_c_search.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edt_c_search.getText().toString().equals("")) {
                    refreshListAfterSearch();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
    }
    public void onSearch(String kw) {
        if (kw.equals("")) {
            return;
        }
        for (int i = 0; i < msgList.size(); i++) {
            if (!msgList.get(i).getMessage().toLowerCase().contains(kw.toLowerCase())) {
                if (recyclerView.getChildAt(i) != null) {
                    msgList.get(i).setIsDisplay(0);
                }
            } else {
                if (recyclerView.getChildAt(i) != null) {
                    msgList.get(i).setIsDisplay(1);
                }
            }
        }
        msgAdapter.notifyDataSetChanged();
    }
    public void refreshListAfterSearch() {
        if (msgList == null)
            return;
        for (int i = 0; i < msgList.size(); i++) {
            msgList.get(i).setIsDisplay(1);
            /*if (recyclerView.getChildAt(i) != null) {
                recyclerView.getChildAt(i).setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = recyclerView.getChildAt(i).getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                recyclerView.getChildAt(i).setLayoutParams(params);
            }*/
        }
        msgAdapter.notifyDataSetChanged();
    }

    public synchronized void onSendMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        // Update vào list msg trong lúc gửi lên server và chờ response
        uniqueMsgId = System.currentTimeMillis()+"";
        CMessageData message = new CMessageData();
        message.setId(uniqueMsgId);
        message.setMessage(msg);
        message.setCreatedAt("sending...");
        message.setIsDisplay(1);
        message.setContentType(CMessageData.CHAT_CONTENT_TYPE_MSG);
        CSender user = new CSender(Variables.userID, Variables.userName, null);
        message.setSender(user);
        msgList.add(message);
        msgAdapter.notifyDataSetChanged();
        if (msgAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, msgAdapter.getItemCount() - 1);
        }

        edt_c_mess.setText("");

        if (chatType == Constants.TYPE_PERSON) {
            sendPrivateMessageToServer(msg, uniqueMsgId);
        } else {
            sendGroupMessageToServer(msg, uniqueMsgId);
        }
    }
    public void sendPrivateMessageToServer(final String msg, final String uniqueMsgId) {
        IOManager.instance(getActivity()).send_private_message(
                Variables.userFName + " " + Variables.userLName,
                uniqueMsgId,
                msg,
                Integer.parseInt(partner_id),
                CMessageData.CHAT_CONTENT_TYPE_MSG);
    }
    public void sendGroupMessageToServer(final String msg, final String uniqueMsgId) {
        IOManager.instance(getActivity()).send_group_message(
                Variables.userFName + " " + Variables.userLName,
                uniqueMsgId,
                msg,
                Integer.parseInt(partner_id),
                CMessageData.CHAT_CONTENT_TYPE_MSG, null);
    }
    public void onLocalReceive_private_message(JSONObject data) {
        try {
            String content = data.getString("content");
            String created = data.getString("created");
            String type = data.getString("type"); // Content type
            String uniqueId = data.getString("uniqueId");
            int last_id = data.getInt("last_id");
            int saved_id = data.getInt("saved_id");
            int author_id = data.getInt("author_id");
            int friend_id = data.getInt("friend_id");

            if (saved_id_global == saved_id) { // this msg I already received, just ignore it
                return;
            } else {
                saved_id_global = saved_id;
            }
            if (author_id  == Integer.parseInt(Variables.userID) && friend_id == Integer.parseInt(partner_id)) {
                // This is my message I was sent
                CSender user = new CSender(String.valueOf(author_id), "", "");
                int isAlreadyOnScreen = 0;
                // My msg, so find the msg text already on screen and replace it with server-side info
                for (CMessageData msg : msgList) {
                    if (msg.getId().equals(uniqueId) || msg.getId().equals(saved_id)) {
                        isAlreadyOnScreen = 1;
                        int index = msgList.indexOf(msg);
                        msg.setId(String.valueOf(saved_id));
                        msg.setMessage(content);
                        msg.setCreatedAt(created);
                        msg.setIsDisplay(1);
                        msg.setSender(user);
                        msg.setContentType(type);
                        msgList.remove(index);
                        msgList.add(index, msg);
                        break;
                    }
                }
                if (isAlreadyOnScreen == 0) {
                    // Althought this is my msg, but it does not already on screen, because I sent on other device
                    CSender u = new CSender(String.valueOf(author_id), "", "");
                    CMessageData msg = new CMessageData();
                    msg.setId(String.valueOf(saved_id));
                    msg.setMessage(content);
                    msg.setCreatedAt(created);
                    msg.setIsDisplay(1);
                    msg.setSender(u);
                    msg.setContentType(type);
                    msgList.add(msg);
                }
            } else if (author_id == Integer.parseInt(partner_id) && friend_id == Integer.parseInt(Variables.userID)) {
                // This is message partner send to me
                CSender user = new CSender(String.valueOf(author_id), "", "");
                CMessageData msg = new CMessageData();
                msg.setId(String.valueOf(saved_id));
                msg.setMessage(content);
                msg.setCreatedAt(created);
                msg.setIsDisplay(1);
                msg.setSender(user);
                msg.setContentType(type);
                msgList.add(msg);

            }
            refreshRecyclerview();

            // confirm read message
            IOManager.instance(getActivity()).confirm_read_message(Integer.parseInt(partner_id), true);

            // Các thao tác trên local db khi nhận msg
            receiveMsgToDB(String.valueOf(saved_id), content, type, created,
                    String.valueOf(author_id), "", String.valueOf(friend_id));

        } catch (JSONException e) {
            // JSON error
            e.printStackTrace();
            Log.e(Constants.TAG_API, data.toString() + Constants.ERR_JSON + e.getMessage());
        }
    }
    public void onLocalReceive_group_message(JSONObject data) {
        try {
            String content = data.getString("content");
            String created = data.getString("created");
            String type = data.getString("type"); // Content type
            String uniqueId = data.getString("uniqueId");
            int last_id = data.getInt("last_id");
            int saved_id = data.getInt("saved_id");
            int author_id = data.getInt("author_id");
            String author_name = data.getString("author_name");
            int group_id = data.getInt("group_id");

            if (saved_id_global == saved_id) { // this msg I already received, just ignore it
                return;
            } else {
                saved_id_global = saved_id;
            }
            if (author_id  == Integer.parseInt(Variables.userID) && group_id == Integer.parseInt(partner_id)) {
                // This is my message I was sent
                CSender user = new CSender(String.valueOf(author_id), "", "");
                int isAlreadyOnScreen = 0;
                for (CMessageData msg : msgList) {
                    if (msg.getId().equals(uniqueId) || msg.getId().equals(saved_id)) {
                        isAlreadyOnScreen = 1;
                        int index = msgList.indexOf(msg);
                        msg.setId(String.valueOf(saved_id));
                        msg.setMessage(content);
                        msg.setCreatedAt(created);
                        msg.setIsDisplay(1);
                        msg.setSender(user);
                        msg.setContentType(type);
                        msgList.remove(index);
                        msgList.add(index, msg);
                        break;
                    }
                }
                if (isAlreadyOnScreen == 0) {
                    // Even if this is my msg, but it does not already on screen, because I sent on other device
                    CSender u = new CSender(String.valueOf(author_id), "", "");
                    CMessageData msg = new CMessageData();
                    msg.setId(String.valueOf(saved_id));
                    msg.setMessage(content);
                    msg.setCreatedAt(created);
                    msg.setIsDisplay(1);
                    msg.setSender(u);
                    msg.setContentType(type);
                    msgList.add(msg);
                }
            } else if (author_id  != Integer.parseInt(Variables.userID) && group_id == Integer.parseInt(partner_id)) {
                // This is message partner send to me
                CSender user = new CSender(String.valueOf(author_id), author_name, "");
                CMessageData msg = new CMessageData();
                msg.setId(String.valueOf(saved_id));
                msg.setMessage(content);
                msg.setCreatedAt(created);
                msg.setIsDisplay(1);
                msg.setSender(user);
                msg.setContentType(type);
                msgList.add(msg);

            }
            refreshRecyclerview();

            // confirm read message
            IOManager.instance(getActivity()).confirm_read_message(Integer.parseInt(partner_id), false);

            // Các thao tác trên local db khi nhận msg
            receiveMsgToDB(String.valueOf(saved_id), content, type, created,
                    String.valueOf(author_id), author_name, String.valueOf(group_id));

        } catch (JSONException e) {
            // JSON error
            e.printStackTrace();
            Log.e(Constants.TAG_API, data.toString() + Constants.ERR_JSON + e.getMessage());
        }
    }
    public void receiveMsgToDB(String saved_id, String content, String type, String created,
                               String author_id, String author_name, String receive_id) {
        // Save msg in local db
        db.addMsg(saved_id, content, created, author_id, author_name, type, receive_id);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (MainActivity.isResumeWhileCalling == 0) {
            Variables.curFrg = Constants.TAG_FRG_CHAT;
            MainActivity.setIndicator(Variables.curFrg);
            if (!Functions.hasConnection(getActivity())) {
                showChatDetailFromDB(partner_id);
                Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            } else {
                prepareGetList(Variables.userToken, chatType, partner_id);
            }

            if (chatType == Constants.TYPE_PERSON)
                IOManager.instance(getActivity()).confirm_read_message(Integer.parseInt(partner_id), true);
            else
                IOManager.instance(getActivity()).confirm_read_message(Integer.parseInt(partner_id), false);
        }
    }

    public void setData() {
        // TODO Auto-generated method stub
        msgList = new ArrayList<>();
        msgAdapter = new CMessageAdapter(getActivity(), msgList, Variables.userID, chatType);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(msgAdapter);

    }
    public void showChatDetailFromDB(String partner_id) {
        msgList.clear();
        db.getAllMsgOfConv(partner_id, msgList);
        if (msgList.size() > 0) {
            msgAdapter.notifyDataSetChanged();
        }
        recyclerView.scrollToPosition(msgList.size()-1);
    }
    public void prepareGetList(String token, int chatType, String partner_id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        if (chatType == Constants.TYPE_PERSON) {
            params.put("isPrivate", "1");
            params.put("friend_id", partner_id);
            params.put("group_id", "");
        } else {
            params.put("isPrivate", "0");
            params.put("friend_id", "");
            params.put("group_id", partner_id);
        }

        // Using HttpURLConnection (CallAPIHandler)
        new RequestServer().execute(Constants.URL_API_CHAT_LIST_MSG, call.createQueryStringForParameters(params));
    }
    private class RequestServer extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(Constants.INFORM_WAIT);
            pDialog.setCancelable(true);
            pDialog.show();
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
            if (pDialog.isShowing())
                pDialog.dismiss();
            if (jsonStr == null) {
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, getActivity());
                return;
            }
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    JSONArray jAMsg = jObj.getJSONArray("objects");
                    msgList.clear();
                    db.delMsgByPartnerId(partner_id);
                    for (int i = 0; i < jAMsg.length(); i++) {
                        JSONObject jElm = jAMsg.getJSONObject(i);
                        int id = jElm.optInt("id");
                        int author_id = 0;
                        int friend_id = 0;
                        int group_id = 0;
                        String author_fname = "";
                        String author_lname = "";
                        if (chatType == Constants.TYPE_PERSON) {
                            author_id = jElm.optInt("author_id");
                            friend_id = jElm.optInt("friend_id");
                        } else {
                            author_id = jElm.optInt("author_id");
                            group_id = jElm.optInt("group_id");
                            author_fname = jElm.optString("author_first_name");
                            author_lname = jElm.optString("author_last_name");
                        }

                        String content = jElm.optString("content");
                        String type = jElm.optString("type");
                        String created = jElm.optString("created");

                        if (type.equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG)) {
                            CMessageData msg = new CMessageData();
                            msg.setId(String.valueOf(id));
                            msg.setMessage(Functions.decodeBase64(content));
                            msg.setCreatedAt(created);
                            msg.setIsDisplay(1);
                            msg.setSender(new CSender(String.valueOf(author_id), author_fname + " " + author_lname, ""));
                            msg.setContentType(type);
                            msgList.add(msg);

                            msgAdapter.notifyDataSetChanged();

                        } else if (type.equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_CALL)) {
                            CMessageData msg = new CMessageData();
                            msg.setId(String.valueOf(id));
                            msg.setMessage(content);
                            msg.setCreatedAt(created);
                            msg.setIsDisplay(1);
                            msg.setSender(new CSender(String.valueOf(author_id), author_fname+" "+author_lname, ""));
                            msg.setContentType(type);
                            msgList.add(msg);

                            msgAdapter.notifyDataSetChanged();
                        }

                        if (chatType == Constants.TYPE_PERSON) {
                            if (type.equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG)) {
                                db.addMsg(String.valueOf(id), Functions.decodeBase64(content), created, String.valueOf(author_id),
                                        author_fname + " " + author_lname, type, String.valueOf(friend_id));
                            } else {
                                db.addMsg(String.valueOf(id), content, created, String.valueOf(author_id),
                                        author_fname + " " + author_lname, type, String.valueOf(friend_id));
                            }
                        } else {
                            if (type.equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG)) {
                                db.addMsg(String.valueOf(id), Functions.decodeBase64(content), created, String.valueOf(author_id),
                                        author_fname + " " + author_lname, type, String.valueOf(group_id));
                            } else {
                                db.addMsg(String.valueOf(id), content, created, String.valueOf(author_id),
                                        author_fname + " " + author_lname, type, String.valueOf(group_id));
                            }
                        }
                    }
                    recyclerView.scrollToPosition(msgList.size()-1);

                } else {
                    JSONObject jOObjects = jObj.getJSONObject("objects");
                    int is_work = jOObjects.getInt("is_work");
                    if (is_work == 0) {
                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_not_working), getActivity());
                        // Logout
                        db.deleteUsers();
                        session.setLogin(false);
                        IOManager.signout();

                        Intent i = new Intent(getActivity(), LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_GETTING + errorMsg);
                        Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, getActivity());
                    }
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                //Functions.toastString(Constants.ERR_JSON, getActivity());
            }
        }

    }

    protected void refreshRecyclerview() {
        // TODO Auto-generated method stub
        msgAdapter.notifyDataSetChanged();
        if (msgAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, msgAdapter.getItemCount() - 1);
        }
    }
    @Override
    public void onStop() {
        db.close();
        super.onStop();
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //menu.findItem(R.id.action_logout).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onLocalNetworkChange() {
        switch (Variables.networkState) {
            case NetworkUtil.TYPE_WIFI:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetList(Variables.userToken, chatType, partner_id);
                    isAlreadyLoadFromServer = 1;
                }
                break;
            case NetworkUtil.TYPE_MOBILE:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetList(Variables.userToken, chatType, partner_id);
                    isAlreadyLoadFromServer = 1;
                }
                break;
            case NetworkUtil.TYPE_NOT_CONNECTED:
                if (Variables.isAlreadyAlertConnection == 0) {
                    Functions.toastString(getResources().getString(R.string.alert_no_internet), getActivity());
                    Variables.isAlreadyAlertConnection = 1;
                }
                break;
            default:
                break;
        }
    }

    public void onLocalOn_user_online(JSONObject data) {
    }
}
