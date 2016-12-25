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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
import com.example.shopmeet.view.MyExpandableListView;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

    // Action bar
    private TextView tvTitle;
    // Search bar
    private EditText edt_per_search;
    private Button bt_per_search;

    private Button bt_group_add;

    private CallAPIHandler call;
    private ProgressDialog pDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_personal, container, false);

        setInitView(v);
        call = new CallAPIHandler();
        setData();
        Log.d("---", Variables.userJoinDate + "\\" + Variables.userToken + "\\");
        return v;
    }

    private void setInitView(View v) {
        // TODO Auto-generated method stub
        setCustomActionBar(Variables.userFName + " " + Variables.userLName);
        MainActivity.ll_icon_tab.setVisibility(View.VISIBLE);
        MainActivity.ll_indicator.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams lParam = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        lParam.setMargins(0, Functions.dpToPx(50), 0, 0);
        MainActivity.fragment_container.setLayoutParams(lParam);

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
        for (int i = 0; i < mbList.size(); i++) {
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
        }
    }
    public void refreshListAfterSearch() {
        if (mbList == null)
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
        Variables.curFrg = Constants.TAG_FRG_PER;
        MainActivity.setIndicator(Variables.curFrg);
        //prepareGetList(Variables.userToken);
        getListGroup();
        getListMembers();
	}

    private void getListMembers() {
        // TODO Auto-generated method stub
        mbList.clear();
        ListMemberData mb1 = new ListMemberData("81", "uname 1", "fname 1", "lname 1", "http://e-space.vn/files/14.jpg", "ab@gmail.com", "3 months ago");
        ListMemberData mb2 = new ListMemberData("82", "uname 2", "fname 2", "lname 2", "http://e-space.vn/files/17.jpg", "cd@gmail.com", "4 months ago");
        ListMemberData mb3 = new ListMemberData("83", "uname 3", "fname 3", "lname 3", "http://e-space.vn/files/16.jpg", "ef@gmail.com", "5 months ago");
        mbList.add(mb1);
        mbList.add(mb2);
        mbList.add(mb3);
        mbAdapter.notifyDataSetChanged();
    }

    private void getListGroup() {
        // TODO Auto-generated method stub
        groupList.clear();
        ListGroupData g1 = new ListGroupData("91", "shop name 1", "2016-02-03", "staff1, staff2, staff3", "http://icons.iconarchive.com/icons/blackvariant/button-ui-system-folders-drives/1024/Group-icon.png");
        ListGroupData g2 = new ListGroupData("92", "shop name 2", "2016-02-03", "staff4, staff5, staff6", "");
        groupList.add(g1);
        groupList.add(g2);
        groupAdapter.notifyDataSetChanged();
    }

    private void prepareGetList(String token, int shop_id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("store_id", String.valueOf(shop_id));
        // Using volley library --> seem faster
        requestServer(params);
        // Using HttpURLConnection (CallAPIHandler)
        //new GetList().execute(Constants.URL_API_GET_CONTACT, call.createQueryStringForParameters(params));
    }
    private void requestServer(final Map<String, String> params) {
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

                        // Group

                        JSONArray jAGroups = jObj.getJSONArray("groups");
                        groupList.clear();

                        for (int i = 0; i < jAGroups.length(); i++) {
                            JSONObject jElm = jAGroups.getJSONObject(i);
                            int group_id = jElm.optInt("group_id");
                            String group_name = jElm.optString("group_name");
                            String date_added = jElm.optString("date_added");
                            String group_avatar = jElm.optString("group_avatar");

                            ListGroupData g = new ListGroupData(String.valueOf(group_id), group_name, date_added, "", group_avatar);
                            groupList.add(g);
                        }
                        if (groupList.size() > 0) {
                            groupAdapter.notifyDataSetChanged();
                        } else {
                            lv_personal_list_group.setVisibility(View.GONE);
                            tv_row_alert_group.setVisibility(View.VISIBLE);
                        }


                        // Staff

                        JSONArray jAStaffs = jObj.getJSONArray("staffs");
                        List<ListMemberData> mbsList = new ArrayList<ListMemberData>();

                        for (int k = 0; k < jAStaffs.length(); k++) {
                            JSONObject jElm2 = jAStaffs.getJSONObject(k);
                            String staff_id = jElm2.optString("staff_id");
                            String username = Functions.getCleanEmptyString(jElm2.optString("username"));
                            String fname = Functions.getCleanEmptyString(jElm2.optString("first_name"));
                            String lname = Functions.getCleanEmptyString(jElm2.optString("last_name"));
                            String avatar = "";//jElm2.optString("avatar");
                            String email = jElm2.optString("email");
                            String date_added_staff = jElm2.optString("date_added");

                            if (!staff_id.equalsIgnoreCase(Variables.userID)) {
                                ListMemberData mb = new ListMemberData(staff_id, username, fname, lname, avatar, email, date_added_staff);
                                mbsList.add(mb);
                            }
                        }
                        if (mbsList.size() > 0) {
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
    }

    /*private class GetList extends AsyncTask<String, Void, String> {
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

                    JSONArray jAGroups = jObj.getJSONArray("groups");
                    groupsList.clear();

                    for (int i = 0; i < jAGroups.length(); i++) {
                        JSONObject jElm = jAGroups.getJSONObject(i);
                        int group_id = jElm.optInt("group_id");
                        String group_name = jElm.optString("group_name");
                        String date_added = jElm.optString("date_added");

                        JSONArray jAStaffs = jElm.getJSONArray("staffs");
                        List<ListMemberData> mbsList = new ArrayList<ListMemberData>();

                        for (int k = 0; k < jAStaffs.length(); k++) {
                            JSONObject jElm2 = jAStaffs.getJSONObject(k);
                            String staff_id = jElm2.optString("staff_id");
                            String username = Functions.getCleanEmptyString(jElm2.optString("username"));
                            String fname = Functions.getCleanEmptyString(jElm2.optString("first_name"));
                            String lname = Functions.getCleanEmptyString(jElm2.optString("last_name"));
                            String avatar = "";//jElm2.optString("avatar");
                            String email = jElm2.optString("email");
                            String date_added_staff = jElm2.optString("date_added");

                            ListMemberData mb = new ListMemberData(staff_id, username, fname, lname, avatar, email, date_added_staff);
                            mbsList.add(mb);
                        }
                        ListGroupData g = new ListGroupData(String.valueOf(group_id), group_name, date_added, mbsList.size(), mbsList);
                        groupsList.add(g);
                    }
                    if (groupsList.size() > 0) {
                        groupsAdapter.notifyDataSetChanged();
                    } else {
                        lv_personal_list_groups.setVisibility(View.GONE);
                        tv_personal_alert.setVisibility(View.VISIBLE);
                    }

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


    public void onLocalReceive_private_message(JSONObject data) {
    }
    public void onLocalReceive_group_message(JSONObject data) {
    }
    @Override
    public void onLocalNetworkChange() {
    }
}
