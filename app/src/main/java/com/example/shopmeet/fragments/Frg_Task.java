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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shopmeet.AppController;
import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListGroupAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListTaskAdapter;
import com.example.shopmeet.adapter.ListTaskAdapter.ListTaskData;
import com.example.shopmeet.adapter.ListTaskAdapter;
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
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Task extends Fragment implements MainActivity.LocalRtcListener {

    // Members
    private TextView tv_task_all, tv_task_today, tv_task_week, tv_row_alert_task;
    private MyExpandableListView lv_task_list;
    private List<ListTaskData> taskList;
    private ListTaskAdapter taskAdapter;

    // Action bar
    private TextView tvTitle;

    private Button bt_task_add;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private int isAlreadyLoadFromServer;
    private String curPeriod = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_task, container, false);
        MainActivity.mLocalRtcListener = this;
        setInitView(v);
        initVariables();

        setData();
        return v;
    }

    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_task_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        bt_task_add = (Button)v.findViewById(R.id.bt_task_add);
        tv_task_all = (TextView)v.findViewById(R.id.tv_task_all);
        tv_task_today = (TextView)v.findViewById(R.id.tv_task_today);
        tv_task_week = (TextView)v.findViewById(R.id.tv_task_week);
        tv_row_alert_task = (TextView)v.findViewById(R.id.tv_row_alert_task);
        lv_task_list = (MyExpandableListView)v.findViewById(R.id.lv_task_list);

        tv_task_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curPeriod = "all";
                if (!Functions.hasConnection(getActivity())) {
                    showTaskFromDB(curPeriod);
                    Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
                } else {
                    prepareGetList(Variables.userToken, curPeriod);
                }
                tv_task_all.setTextColor(getActivity().getResources().getColor(R.color.dark_global));
                tv_task_today.setTextColor(getActivity().getResources().getColor(R.color.gray_bright_global));
                tv_task_week.setTextColor(getActivity().getResources().getColor(R.color.gray_bright_global));
            }
        });
        tv_task_today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curPeriod = "today";
                if (!Functions.hasConnection(getActivity())) {
                    showTaskFromDB(curPeriod);
                    Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
                } else {
                    prepareGetList(Variables.userToken, curPeriod);
                }
                tv_task_today.setTextColor(getActivity().getResources().getColor(R.color.dark_global));
                tv_task_all.setTextColor(getActivity().getResources().getColor(R.color.gray_bright_global));
                tv_task_week.setTextColor(getActivity().getResources().getColor(R.color.gray_bright_global));
            }
        });
        tv_task_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curPeriod = "week";
                if (!Functions.hasConnection(getActivity())) {
                    showTaskFromDB(curPeriod);
                    Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
                } else {
                    prepareGetList(Variables.userToken, curPeriod);
                }
                tv_task_week.setTextColor(getActivity().getResources().getColor(R.color.dark_global));
                tv_task_all.setTextColor(getActivity().getResources().getColor(R.color.gray_bright_global));
                tv_task_today.setTextColor(getActivity().getResources().getColor(R.color.gray_bright_global));
            }
        });
        bt_task_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goCreateTask();
            }
        });
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        call = new CallAPIHandler();
        db = new SQLiteHandler(getActivity());
        isAlreadyLoadFromServer = 0;
        curPeriod = "all";
    }
    public void goCreateTask() {
        Frg_Create_Task frgCreateTask = new Frg_Create_Task();

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frgCreateTask, Constants.TAG_FRG_CREATE_TASK)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_CREATE_TASK;
    }

    private void setCustomActionBar(String title) {
        // TODO Auto-generated method stub
        getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
        getActivity().getActionBar().setCustomView(R.layout.actionbar_main);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }

    public void setData() {
        // TODO Auto-generated method stub
        if (taskList == null)
            taskList = new ArrayList<ListTaskData>();
        if (taskAdapter == null)
            taskAdapter = new ListTaskAdapter(getActivity(), taskList);
        lv_task_list.setExpanded(true);
        lv_task_list.setAdapter(taskAdapter);

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Variables.curFrg = Constants.TAG_FRG_TASK;
        MainActivity.setIndicator(Variables.curFrg);

        if (!Functions.hasConnection(getActivity())) {
            showTaskFromDB(curPeriod);
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
        } else {
            prepareGetList(Variables.userToken, curPeriod);
        }
    }
    private void showTaskFromDB(String period) {
        // TODO Auto-generated method stub
        taskList.clear();
        db.getPeriodTask(taskList, period);
        if (taskList.size() > 0) {
            taskAdapter.notifyDataSetChanged();
            lv_task_list.setVisibility(View.VISIBLE);
            tv_row_alert_task.setVisibility(View.GONE);
        } else {
            lv_task_list.setVisibility(View.GONE);
            tv_row_alert_task.setVisibility(View.VISIBLE);
        }
    }

    private void prepareGetList(String token, String period) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("period", period);
        // Using volley library --> seem faster
        //requestServer(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServer().execute(Constants.URL_API_LIST_TASK, call.createQueryStringForParameters(params));
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

                    JSONArray jATasks = jObj.getJSONArray("tasks");
                    taskList.clear();

                    if (curPeriod.equalsIgnoreCase("all"))
                        db.deleteTask();

                    for (int i = 0; i < jATasks.length(); i++) {
                        JSONObject jElm = jATasks.getJSONObject(i);
                        int task_id = jElm.optInt("task_id");
                        String task_staff_id = jElm.optString("id");
                        String task_content = jElm.optString("content");
                        String task_require_person_id = jElm.optString("person_id");
                        String task_require_person_fname = jElm.optString("person_first_name");
                        String task_require_person_lname = jElm.optString("person_last_name");
                        String task_create_at = jElm.optString("date_added");
                        String task_deadline = jElm.optString("deadline");
                        int task_status = jElm.optInt("status");

                        ListTaskData g = new ListTaskData(String.valueOf(task_id), task_staff_id, task_content, task_require_person_id,
                                task_require_person_fname, task_require_person_lname, task_create_at, task_deadline, task_status);
                        taskList.add(g);

                        if (curPeriod.equalsIgnoreCase("all")) {
                            db.addTask(String.valueOf(task_id), task_staff_id, task_content, task_require_person_id,
                                    task_require_person_fname, task_require_person_lname, task_create_at, task_deadline,
                                    String.valueOf(task_status), "", "");
                        }
                    }
                    if (taskList.size() > 0) {
                        taskAdapter.notifyDataSetChanged();
                        lv_task_list.setVisibility(View.VISIBLE);
                        tv_row_alert_task.setVisibility(View.GONE);
                    } else {
                        lv_task_list.setVisibility(View.GONE);
                        tv_row_alert_task.setVisibility(View.VISIBLE);
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
                        lv_task_list.setVisibility(View.GONE);
                        tv_row_alert_task.setVisibility(View.VISIBLE);
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
                    prepareGetList(Variables.userToken, curPeriod);
                    isAlreadyLoadFromServer = 1;
                }
                break;
            case NetworkUtil.TYPE_MOBILE:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetList(Variables.userToken, curPeriod);
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
