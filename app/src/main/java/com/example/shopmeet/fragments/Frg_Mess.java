package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListGroupAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListMessAdapter;
import com.example.shopmeet.adapter.ListMessAdapter.ListMessData;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.NetworkUtil;
import com.example.shopmeet.utils.SQLiteHandler;
import com.example.shopmeet.utils.SessionManager;
import com.example.shopmeet.view.MyExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.manager.IOManager;

/**
 * Created by UserPC on 6/10/2016.
 */
public class Frg_Mess extends Fragment implements MainActivity.LocalRtcListener {

    // Action bar
    private TextView tv_messtd_alert, tv_messp_alert;
    // Today
    private MyExpandableListView lv_mess_list_today;
    private List<ListMessData> messListTD;
    private ListMessAdapter messAdapterTD;
    // Pass
    private MyExpandableListView lv_mess_list_pass;
    private List<ListMessData> messListP;
    private ListMessAdapter messAdapterP;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private int isAlreadyLoadFromServer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_mess, container, false);
        MainActivity.mLocalRtcListener = this;
        setInitView(v);
        initVariables();
        setData();

        return v;
    }

    private void initVariables() {
        // TODO Auto-generated method stub
        isAlreadyLoadFromServer = 0;
        session = new SessionManager(getActivity());
        db = new SQLiteHandler(getActivity());
        call = new CallAPIHandler();
    }

    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_mess_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        tv_messtd_alert = (TextView)v.findViewById(R.id.tv_messtd_alert);
        lv_mess_list_today = (MyExpandableListView)v.findViewById(R.id.lv_mess_list_today);

        tv_messp_alert = (TextView)v.findViewById(R.id.tv_messp_alert);
        lv_mess_list_pass = (MyExpandableListView)v.findViewById(R.id.lv_mess_list_pass);

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

    }
    public void setData() {
        // TODO Auto-generated method stub
        if (messListTD == null)
            messListTD = new ArrayList<ListMessData>();
        if (messAdapterTD == null)
            messAdapterTD = new ListMessAdapter(getActivity(), messListTD);
        lv_mess_list_today.setExpanded(true);
        lv_mess_list_today.setAdapter(messAdapterTD);

        if (messListP == null)
            messListP = new ArrayList<ListMessData>();
        if (messAdapterP == null)
            messAdapterP = new ListMessAdapter(getActivity(), messListP);
        lv_mess_list_pass.setExpanded(true);
        lv_mess_list_pass.setAdapter(messAdapterP);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (MainActivity.isResumeWhileCalling == 0) {
            Variables.curFrg = Constants.TAG_FRG_MESS;
            MainActivity.setIndicator(Variables.curFrg);
            if (!Functions.hasConnection(getActivity())) {
                showConvFromDB();
                Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            } else {
                prepareGetList(Variables.userToken);
            }
        }
    }
    @Override
    public void onStop() {
        db.close();
        super.onStop();
    }
    public void showConvFromDB() {
        messListTD.clear();
        messListP.clear();
        Variables.numConversationUnSeen = db.getListConv(messListTD, messListP);
        if (messListTD.size() > 0) {
            messAdapterTD.notifyDataSetChanged();
            lv_mess_list_today.setVisibility(View.VISIBLE);
            tv_messtd_alert.setVisibility(View.GONE);
        } else {
            lv_mess_list_today.setVisibility(View.GONE);
            tv_messtd_alert.setVisibility(View.VISIBLE);
        }
        if (messListP.size() > 0) {
            messAdapterP.notifyDataSetChanged();
            lv_mess_list_pass.setVisibility(View.VISIBLE);
            tv_messp_alert.setVisibility(View.GONE);
        } else {
            lv_mess_list_pass.setVisibility(View.GONE);
            tv_messp_alert.setVisibility(View.VISIBLE);
        }
    }
    private void prepareGetList(String token) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        new RequestServer().execute(Constants.URL_API_CHAT_LIST_CONVERSATION, call.createQueryStringForParameters(params));
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
                    JSONObject jOObjects = jObj.getJSONObject("objects");

                    messListTD.clear();
                    messListP.clear();
                    db.deleteConv();
                    Variables.numConversationUnSeen = 0;
                    // Private
                    JSONArray jAPrivate = jOObjects.getJSONArray("private");
                    for (int i = 0; i < jAPrivate.length(); i++) {
                        JSONObject jElm = jAPrivate.getJSONObject(i);
                        String author_id = jElm.optString("author_id");
                        String author_first_name = jElm.optString("author_first_name");
                        String author_last_name = jElm.optString("author_last_name");
                        String author_avatar = jElm.optString("author_avatar");
                        String friend_id = jElm.optString("friend_id");
                        String friend_first_name = jElm.optString("friend_first_name");
                        String friend_last_name = jElm.optString("friend_last_name");
                        String friend_avatar = jElm.optString("friend_avatar");
                        String content = jElm.optString("content");
                        String created = jElm.optString("created");
                        String type = jElm.optString("type");
                        int seen = jElm.optInt("seen");

                        ListMessData g = null;
                        if (author_id.equalsIgnoreCase(Variables.userID)) { // msg I was sent
                            g = new ListMessData(
                                    friend_id,
                                    friend_first_name,
                                    friend_last_name,
                                    "",
                                    friend_avatar,
                                    content,
                                    author_id,
                                    created,
                                    seen,
                                    1, type, "");
                            db.addConv(friend_id, friend_first_name, friend_last_name, "", content, author_id, created, String.valueOf(seen), "1", type);
                        } else { // msg Anyone sent to me
                            g = new ListMessData(
                                    author_id,
                                    author_first_name,
                                    author_last_name,
                                    "",
                                    author_avatar,
                                    content,
                                    author_id,
                                    created,
                                    seen,
                                    1, type, "");
                            db.addConv(author_id, author_first_name, author_last_name, "", content, author_id, created, String.valueOf(seen), "1", type);
                        }
                        if (Functions.getPeriod(created, "yyyy-MM-dd hh:mm:ss").equalsIgnoreCase("today"))
                            messListTD.add(g);
                        else
                            messListP.add(g);
                        if (seen > 0)
                            Variables.numConversationUnSeen++;
                    }

                    // Group
                    JSONArray jAGroup = jOObjects.getJSONArray("group");
                    for (int i = 0; i < jAGroup.length(); i++) {
                        JSONObject jElm2 = jAGroup.getJSONObject(i);
                        String group_id = jElm2.optString("group_id");
                        String group_name = jElm2.optString("group_name");
                        String group_avatar = jElm2.optString("group_avatar");
                        String author_id = jElm2.optString("author_id");
                        String content = jElm2.optString("content");
                        String created = jElm2.optString("created");
                        String type = jElm2.optString("type");
                        int seen = jElm2.optInt("seen");

                        ListMessData g = new ListMessData(
                                group_id,
                                "",
                                "",
                                group_name,
                                group_avatar,
                                content,
                                author_id,
                                created,
                                seen,
                                0, type, "");
                        db.addConv(group_id, "", "", group_name, content, author_id, created, String.valueOf(seen), "0", type);
                        if (Functions.getPeriod(created, "yyyy-MM-dd hh:mm:ss").equalsIgnoreCase("today"))
                            messListTD.add(g);
                        else
                            messListP.add(g);
                        if (seen > 0)
                            Variables.numConversationUnSeen++;
                    }

                    if (messListTD.size() > 0) {
                        messAdapterTD.notifyDataSetChanged();
                        lv_mess_list_today.setVisibility(View.VISIBLE);
                        tv_messtd_alert.setVisibility(View.GONE);
                    } else {
                        lv_mess_list_today.setVisibility(View.GONE);
                        tv_messtd_alert.setVisibility(View.VISIBLE);
                    }
                    if (messListP.size() > 0) {
                        messAdapterP.notifyDataSetChanged();
                        lv_mess_list_pass.setVisibility(View.VISIBLE);
                        tv_messp_alert.setVisibility(View.GONE);
                    } else {
                        lv_mess_list_pass.setVisibility(View.GONE);
                        tv_messp_alert.setVisibility(View.VISIBLE);
                    }

                    MainActivity.setUnseenCountTab(Variables.numConversationUnSeen);

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

    public void onLocalReceive_private_message(JSONObject data) {
        try {
            int friend_id = data.getInt("friend_id");
            if (friend_id == Integer.parseInt(Variables.userID))
                prepareGetList(Variables.userToken);
        } catch (JSONException e) {
            // JSON error
            e.printStackTrace();
            Log.e(Constants.TAG_API, data.toString() + Constants.ERR_JSON + e.getMessage());
        }
    }
    public void onLocalReceive_group_message(JSONObject data) {
        if (data != null) {
            prepareGetList(Variables.userToken);
        }
    }
    @Override
    public void onLocalNetworkChange() {
        switch (Variables.networkState) {
            case NetworkUtil.TYPE_WIFI:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetList(Variables.userToken);
                    isAlreadyLoadFromServer = 1;
                }
                break;
            case NetworkUtil.TYPE_MOBILE:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetList(Variables.userToken);
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
