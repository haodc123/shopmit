package com.example.shopmeet.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.example.shopmeet.AppController;
import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.ShopChoosing;
import com.example.shopmeet.SplashActivity;
import com.example.shopmeet.adapter.ListGroupAdapter;
import com.example.shopmeet.adapter.ListGroupAdapter.ListGroupData;
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

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jp.chat.video.android.manager.IOManager;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Personal extends Fragment implements MainActivity.LocalRtcListener {

    // Profile
    private ImageView img_personal_profile_arrow, img_personal_avatar;
    private TextView tv_personal_name, tv_personal_email;
    private LinearLayout ll_personal_profile;
    // Group
    private ImageView img_personal_group_arrow;
    private TextView tv_personal_group_title, tv_row_alert_group;
    private MyExpandableListView lv_personal_list_group;
    private List<ListGroupData> groupList;
    private ListGroupAdapter groupAdapter;
    // Members
    private ImageView img_personal_members_arrow;
    private TextView tv_personal_members_title, tv_row_alert_mb;
    private MyExpandableListView lv_personal_list_members;
    private List<ListMemberData> mbList;
    private ListMemberAdapter mbAdapter;

    private int isProfileExpanded = 1;
    private int isGroupExpanded = 1;
    private int isMemberExpanded = 1;

    // Search bar
    private EditText edt_per_search;
    private Button bt_per_search;

    private Button bt_group_add;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private int isAlreadyLoadFromServer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_personal, container, false);
        MainActivity.mLocalRtcListener = this;
        setInitView(v);
        initVariables();
        setData();
        Log.d("---", Variables.userJoinDate + "\\" + Variables.userToken + "\\");
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
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_personal_title));
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

        bt_group_add = (Button)v.findViewById(R.id.bt_group_add);
        img_personal_profile_arrow = (ImageView)v.findViewById(R.id.img_personal_profile_arrow);
        img_personal_avatar = (ImageView)v.findViewById(R.id.img_personal_avatar);
        tv_personal_name = (TextView)v.findViewById(R.id.tv_personal_name);
        tv_personal_email = (TextView)v.findViewById(R.id.tv_personal_email);
        ll_personal_profile = (LinearLayout)v.findViewById(R.id.ll_personal_profile);

        img_personal_group_arrow = (ImageView)v.findViewById(R.id.img_personal_group_arrow);
        tv_personal_group_title = (TextView)v.findViewById(R.id.tv_personal_group_title);
        tv_row_alert_group = (TextView)v.findViewById(R.id.tv_row_alert_group);
        lv_personal_list_group = (MyExpandableListView)v.findViewById(R.id.lv_personal_list_group);

        img_personal_members_arrow = (ImageView)v.findViewById(R.id.img_personal_members_arrow);
        tv_personal_members_title = (TextView)v.findViewById(R.id.tv_personal_members_title);
        tv_row_alert_mb = (TextView)v.findViewById(R.id.tv_row_alert_mb);
        lv_personal_list_members = (MyExpandableListView)v.findViewById(R.id.lv_personal_list_members);

        img_personal_profile_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpandabilityContent("profile");
            }
        });
        img_personal_group_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpandabilityContent("group");
            }
        });
        img_personal_members_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpandabilityContent("members");
            }
        });
        bt_group_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goCreateGroup();
            }
        });
    }
    public void goCreateGroup() {
        Frg_Create_Group frgCreateGroup = new Frg_Create_Group();

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frgCreateGroup, Constants.TAG_FRG_CREATE_GROUP)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_CREATE_GROUP;
    }
    public void onSearch(String kw) {
        if (kw.equals(""))
            return;
        /*for (int i = 0; i < mbList.size(); i++) {
            String curName = mbList.get(i).getMb_fname() + " " + mbList.get(i).getMb_lname();
            if (!curName.toLowerCase().contains(kw.toLowerCase())) {
                lv_personal_list_members.getChildAt(i).setVisibility(View.GONE);
                ViewGroup.LayoutParams params=lv_personal_list_members.getChildAt(i).getLayoutParams();
                params.height=0;
                lv_personal_list_members.getChildAt(i).setLayoutParams(params);
            } else {
                lv_personal_list_members.getChildAt(i).setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params=lv_personal_list_members.getChildAt(i).getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lv_personal_list_members.getChildAt(i).setLayoutParams(params);
            }
        }*/
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
            lv_personal_list_members.setVisibility(View.VISIBLE);
            tv_row_alert_mb.setVisibility(View.GONE);
        } else {
            lv_personal_list_members.setVisibility(View.GONE);
            tv_row_alert_mb.setVisibility(View.VISIBLE);
        }

        int isHaveGroupMatch = 0;
        for (int i = 0; i < groupList.size(); i++) {
            String curName = groupList.get(i).getGroup_name();
            if (!curName.toLowerCase().contains(kw.toLowerCase())) {
                groupList.get(i).setIsDisplay(0);
            } else {
                groupList.get(i).setIsDisplay(1);
                isHaveGroupMatch = 1;
            }
        }
        groupAdapter.notifyDataSetChanged();
        if (isHaveGroupMatch == 1) {
            lv_personal_list_group.setVisibility(View.VISIBLE);
            tv_row_alert_group.setVisibility(View.GONE);
        } else {
            lv_personal_list_group.setVisibility(View.GONE);
            tv_row_alert_group.setVisibility(View.VISIBLE);
        }
    }
    public void refreshListAfterSearch() {
        /*if (mbList == null)
            return;
        for (int i = 0; i < mbList.size(); i++) {
            if (lv_personal_list_members.getChildAt(i) != null) {
                lv_personal_list_members.getChildAt(i).setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = lv_personal_list_members.getChildAt(i).getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lv_personal_list_members.getChildAt(i).setLayoutParams(params);
            }
        }
        if (mbList.size() > 0) {
            lv_personal_list_members.setVisibility(View.VISIBLE);
            tv_row_alert_mb.setVisibility(View.GONE);
        } else {
            lv_personal_list_members.setVisibility(View.GONE);
            tv_row_alert_mb.setVisibility(View.VISIBLE);
        }*/
        if (mbList == null)
            return;
        for (int i = 0; i < mbList.size(); i++) {
            mbList.get(i).setIsDisplay(1);
        }
        mbAdapter.notifyDataSetChanged();
        if (mbList.size() > 0) {
            lv_personal_list_members.setVisibility(View.VISIBLE);
            tv_row_alert_mb.setVisibility(View.GONE);
        } else {
            lv_personal_list_members.setVisibility(View.GONE);
            tv_row_alert_mb.setVisibility(View.VISIBLE);
        }

        if (groupList == null)
            return;
        for (int i = 0; i < groupList.size(); i++) {
            groupList.get(i).setIsDisplay(1);
        }
        groupAdapter.notifyDataSetChanged();
        if (groupList.size() > 0) {
            lv_personal_list_group.setVisibility(View.VISIBLE);
            tv_row_alert_group.setVisibility(View.GONE);
        } else {
            lv_personal_list_group.setVisibility(View.GONE);
            tv_row_alert_group.setVisibility(View.VISIBLE);
        }
    }
    public void setExpandabilityContent(String part) {
        LinearLayout.LayoutParams loPar = null;
        switch (part) {
            case "profile":
                if (isProfileExpanded == 1) { // is expanding, do close
                    loPar = new LinearLayout.LayoutParams(0, 0);
                    ll_personal_profile.setLayoutParams(loPar);
                    ll_personal_profile.setVisibility(View.INVISIBLE);
                    isProfileExpanded = 0;
                    img_personal_profile_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_left));
                } else { // open
                    loPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    ll_personal_profile.setLayoutParams(loPar);
                    ll_personal_profile.setVisibility(View.VISIBLE);
                    isProfileExpanded = 1;
                    img_personal_profile_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_down));
                }
                break;
            case "group":
                if (isGroupExpanded == 1) { // is expanding, do close
                    loPar = new LinearLayout.LayoutParams(0, 0);
                    lv_personal_list_group.setLayoutParams(loPar);
                    lv_personal_list_group.setVisibility(View.INVISIBLE);
                    lv_personal_list_group.setExpanded(false);
                    isGroupExpanded = 0;
                    img_personal_group_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_left));
                } else { // open
                    loPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lv_personal_list_group.setLayoutParams(loPar);
                    lv_personal_list_group.setVisibility(View.VISIBLE);
                    lv_personal_list_group.setExpanded(true);
                    isGroupExpanded = 1;
                    img_personal_group_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_down));
                }
                break;
            case "members":
                if (isMemberExpanded == 1) { // is expanding, do close
                    loPar = new LinearLayout.LayoutParams(0, 0);
                    lv_personal_list_members.setLayoutParams(loPar);
                    lv_personal_list_members.setVisibility(View.INVISIBLE);
                    lv_personal_list_members.setExpanded(false);
                    isMemberExpanded = 0;
                    img_personal_members_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_left));
                } else { // open
                    loPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lv_personal_list_members.setLayoutParams(loPar);
                    lv_personal_list_members.setVisibility(View.VISIBLE);
                    lv_personal_list_members.setExpanded(true);
                    isMemberExpanded = 1;
                    img_personal_members_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_down));
                }
                break;
        }
    }

    public void setData() {
        // TODO Auto-generated method stub
        if (groupList == null)
            groupList = new ArrayList<ListGroupData>();
        if (groupAdapter == null)
            groupAdapter = new ListGroupAdapter(getActivity(), groupList);
        lv_personal_list_group.setExpanded(true);
        lv_personal_list_group.setAdapter(groupAdapter);

        if (mbList == null)
            mbList = new ArrayList<ListMemberData>();
        if (mbAdapter == null)
            mbAdapter = new ListMemberAdapter(getActivity(), mbList);
        lv_personal_list_members.setExpanded(true);
        lv_personal_list_members.setAdapter(mbAdapter);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (MainActivity.isResumeWhileCalling == 0) {
            Variables.curFrg = Constants.TAG_FRG_PER;
            MainActivity.setIndicator(Variables.curFrg);
            if (!Functions.hasConnection(getActivity())) {
                showContactFromDB();
                Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            } else {
                prepareGetList(Variables.userToken);
                prepareGetUnSeen(Variables.userToken);
            }
        }
        //getListGroup();
        //getListMembers();
    }
    private void showContactFromDB() {
        getListGroup();
        getListMembers();
    }
    private void getListMembers() {
        // TODO Auto-generated method stub
        mbList.clear();
        db.getAllStaff(mbList);
        if (mbList.size() > 0) {
            mbAdapter.notifyDataSetChanged();
            lv_personal_list_members.setVisibility(View.VISIBLE);
            tv_row_alert_mb.setVisibility(View.GONE);
        } else {
            lv_personal_list_members.setVisibility(View.GONE);
            tv_row_alert_mb.setVisibility(View.VISIBLE);
        }
    }

    private void getListGroup() {
        // TODO Auto-generated method stub
        groupList.clear();
        db.getAllGroup(groupList);
        if (groupList.size() > 0) {
            groupAdapter.notifyDataSetChanged();
        } else {
            lv_personal_list_group.setVisibility(View.GONE);
            tv_row_alert_group.setVisibility(View.VISIBLE);
        }
    }

    private void prepareGetList(String token) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        // Using volley library --> seem faster
        //requestServer(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServer().execute(Constants.URL_API_GET_CONTACT, call.createQueryStringForParameters(params));
    }
    /*private void requestServer(final Map<String, String> params) {
        pDialog.setMessage(Constants.INFORM_WAIT);
        pDialog.setCancelable(false);
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_API_GET_CONTACT, new Response.Listener<String>() {

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
                        JSONObject jOObjects = jObj.getJSONObject("objects");

                        // Group
                        JSONArray jAGroup = jOObjects.getJSONArray("group");
                        groupList.clear();

                        for (int i = 0; i < jAGroup.length(); i++) {
                            JSONObject jElm = jAGroup.getJSONObject(i);
                            int group_id = jElm.optInt("group_id");
                            String group_name = jElm.optString("group_name");
                            String date_added = jElm.optString("date_added");
                            String group_avatar = jElm.optString("group_avatar");

                            ListGroupData g = new ListGroupData(String.valueOf(group_id), group_name, date_added, "", group_avatar, 1);
                            groupList.add(g);
                        }
                        if (groupList.size() > 0) {
                            groupAdapter.notifyDataSetChanged();
                        } else {
                            lv_personal_list_group.setVisibility(View.GONE);
                            tv_row_alert_group.setVisibility(View.VISIBLE);
                        }

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
                                ListMemberData mb = new ListMemberData(staff_id, username, fname, lname, avatar, email, date_added_staff, 1);
                                mbList.add(mb);
                            }
                        }
                        if (mbList.size() > 0) {
                            mbAdapter.notifyDataSetChanged();
                        } else {
                            lv_personal_list_members.setVisibility(View.GONE);
                            tv_row_alert_mb.setVisibility(View.VISIBLE);
                        }

                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, getActivity());
                        lv_personal_list_group.setVisibility(View.GONE);
                        tv_row_alert_group.setVisibility(View.VISIBLE);
                        lv_personal_list_members.setVisibility(View.GONE);
                        tv_row_alert_mb.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.e(Constants.TAG_API, response.toString() + Constants.ERR_JSON + e.getMessage());
                    Functions.toastString(Constants.ERR_JSON + ": " + e.getMessage(), getActivity());
                    lv_personal_list_group.setVisibility(View.GONE);
                    tv_row_alert_group.setVisibility(View.VISIBLE);
                    lv_personal_list_members.setVisibility(View.GONE);
                    tv_row_alert_mb.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                Log.e(Constants.TAG_API, Constants.ERR_NO_DATA_FROM_SERVER + error.getMessage());
                lv_personal_list_group.setVisibility(View.GONE);
                tv_row_alert_group.setVisibility(View.VISIBLE);
                lv_personal_list_members.setVisibility(View.GONE);
                tv_row_alert_mb.setVisibility(View.VISIBLE);
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

                    // Group
                    JSONArray jAGroup = jOObjects.getJSONArray("group");
                    groupList.clear();
                    db.deleteGroups();
                    for (int i = 0; i < jAGroup.length(); i++) {
                        JSONObject jElm = jAGroup.getJSONObject(i);
                        int group_id = jElm.optInt("group_id");
                        String group_name = jElm.optString("group_name");
                        String date_added = jElm.optString("date_added");
                        String group_avatar = jElm.optString("group_avatar");

                        ListGroupData g = new ListGroupData(String.valueOf(group_id), group_name, date_added, "", group_avatar, 1);
                        groupList.add(g);

                        db.addGroup(String.valueOf(group_id), group_name, date_added, "", group_avatar);
                    }
                    if (groupList.size() > 0) {
                        groupAdapter.notifyDataSetChanged();
                    } else {
                        lv_personal_list_group.setVisibility(View.GONE);
                        tv_row_alert_group.setVisibility(View.VISIBLE);
                    }

                    // Staff
                    JSONArray jAStaff = jOObjects.getJSONArray("staff");
                    mbList.clear();
                    db.deleteStaffs();
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
                            ListMemberData mb = new ListMemberData(staff_id, username, fname, lname, avatar, email, date_added_staff, 1);
                            mbList.add(mb);

                            db.addStaff(staff_id, username, fname, lname, avatar, email, date_added_staff);
                        }
                    }
                    if (mbList.size() > 0) {
                        mbAdapter.notifyDataSetChanged();
                    } else {
                        lv_personal_list_members.setVisibility(View.GONE);
                        tv_row_alert_mb.setVisibility(View.VISIBLE);
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
                        lv_personal_list_group.setVisibility(View.GONE);
                        tv_row_alert_group.setVisibility(View.VISIBLE);
                        lv_personal_list_members.setVisibility(View.GONE);
                        tv_row_alert_mb.setVisibility(View.VISIBLE);
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
                lv_personal_list_group.setVisibility(View.GONE);
                tv_row_alert_group.setVisibility(View.VISIBLE);
                lv_personal_list_members.setVisibility(View.GONE);
                tv_row_alert_mb.setVisibility(View.VISIBLE);
            }
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
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, getActivity());
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
