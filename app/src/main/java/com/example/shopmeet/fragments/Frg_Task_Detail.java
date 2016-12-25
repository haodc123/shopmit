package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.shopmeet.AppController;
import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListGroupAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter.ListMemberData;
import com.example.shopmeet.adapter.ListTaskAdapter.ListTaskData;
import com.example.shopmeet.adapter.ListTaskAdapter;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.MyLinkedHashMap;
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
public class Frg_Task_Detail extends Fragment implements MainActivity.LocalRtcListener {

    // Action bar
    private TextView tvTitle;

    private TextView row_tkd_created, row_tkd_content, row_tkd_require_person, row_tkd_deadline, row_tkd_status, tv_tkd_alert;
    private EditText edt_tkd_comment;
    private Button bt_tkd_cancel, bt_tkd_complete;

    // Members
    private TextView tv_crg_alert;
    private MyExpandableListView lv_tkd_list_members;
    public List<ListMemberData> mbList;
    private ListMemberAdapter mbAdapter;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private HashMap<String, String> taskSaved;
    private SQLiteHandler db;
    private SessionManager session;

    private String task_id, task_staff_id, task_comment;
    private int cur_status; // current status
    private int isAlreadyLoadFromServer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_task_detail, container, false);
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
            task_id = b.getString("task_id");
            task_staff_id = b.getString("task_staff_id");
        }
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        call = new CallAPIHandler();
        db = new SQLiteHandler(getActivity());
        taskSaved = new HashMap<String, String>();
        isAlreadyLoadFromServer = 0;
    }
    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_tkd_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        row_tkd_created = (TextView) v.findViewById(R.id.row_tkd_created);
        row_tkd_content = (TextView) v.findViewById(R.id.row_tkd_content);
        row_tkd_require_person = (TextView) v.findViewById(R.id.row_tkd_require_person);
        row_tkd_deadline = (TextView) v.findViewById(R.id.row_tkd_deadline);
        row_tkd_status = (TextView)v.findViewById(R.id.row_tkd_status);
        edt_tkd_comment = (EditText)v.findViewById(R.id.edt_tkd_comment);

        tv_tkd_alert = (TextView) v.findViewById(R.id.tv_tkd_alert);
        lv_tkd_list_members = (MyExpandableListView)v.findViewById(R.id.lv_tkd_list_members);

        bt_tkd_cancel = (Button)v.findViewById(R.id.bt_tkd_cancel);
        bt_tkd_complete = (Button)v.findViewById(R.id.bt_tkd_complete);

        bt_tkd_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
        bt_tkd_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onComplete();
            }
        });
    }

    public void onCancel() {
        getActivity().getFragmentManager().popBackStack();
    }
    public void onComplete() {
        if (cur_status == ListTaskData.TASK_STATUS_COMPLETED) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.tkd_alert_completed), getActivity());
            return;
        }
        if (cur_status == ListTaskData.TASK_STATUS_UNCOMPLETED) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.tkd_alert_uncompleted), getActivity());
            return;
        }
        task_comment = edt_tkd_comment.getText().toString();
        if (task_comment.equalsIgnoreCase("")) {
            prepareUpdateTask(Variables.userToken, task_staff_id, ListTaskData.TASK_STATUS_COMPLETED, "");
        } else {
            prepareUpdateTask(Variables.userToken, task_staff_id, ListTaskData.TASK_STATUS_UNCOMPLETED, task_comment);
        }

    }

    public void setData() {
        // TODO Auto-generated method stub
        if (mbList == null)
            mbList = new ArrayList<ListMemberData>();
        if (mbAdapter == null)
            mbAdapter = new ListMemberAdapter(getActivity(), mbList);
        lv_tkd_list_members.setExpanded(true);
        lv_tkd_list_members.setAdapter(mbAdapter);
    }

    private void setCustomActionBar(String title) {
        // TODO Auto-generated method stub
        getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
        getActivity().getActionBar().setCustomView(R.layout.actionbar_main);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (MainActivity.isResumeWhileCalling == 0) {
            Variables.curFrg = Constants.TAG_FRG_TASK_DETAIL;
            MainActivity.setIndicator(Variables.curFrg);
            if (!Functions.hasConnection(getActivity())) {
                showTaskDetailFromDB(task_staff_id);
                Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            } else {
                prepareGetTaskDetail(Variables.userToken, task_staff_id);
            }
        }
    }
    private void showTaskDetailFromDB(String task_staff_id) {
        taskSaved = db.getTaskDetails(task_staff_id);

        String created = taskSaved.get(SQLiteHandler.KEY_TASK_CREATED);
        String content = taskSaved.get(SQLiteHandler.KEY_TASK_CONTENT);
        String person_fname = taskSaved.get(SQLiteHandler.KEY_TASK_RP_FNAME);
        String person_lname = taskSaved.get(SQLiteHandler.KEY_TASK_RP_LNAME);
        String deadline = taskSaved.get(SQLiteHandler.KEY_TASK_DEADLINE);
        String comment = taskSaved.get(SQLiteHandler.KEY_TASK_COMMENT);
        String staff_arr = taskSaved.get(SQLiteHandler.KEY_TASK_STAFF_ARR);
        cur_status = Integer.parseInt(taskSaved.get(SQLiteHandler.KEY_TASK_STATUS));

        row_tkd_created.setText(created);
        row_tkd_content.setText(content);
        row_tkd_require_person.setText(person_fname+" "+person_lname);
        row_tkd_deadline.setText(deadline);
        edt_tkd_comment.setText(comment);
        setCurrentStatus();

        mbList.clear();
        db.getSomeStaff(mbList, staff_arr);
        if (mbList.size() > 0) {
            mbAdapter.notifyDataSetChanged();
            /*lv_tkd_list_members.setVisibility(View.VISIBLE);
            tv_tkd_alert.setVisibility(View.GONE);*/
        } else {
            /*lv_tkd_list_members.setVisibility(View.GONE);
            tv_tkd_alert.setVisibility(View.VISIBLE);*/
        }
    }
    private void prepareGetTaskDetail(String token, String task_staff_id) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("id", task_staff_id);
        // Using volley library --> seem faster
        //requestServerTaskDetail(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServerTaskDetail().execute(Constants.URL_API_DETAIL_TASK, call.createQueryStringForParameters(params));
    }
    private class RequestServerTaskDetail extends AsyncTask<String, Void, String> {
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
                    JSONObject jOTask = jObj.getJSONObject("task");
                    String created = jOTask.getString("date_added");
                    String content = jOTask.getString("content");
                    String person_fname = jOTask.getString("person_first_name");
                    String person_lname = jOTask.getString("person_last_name");
                    String deadline = jOTask.getString("deadline");
                    String comment = jOTask.getString("comment");
                    cur_status = jOTask.getInt("status");

                    row_tkd_created.setText(created);
                    row_tkd_content.setText(content);
                    row_tkd_require_person.setText(person_fname+" "+person_lname);
                    row_tkd_deadline.setText(deadline);
                    edt_tkd_comment.setText(comment);
                    setCurrentStatus();

                    // Staff
                    String staff_arr = "";
                    JSONArray jAStaffs = jOTask.getJSONArray("staffs");
                    mbList.clear();

                    for (int k = 0; k < jAStaffs.length(); k++) {
                        JSONObject jElm2 = jAStaffs.getJSONObject(k);
                        String staff_id = jElm2.optString("staff_id");
                        String username = jElm2.optString("username");
                        String fname = jElm2.optString("first_name");
                        String lname = jElm2.optString("last_name");
                        String avatar = jElm2.optString("staff_avatar");
                        String email = jElm2.optString("email");
                        String date_added_staff = jElm2.optString("date_added");

                        if (!staff_id.equalsIgnoreCase(Variables.userID)) {
                            ListMemberData mb = new ListMemberData(staff_id, username, fname, lname, avatar, email, date_added_staff, 1);
                            mbList.add(mb);
                        }
                        staff_arr += staff_arr.equals("") ? ""+staff_id+"" : ","+staff_id + "";
                    }
                    if (mbList.size() > 0) {
                        lv_tkd_list_members.setVisibility(View.VISIBLE);
                        tv_tkd_alert.setVisibility(View.GONE);
                        mbAdapter.notifyDataSetChanged();
                    } else {
                        lv_tkd_list_members.setVisibility(View.GONE);
                        tv_tkd_alert.setVisibility(View.VISIBLE);
                    }

                    db.updateTask(task_staff_id, String.valueOf(cur_status), comment, staff_arr);

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
                        lv_tkd_list_members.setVisibility(View.GONE);
                        tv_tkd_alert.setVisibility(View.VISIBLE);
                    }
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON, getActivity());
                lv_tkd_list_members.setVisibility(View.GONE);
                tv_tkd_alert.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setCurrentStatus() {
        switch (cur_status) {
            case ListTaskData.TASK_STATUS_OUT_OF_DATE:
                row_tkd_status.setText(Functions.getResString(getActivity(), R.string.status_ofd));
                edt_tkd_comment.setEnabled(true);
                edt_tkd_comment.setBackground(getActivity().getResources().getDrawable(R.drawable.edt_bg_schema_blue));
                break;
            case ListTaskData.TASK_STATUS_COMPLETED:
                row_tkd_status.setText(Functions.getResString(getActivity(), R.string.status_completed));
                edt_tkd_comment.setEnabled(false);
                edt_tkd_comment.setBackground(getActivity().getResources().getDrawable(R.drawable.edt_bg_schema_blue_disable));
                bt_tkd_complete.setEnabled(false);
                bt_tkd_complete.setBackgroundColor(getActivity().getResources().getColor(R.color.gray_bright_global));
                break;
            case ListTaskData.TASK_STATUS_UNCOMPLETED:
                row_tkd_status.setText(Functions.getResString(getActivity(), R.string.status_uncompleted));
                edt_tkd_comment.setEnabled(false);
                edt_tkd_comment.setBackground(getActivity().getResources().getDrawable(R.drawable.edt_bg_schema_blue_disable));
                bt_tkd_complete.setEnabled(false);
                bt_tkd_complete.setBackgroundColor(getActivity().getResources().getColor(R.color.gray_bright_global));
                break;
            default:
                row_tkd_status.setText(Functions.getResString(getActivity(), R.string.status_waiting));
                edt_tkd_comment.setEnabled(true);
                edt_tkd_comment.setBackground(getActivity().getResources().getDrawable(R.drawable.edt_bg_schema_blue));
                break;
        }
    }
    /**
     * Update task
     */
    private void prepareUpdateTask(String token, String task_staff_id, int status_update, String comment) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        cur_status = status_update;
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("id", task_staff_id);
        params.put("status", String.valueOf(status_update));
        params.put("comment", comment);
        // Using volley library --> seem faster
        //requestServerUpdateTask(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServerUpdateTask().execute(Constants.URL_API_UPDATE_TASK, call.createQueryStringForParameters(params));
    }
    private class RequestServerUpdateTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(Constants.INFORM_WAIT);
            pDialog.setCancelable(false);
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

                    Functions.toastString(Functions.getResString(getActivity(), R.string.alert_update_task_ok), getActivity());
                    setCurrentStatus();

                    db.updateTask2(task_staff_id, String.valueOf(cur_status), edt_tkd_comment.getText().toString());
                    ((MainActivity)getActivity()).openTab(Constants.TAG_FRG_TASK);

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
                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_update_task_failed), getActivity());
                    }
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON, getActivity());
            }
        }

    }
    /*private void requestServerUpdateTask(final Map<String, String> params) {
        pDialog.setMessage(Constants.INFORM_WAIT);
        pDialog.setCancelable(false);
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_API_UPDATE_TASK, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("---Response: ", response.toString());
                if (pDialog.isShowing())
                    pDialog.dismiss();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_update_task_ok), getActivity());

                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_update_task_failed), getActivity());
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.e(Constants.TAG_API, response.toString() + Constants.ERR_JSON + e.getMessage());
                    Functions.toastString(Constants.ERR_JSON + ": " + e.getMessage(), getActivity());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                Log.e(Constants.TAG_API, Constants.ERR_NO_DATA_FROM_SERVER + error.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                return params;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, Constants.TAG_API);
    }*/




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
                    prepareGetTaskDetail(Variables.userToken, task_staff_id);
                    isAlreadyLoadFromServer = 1;
                }
                break;
            case NetworkUtil.TYPE_MOBILE:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetTaskDetail(Variables.userToken, task_staff_id);
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
