package com.example.shopmeet.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListNoteAdapter;
import com.example.shopmeet.adapter.ListStatusAdapter;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.NoteData;
import com.example.shopmeet.model.StatusData;
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
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Timeline extends Fragment implements MainActivity.LocalRtcListener {

    // Members
    private TextView tv_row_alert_status;
    private MyExpandableListView lv_status_list;
    private List<StatusData> statusList;
    private ListStatusAdapter statusAdapter;

    private EditText edt_status_type;
    private Button bt_status_save;

    // Action bar
    private TextView tvTitle;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private int isAlreadyLoadFromServer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_status, container, false);
        MainActivity.mLocalRtcListener = this;
        setInitView(v);
        initVariables();

        setData();
        return v;
    }
    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_timeline_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        bt_status_save = (Button)v.findViewById(R.id.bt_status_save);
        edt_status_type = (EditText)v.findViewById(R.id.edt_status_type);

        bt_status_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_status_type.getText().toString().equalsIgnoreCase(""))
                    return;
                else
                    onAdd(edt_status_type.getText().toString());
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        tv_row_alert_status = (TextView)v.findViewById(R.id.tv_row_alert_status);
        lv_status_list = (MyExpandableListView)v.findViewById(R.id.lv_status_list);

    }
    private void onAdd(String content) {
        prepareAdd(Variables.userToken, content);
    }
    public void prepareAdd(String token, String content) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("content", content);
        new RequestServerAdd().execute(Constants.URL_API_STATUS_ADD, call.createQueryStringForParameters(params));
    }
    private class RequestServerAdd extends AsyncTask<String, Void, String> {
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

                    Functions.toastString(Functions.getResString(getActivity(), R.string.inform_add_success), getActivity());
                    edt_status_type.setText("");

                    JSONObject jAStatus = jObj.getJSONObject("objects");

                    int status_id = jAStatus.optInt("timeline_id");
                    String status_content = jAStatus.optString("content");
                    String staff_id = jAStatus.optString("staff_id");
                    String staff_fname = jAStatus.optString("first_name");
                    String staff_lname = jAStatus.optString("last_name");
                    String staff_avatar = jAStatus.optString("avatar");
                    String status_create_at = jAStatus.optString("date_added");
                    String status_modified = jAStatus.optString("date_modified");
                    int numLike = jAStatus.optInt("total_like");
                    int numComment = jAStatus.optInt("total_comment");
                    int isLike = jAStatus.optInt("is_like");

                    StatusData g = new StatusData(
                            String.valueOf(status_id), staff_id, staff_fname, staff_lname, staff_avatar,
                            status_create_at, status_modified, status_content, 1, numLike, numComment, isLike);
                    statusList.add(0, g);

                    db.addStatus(String.valueOf(status_id), staff_id, staff_fname, staff_lname,
                            status_create_at, status_modified, status_content,
                            String.valueOf(numLike), String.valueOf(numComment), String.valueOf(isLike));

                    if (statusList.size() > 0) {
                        statusAdapter.notifyDataSetChanged();
                        lv_status_list.setVisibility(View.VISIBLE);
                        tv_row_alert_status.setVisibility(View.GONE);
                    } else {
                        lv_status_list.setVisibility(View.GONE);
                        tv_row_alert_status.setVisibility(View.VISIBLE);
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
                        getActivity().startActivity(i);
                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
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
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        call = new CallAPIHandler();
        db = new SQLiteHandler(getActivity());
        isAlreadyLoadFromServer = 0;
    }


    public void setData() {
        // TODO Auto-generated method stub
        if (statusList == null)
            statusList = new ArrayList<StatusData>();
        if (statusAdapter == null)
            statusAdapter = new ListStatusAdapter(getActivity(), statusList);
        lv_status_list.setExpanded(true);
        lv_status_list.setAdapter(statusAdapter);

        /*statusList.clear();
        statusData mb1 = new statusData("81", "133", "fname 1", "lname 1", "http://e-space.vn/files/14.jpg", "ab@gmail.com", "3 months ago", 1);
        statusData mb2 = new statusData("82", "uname 2", "fname 2", "lname 2", "http://e-space.vn/files/17.jpg", "cd@gmail.com", "4 months ago", 1);
        statusData mb3 = new statusData("83", "uname 3", "fname 3", "lname 3", "http://e-space.vn/files/16.jpg", "ef@gmail.com", "5 months ago", 1);
        statusList.add(mb1);
        statusList.add(mb2);
        statusList.add(mb3);
        statusAdapter.notifyDataSetChanged();*/
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Variables.curFrg = Constants.TAG_FRG_TIMELINE;
        MainActivity.setIndicator(Variables.curFrg);

        if (!Functions.hasConnection(getActivity())) {
            showListStatusFromDB();
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
        } else {
            prepareGetList(Variables.userToken);
        }
    }
    private void showListStatusFromDB() {
        statusList.clear();
        db.getAllStatus(statusList);
        if (statusList.size() > 0) {
            statusAdapter.notifyDataSetChanged();
            lv_status_list.setVisibility(View.VISIBLE);
            tv_row_alert_status.setVisibility(View.GONE);
        } else {
            lv_status_list.setVisibility(View.GONE);
            tv_row_alert_status.setVisibility(View.VISIBLE);
        }
    }

    public void prepareGetList(String token) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        new RequestServer().execute(Constants.URL_API_STATUS_LIST, call.createQueryStringForParameters(params));
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

                    JSONArray jAstatuss = jObj.getJSONArray("objects");
                    statusList.clear();
                    db.deleteStatus();
                    for (int i = 0; i < jAstatuss.length(); i++) {
                        JSONObject jElm = jAstatuss.getJSONObject(i);
                        int status_id = jElm.optInt("timeline_id");
                        String status_content = jElm.optString("content");
                        String staff_id = jElm.optString("staff_id");
                        String staff_fname = jElm.optString("first_name");
                        String staff_lname = jElm.optString("last_name");
                        String staff_avatar = jElm.optString("avatar");
                        String status_create_at = jElm.optString("date_added");
                        String status_modified = jElm.optString("date_modified");
                        int numLike = jElm.optInt("total_like");
                        int numComment = jElm.optInt("total_comment");
                        int isLike = jElm.optInt("is_like");

                        StatusData g = new StatusData(
                                String.valueOf(status_id), staff_id, staff_fname, staff_lname, staff_avatar,
                                status_create_at, status_modified, status_content, 1, numLike, numComment, isLike);
                        statusList.add(g);

                        if (Functions.getPeriod(status_modified, Constants.DATETIME_FORMAT).equalsIgnoreCase("today") ||
                                Functions.getPeriod(status_modified, Constants.DATETIME_FORMAT).equalsIgnoreCase("week") ||
                                Functions.getPeriod(status_modified, Constants.DATETIME_FORMAT).equalsIgnoreCase("10day")) {
                            // About 10 day
                            db.addStatus(String.valueOf(status_id), staff_id, staff_fname, staff_lname,
                                    status_create_at, status_modified, status_content,
                                    String.valueOf(numLike), String.valueOf(numComment), String.valueOf(isLike));
                        }
                    }
                    if (statusList.size() > 0) {
                        statusAdapter.notifyDataSetChanged();
                        lv_status_list.setVisibility(View.VISIBLE);
                        tv_row_alert_status.setVisibility(View.GONE);
                    } else {
                        lv_status_list.setVisibility(View.GONE);
                        tv_row_alert_status.setVisibility(View.VISIBLE);
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
                        lv_status_list.setVisibility(View.GONE);
                        tv_row_alert_status.setVisibility(View.VISIBLE);
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
