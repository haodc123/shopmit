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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.shopmeet.AppController;
import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListGroupAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListMemberSelectAdapter;
import com.example.shopmeet.adapter.ListMemberSelectAdapter.ListMemberSelectData;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.MyLinkedHashMap;
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
public class Frg_Create_Group extends Fragment implements MainActivity.LocalRtcListener {

    // Action bar
    private TextView tvTitle;

    // Search bar
    private EditText edt_per_search;
    private Button bt_per_search;

    private EditText edt_crg_name;
    private Button bt_crg_cancel, bt_crg_create;

    // Store
    /*private Spinner sp_crg_store;
    private MyLinkedHashMap<Integer, String> listStore;
    ArrayAdapter<String> storeAdapter;*/

    // Members
    private TextView tv_crg_alert;
    private MyExpandableListView lv_crg_list_members;
    public List<ListMemberSelectData> mbList;
    private ListMemberSelectAdapter mbAdapter;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    private String staff_id_selected = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_create_group, container, false);
        MainActivity.mLocalRtcListener = this;
        //listStore = new MyLinkedHashMap<>();

        setInitView(v);
        initVariables();

        setData();
        return v;
    }

    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_crg_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        //sp_crg_store = (Spinner)v.findViewById(R.id.sp_crg_store);
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

        bt_crg_cancel = (Button)v.findViewById(R.id.bt_crg_cancel);
        bt_crg_create = (Button)v.findViewById(R.id.bt_crg_create);
        edt_crg_name = (EditText)v.findViewById(R.id.edt_crg_name);

        tv_crg_alert = (TextView) v.findViewById(R.id.tv_crg_alert);
        lv_crg_list_members = (MyExpandableListView)v.findViewById(R.id.lv_crg_list_members);

        bt_crg_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
        bt_crg_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateGroup();
            }
        });
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        db = new SQLiteHandler(getActivity());
        call = new CallAPIHandler();
    }
    public void onCancel() {
        getActivity().getFragmentManager().popBackStack();
    }
    public void onCreateGroup() {
        if (edt_crg_name.getText().toString().equalsIgnoreCase("")) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_fill_name), getActivity());
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
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_fill_staff), getActivity());
            return;
        }
        Log.e("-----------", staff_ids);
        String group_name = edt_crg_name.getText().toString();
        /*if (listStore.size() == 0) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_fill_store), getActivity());
            return;
        }
        int store_id = listStore.getKey(sp_crg_store.getSelectedItemPosition());*/
        prepareCreateGroup(Variables.userToken, group_name, staff_ids);
    }

    public void setData() {
        // TODO Auto-generated method stub
        if (mbList == null)
            mbList = new ArrayList<ListMemberSelectData>();
        if (mbAdapter == null)
            mbAdapter = new ListMemberSelectAdapter(getActivity(), mbList);
        lv_crg_list_members.setExpanded(true);
        lv_crg_list_members.setAdapter(mbAdapter);
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
            lv_crg_list_members.setVisibility(View.VISIBLE);
            tv_crg_alert.setVisibility(View.GONE);
        } else {
            lv_crg_list_members.setVisibility(View.GONE);
            tv_crg_alert.setVisibility(View.VISIBLE);
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
            lv_crg_list_members.setVisibility(View.VISIBLE);
            tv_crg_alert.setVisibility(View.GONE);
        } else {
            lv_crg_list_members.setVisibility(View.GONE);
            tv_crg_alert.setVisibility(View.VISIBLE);
        }

    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Variables.curFrg = Constants.TAG_FRG_CREATE_GROUP;
        MainActivity.setIndicator(Variables.curFrg);
        //getListMembers();
        prepareGetListStaff(Variables.userToken);
    }
    private void getListMembers() {
        // TODO Auto-generated method stub
        mbList.clear();
        ListMemberSelectData mb1 = new ListMemberSelectData("member id 1", "uname 1", "fname 1", "lname 1", "http://e-space.vn/files/14.jpg", "ab@gmail.com", "3 months ago", 1, 0);
        ListMemberSelectData mb2 = new ListMemberSelectData("member id 2", "uname 2", "fname 2", "lname 2", "http://e-space.vn/files/17.jpg", "cd@gmail.com", "4 months ago", 1, 0);
        ListMemberSelectData mb3 = new ListMemberSelectData("member id 3", "uname 3", "fname 3", "lname 3", "http://e-space.vn/files/16.jpg", "ef@gmail.com", "5 months ago", 1, 0);
        mbList.add(mb1);
        mbList.add(mb2);
        mbList.add(mb3);
        mbAdapter.notifyDataSetChanged();
    }

    /**
     * request create group
     * @param token
     */
    private void prepareCreateGroup(String token, String group_name, String staff_ids) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("group_name", group_name);
        params.put("staff_id_array", staff_ids);
        params.put("app", "1");
        // Using volley library --> seem faster
        //requestServerCreateGroup(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServerCreateGroup().execute(Constants.URL_API_CREATE_GROUP, call.createQueryStringForParameters(params));
    }
    /*private void requestServerCreateGroup(final Map<String, String> params) {
        displayDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_API_CREATE_GROUP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("---Response: ", response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_finish_ok), getActivity());

                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_finish_failed), getActivity());
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
                hideDialog();
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

    private class RequestServerCreateGroup extends AsyncTask<String, Void, String> {
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

                    Functions.toastString(Functions.getResString(getActivity(), R.string.alert_finish_ok), getActivity());
                    ((MainActivity)getActivity()).openTab(Constants.TAG_FRG_PER);

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
                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_finish_failed), getActivity());
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_GETTING + errorMsg);
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
    /*private void requestServerListStaff(final Map<String, String> params) {
        displayDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_API_GET_CONTACT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("---Response: ", response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
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
                            lv_crg_list_members.setVisibility(View.GONE);
                            tv_crg_alert.setVisibility(View.VISIBLE);
                        }

                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, getActivity());
                        lv_crg_list_members.setVisibility(View.GONE);
                        tv_crg_alert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.e(Constants.TAG_API, response.toString() + Constants.ERR_JSON + e.getMessage());
                    Functions.toastString(Constants.ERR_JSON + ": " + e.getMessage(), getActivity());
                    lv_crg_list_members.setVisibility(View.GONE);
                    tv_crg_alert.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Log.e(Constants.TAG_API, Constants.ERR_NO_DATA_FROM_SERVER + error.getMessage());
                lv_crg_list_members.setVisibility(View.GONE);
                tv_crg_alert.setVisibility(View.VISIBLE);
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
                        lv_crg_list_members.setVisibility(View.GONE);
                        tv_crg_alert.setVisibility(View.VISIBLE);
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
                        lv_crg_list_members.setVisibility(View.GONE);
                        tv_crg_alert.setVisibility(View.VISIBLE);
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_GETTING + errorMsg);
                    }

                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                //Functions.toastString(Constants.ERR_JSON, getActivity());
                lv_crg_list_members.setVisibility(View.GONE);
                tv_crg_alert.setVisibility(View.VISIBLE);
            }
        }

    }


    /*private void prepareGetListStore(String token) {
        if (!Functions.hasConnection(getActivity())) {
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        new RequestServerListStore().execute(Constants.URL_API_GET_SHOPS, call.createQueryStringForParameters(params));
    }
    private class RequestServerListStore extends AsyncTask<String, Void, String> {

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

                    JSONArray jAStores = jObj.getJSONArray("stores");

                    for (int i = 0; i < jAStores.length(); i++) {
                        JSONObject jElm = jAStores.getJSONObject(i);
                        int s_id = jElm.optInt("store_id");
                        String s_name = jElm.optString("store_name");
                        int is_s_primary = jElm.optInt("isPrimary");
                        listStore.put(s_id, s_name);
                    }
                    List<String> sArray =  new ArrayList<String>();
                    for (int i = 0; i <listStore.size(); i++) {
                        sArray.add(listStore.getValue(i));
                    }
                    storeAdapter = new ArrayAdapter<String>(
                            getActivity(), android.R.layout.simple_spinner_item, sArray);
                    sp_crg_store.setAdapter(storeAdapter);
                    storeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                } else {
                    // Error in getting. Get the error message
                    String errorMsg = jObj.getString("message");
                    Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                    Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, getActivity());
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON + ": " + e.getMessage(), getActivity());
            }
        }
    }*/

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
