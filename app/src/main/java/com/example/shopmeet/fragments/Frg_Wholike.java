package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter.ListMemberData;
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.manager.IOManager;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Wholike extends Fragment implements MainActivity.LocalRtcListener {

    private TextView tv_wh_alert;
    // Members
    private MyExpandableListView lv_wh_list_members;
    private List<ListMemberData> mbList;
    private ListMemberAdapter mbAdapter;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private HashMap<String, String> statusSaved;
    private int isAlreadyLoadFromServer;

    private String status_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_wholike, container, false);
        MainActivity.mLocalRtcListener = this;
        getBundle();
        setInitView(v);
        initVariables();

        setData();
        return v;
    }
    private void getBundle() {
        Bundle b = this.getArguments();
        if (b != null) {
            status_id = b.getString("status_id");
        }
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        call = new CallAPIHandler();
        db = new SQLiteHandler(getActivity());
        statusSaved = new HashMap<String, String>();
        isAlreadyLoadFromServer = 0;
    }
    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBarChat(Functions.getResString(getActivity(), R.string.frg_wholike_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        tv_wh_alert = (TextView) v.findViewById(R.id.tv_wh_alert);
        lv_wh_list_members = (MyExpandableListView)v.findViewById(R.id.lv_wh_list_members);

    }
    public void setData() {
        // TODO Auto-generated method stub
        if (mbList == null)
            mbList = new ArrayList<ListMemberData>();
        if (mbAdapter == null)
            mbAdapter = new ListMemberAdapter(getActivity(), mbList);
        lv_wh_list_members.setExpanded(true);
        lv_wh_list_members.setAdapter(mbAdapter);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (MainActivity.isResumeWhileCalling == 0) {
            Variables.curFrg = Constants.TAG_FRG_WHOLIKE;
            MainActivity.setIndicator(Variables.curFrg);
            if (!Functions.hasConnection(getActivity())) {
                Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            } else {
                prepareGetList(Variables.userToken, status_id);
            }
        }
    }

    private void prepareGetList(String token, String status_id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("timeline_id", status_id);
        // Using volley library --> seem faster
        //requestServer(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServer().execute(Constants.URL_API_GET_WHOLIKE, call.createQueryStringForParameters(params));
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

                    JSONArray jAStaff = jObj.getJSONArray("objects");
                    mbList.clear();
                    for (int k = 0; k < jAStaff.length(); k++) {
                        JSONObject jElm2 = jAStaff.getJSONObject(k);
                        String staff_id = jElm2.optString("staff_id");
                        String fname = jElm2.optString("first_name");
                        String lname = jElm2.optString("last_name");
                        String avatar = jElm2.optString("avatar");

                        ListMemberData mb = new ListMemberData(staff_id, "", fname, lname, avatar, "", "", 1);
                        mbList.add(mb);
                    }

                    if (mbList.size() > 0) {
                        lv_wh_list_members.setVisibility(View.VISIBLE);
                        tv_wh_alert.setVisibility(View.GONE);
                        mbAdapter.notifyDataSetChanged();
                    } else {
                        lv_wh_list_members.setVisibility(View.GONE);
                        tv_wh_alert.setVisibility(View.VISIBLE);
                    }

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
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, getActivity());
                        lv_wh_list_members.setVisibility(View.GONE);
                        tv_wh_alert.setVisibility(View.VISIBLE);
                    }
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON, getActivity());
                lv_wh_list_members.setVisibility(View.GONE);
                tv_wh_alert.setVisibility(View.VISIBLE);
            }
        }

    }



    @Override
    public void onStop() {
        db.close();
        super.onStop();
    }
    public void onLocalReceive_private_message(JSONObject data) {
    }
    public void onLocalReceive_group_message(JSONObject data) {
    }
    @Override
    public void onLocalNetworkChange() {
        switch (Variables.networkState) {
            case NetworkUtil.TYPE_WIFI:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetList(Variables.userToken, status_id);
                    isAlreadyLoadFromServer = 1;
                }
                break;
            case NetworkUtil.TYPE_MOBILE:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetList(Variables.userToken, status_id);
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
        try {
            String status = data.getString("status");
            String user_id = data.getString("user_id");
            mbAdapter.setStatusStaff(status, user_id);
        } catch (JSONException e) {
            // JSON error
            e.printStackTrace();
            Log.e(Constants.TAG_API, data.toString() + Constants.ERR_JSON + e.getMessage());
        }
    }
}
