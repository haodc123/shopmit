package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.shopmeet.AppController;
import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListMemberSelectAdapter;
import com.example.shopmeet.adapter.ListMemberSelectAdapter.ListMemberSelectData;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.MyLinkedHashMap;
import com.example.shopmeet.utils.SQLiteHandler;
import com.example.shopmeet.utils.SessionManager;
import com.example.shopmeet.view.MyDatePickerFragment;
import com.example.shopmeet.view.MyExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.manager.IOManager;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Create_Task extends Fragment implements MainActivity.LocalRtcListener {

    // Action bar
    private TextView tvTitle;

    // Search bar
    private EditText edt_per_search;
    private Button bt_per_search;

    private EditText edt_crt_content;
    private Button bt_crt_cancel, bt_crt_create;
    private TextView tv_crt_deadline;
    private ImageView img_crt_deadline;

    // Members
    private TextView tv_crt_alert;
    private MyExpandableListView lv_crt_list_members;
    public List<ListMemberSelectData> mbList;
    private ListMemberSelectAdapter mbAdapter;

    // Deadline
    private String sDeadline = "";

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    private String staff_id_selected = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_create_task, container, false);
        MainActivity.mLocalRtcListener = this;

        setInitView(v);
        initVariables();
        setData();
        return v;
    }

    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_crt_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        edt_per_search = (EditText)v.findViewById(R.id.edt_per_search);
        bt_per_search = (Button)v.findViewById(R.id.bt_per_search);

        bt_per_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearch(edt_per_search.getText().toString());
            }
        });

        edt_per_search.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edt_per_search.getText().toString().equals("")) {
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

        tv_crt_deadline = (TextView)v.findViewById(R.id.tv_crt_deadline);
        img_crt_deadline = (ImageView)v.findViewById(R.id.img_crt_deadline);
        bt_crt_cancel = (Button)v.findViewById(R.id.bt_crt_cancel);
        bt_crt_create = (Button)v.findViewById(R.id.bt_crt_create);
        edt_crt_content = (EditText)v.findViewById(R.id.edt_crt_content);

        tv_crt_alert = (TextView) v.findViewById(R.id.tv_crt_alert);
        lv_crt_list_members = (MyExpandableListView)v.findViewById(R.id.lv_crt_list_members);

        bt_crt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
        bt_crt_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateTask();
            }
        });
        img_crt_deadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        db = new SQLiteHandler(getActivity());
        call = new CallAPIHandler();
    }
    public void openDatePicker() {
        DialogFragment newFragment = new MyDatePickerFragment() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                String sMonth = String.valueOf(month).length()< 2 ? "0" + String.valueOf(month) : String.valueOf(month);
                String sDay = String.valueOf(day).length()< 2 ? "0" + String.valueOf(day) : String.valueOf(day);
                sDeadline = year + "-" + sMonth + "-" + sDay;
                tv_crt_deadline.setText(sDeadline);
            }
        };

        newFragment.show(getActivity().getFragmentManager(), "Choose deadlne");
    }
    public void onCancel() {
        getActivity().getFragmentManager().popBackStack();
    }
    public void onCreateTask() {
        if (edt_crt_content.getText().toString().equalsIgnoreCase("")) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_crt_fill_content), getActivity());
            return;
        }
        if (sDeadline.equalsIgnoreCase("")) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_crt_fill_deadline), getActivity());
            return;
        }
        int isEmptyStaff = 1;
        String staff_ids = "";
        for (int i = 0; i < mbList.size(); i++) {
            mbList.get(i).setIsSelected(mbAdapter.getIsSelected(i));
            if (mbList.get(i).getIsSelected() == 1) {
                isEmptyStaff = 0;
                staff_ids += staff_ids.equalsIgnoreCase("") ? mbList.get(i).getMb_id() : ","+mbList.get(i).getMb_id();
            }
        }
        if (isEmptyStaff == 1 && staff_ids.equalsIgnoreCase("")) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_crt_fill_staff), getActivity());
            return;
        }
        Log.e("-----------", staff_ids);
        String content = edt_crt_content.getText().toString();
        prepareCreateTask(Variables.userToken, content, staff_ids, sDeadline);
    }

    public void setData() {
        // TODO Auto-generated method stub
        if (mbList == null)
            mbList = new ArrayList<ListMemberSelectData>();
        if (mbAdapter == null)
            mbAdapter = new ListMemberSelectAdapter(getActivity(), mbList);
        lv_crt_list_members.setExpanded(true);
        //lv_crt_list_members.setChoiceMode(MyExpandableListView.CHOICE_MODE_MULTIPLE);
        lv_crt_list_members.setAdapter(mbAdapter);
    }

    private void setCustomActionBar(String title) {
        // TODO Auto-generated method stub
        getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
        getActivity().getActionBar().setCustomView(R.layout.actionbar_main);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }

    public void onSearch(String kw) {
        for (int i = 0; i < mbList.size(); i++) {
            mbList.get(i).setIsSelected(mbAdapter.getIsSelected(i));
        }
        if (kw.equals(""))
            return;
        int isHaveMBMatch = 0;
        for (int i = 0; i < mbList.size(); i++) {
            String curName = mbList.get(i).getMb_fname() + " " + mbList.get(i).getMb_lname();
            if (!curName.toLowerCase().contains(kw.toLowerCase())) {
                mbList.get(i).setIsDisplay(0);
            } else {
                mbList.get(i).setIsDisplay(1);
                isHaveMBMatch = 1;
            }
        }
        mbAdapter.notifyDataSetChanged();
        if (isHaveMBMatch == 1) {
            lv_crt_list_members.setVisibility(View.VISIBLE);
            tv_crt_alert.setVisibility(View.GONE);
        } else {
            lv_crt_list_members.setVisibility(View.GONE);
            tv_crt_alert.setVisibility(View.VISIBLE);
        }
    }
    public void refreshListAfterSearch() {
        if (mbList == null)
            return;
        for (int i = 0; i < mbList.size(); i++) {
            mbList.get(i).setIsDisplay(1);
        }
        mbAdapter.notifyDataSetChanged();
        if (mbList.size() > 0) {
            lv_crt_list_members.setVisibility(View.VISIBLE);
            tv_crt_alert.setVisibility(View.GONE);
        } else {
            lv_crt_list_members.setVisibility(View.GONE);
            tv_crt_alert.setVisibility(View.VISIBLE);
        }

    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Variables.curFrg = Constants.TAG_FRG_CREATE_TASK;
        MainActivity.setIndicator(Variables.curFrg);
        prepareGetListStaff(Variables.userToken);
    }
    /**
     * request create task
     * @param token
     */
    private void prepareCreateTask(String token, String content, String staff_ids, String deadline) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("content", content);
        params.put("staff_id_array", staff_ids);
        params.put("deadline", deadline);
        // Using volley library --> seem faster
        //requestServerCreateTask(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServerCreateTask().execute(Constants.URL_API_CREATE_TASK, call.createQueryStringForParameters(params));
    }
    private class RequestServerCreateTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayDialog();
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
            hideDialog();
            if (jsonStr == null) {
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, getActivity());
                return;
            }
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    Functions.toastString(Functions.getResString(getActivity(), R.string.alert_crt_finish_ok), getActivity());
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
                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_crt_finish_failed), getActivity());
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

    /**
     * Get list staff
     * @param token
     */
    private void prepareGetListStaff(String token) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        // Using volley library --> seem faster
        //requestServerListStaff(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServerListStaff().execute(Constants.URL_API_GET_CONTACT, call.createQueryStringForParameters(params));
    }

    private class RequestServerListStaff extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayDialog();
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
            hideDialog();
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

                    // Staff
                    JSONArray jAStaff = jOObjects.getJSONArray("staff");
                    mbList.clear();

                    for (int k = 0; k < jAStaff.length(); k++) {
                        JSONObject jElm2 = jAStaff.getJSONObject(k);
                        String staff_id = jElm2.optString("staff_id");
                        String username = jElm2.optString("username");
                        String fname = jElm2.optString("first_name");
                        String lname = jElm2.optString("last_name");
                        String avatar = jElm2.optString("staff_avatar");
                        String email = jElm2.optString("email");
                        String date_added_staff = jElm2.optString("date_added");

                        if (!staff_id.equalsIgnoreCase(Variables.userID)) {
                            ListMemberSelectData mb = new ListMemberSelectData(staff_id, username, fname, lname, avatar, email, date_added_staff, 1, 0);
                            mbList.add(mb);
                        }
                    }
                    if (mbList.size() > 0) {
                        mbAdapter.notifyDataSetChanged();
                    } else {
                        lv_crt_list_members.setVisibility(View.GONE);
                        tv_crt_alert.setVisibility(View.VISIBLE);
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
                        lv_crt_list_members.setVisibility(View.GONE);
                        tv_crt_alert.setVisibility(View.VISIBLE);
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


    public void displayDialog() {
        pDialog.setMessage(Constants.INFORM_WAIT);
        pDialog.setCancelable(true);
        pDialog.show();
    }
    public void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    public void onLocalReceive_private_message(JSONObject data) {
    }
    public void onLocalReceive_group_message(JSONObject data) {
    }
    @Override
    public void onLocalNetworkChange() {
    }
    public void onLocalOn_user_online(JSONObject data) {
    }
}
