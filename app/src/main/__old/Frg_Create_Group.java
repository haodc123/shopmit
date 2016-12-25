package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.shopmeet.AppController;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter.ListMemberData;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.CallAPIHandler;
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

/**
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Create_Group extends Fragment implements MainActivity.LocalRtcListener {

    // Action bar
    private TextView tvTitle;

    // Search bar
    private EditText edt_per_search;
    private Button bt_per_search;

    private TextView tv_crg_name, tv_crg_staff;
    private EditText edt_crg_name;
    private Button bt_crg_cancel, bt_crg_create;
    // Members
    private TextView tv_crg_alert;
    private MyExpandableListView lv_crg_list_members;
    private List<ListMemberData> mbList;
    private ListMemberAdapter mbAdapter;

    private CallAPIHandler call;
    private ProgressDialog pDialog;

    private String staff_id_selected = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_create_group, container, false);
        setInitView(v);
        call = new CallAPIHandler();
        setData();
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

        bt_crg_cancel = (Button)v.findViewById(R.id.bt_crg_cancel);
        bt_crg_create = (Button)v.findViewById(R.id.bt_crg_create);
        tv_crg_name = (TextView) v.findViewById(R.id.tv_crg_name);
        tv_crg_staff = (TextView) v.findViewById(R.id.tv_crg_staff);
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

    public void onCancel() {
        getActivity().getFragmentManager().popBackStack();
    }
    public void onCreateGroup() {

    }
    private void prepareCreateGroup(String group_name, String staff_ids) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("group_name", group_name);
        params.put("staff_id_array", staff_ids);
        // Using volley library --> seem faster
        requestServer(params);
        // Using HttpURLConnection (CallAPIHandler)
        //new GetList().execute(Constants.URL_API_GET_GROUPS, call.createQueryStringForParameters(params));
    }
    private void requestServer(final Map<String, String> params) {
        pDialog.setMessage(Constants.INFORM_WAIT);
        pDialog.setCancelable(false);
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_API_CREATE_GROUP, new Response.Listener<String>() {

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

                        String msg = jObj.getString("message");
                        Functions.toastString(msg, getActivity());

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
    public void setData() {
        // TODO Auto-generated method stub
        if (mbList == null)
            mbList = new ArrayList<ListMemberData>();
        if (mbAdapter == null)
            mbAdapter = new ListMemberAdapter(getActivity(), mbList);
        lv_crg_list_members.setExpanded(true);
        lv_crg_list_members.setAdapter(mbAdapter);
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
        if (kw.equals(""))
            return;
        for (int i = 0; i < mbList.size(); i++) {
            String curName = mbList.get(i).getMb_fname() + " " + mbList.get(i).getMb_lname();
            if (!curName.toLowerCase().contains(kw.toLowerCase())) {
                lv_crg_list_members.getChildAt(i).setVisibility(View.GONE);
                ViewGroup.LayoutParams params=lv_crg_list_members.getChildAt(i).getLayoutParams();
                params.height=0;
                lv_crg_list_members.getChildAt(i).setLayoutParams(params);
            } else {
                lv_crg_list_members.getChildAt(i).setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params=lv_crg_list_members.getChildAt(i).getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lv_crg_list_members.getChildAt(i).setLayoutParams(params);
            }
        }
    }
    public void refreshListAfterSearch() {
        if (mbList == null)
            return;
        for (int i = 0; i < mbList.size(); i++) {
            if (lv_crg_list_members.getChildAt(i) != null) {
                lv_crg_list_members.getChildAt(i).setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = lv_crg_list_members.getChildAt(i).getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lv_crg_list_members.getChildAt(i).setLayoutParams(params);
            }
        }
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
        getListMembers();
	}
    private void getListMembers() {
        // TODO Auto-generated method stub
        mbList.clear();
        ListMemberData mb1 = new ListMemberData("member id 1", "uname 1", "fname 1", "lname 1", "http://e-space.vn/files/14.jpg", "ab@gmail.com", "3 months ago");
        ListMemberData mb2 = new ListMemberData("member id 2", "uname 2", "fname 2", "lname 2", "http://e-space.vn/files/17.jpg", "cd@gmail.com", "4 months ago");
        ListMemberData mb3 = new ListMemberData("member id 3", "uname 3", "fname 3", "lname 3", "http://e-space.vn/files/16.jpg", "ef@gmail.com", "5 months ago");
        mbList.add(mb1);
        mbList.add(mb2);
        mbList.add(mb3);
        mbAdapter.notifyDataSetChanged();
    }

    public void onLocalReceive_private_message(JSONObject data) {
    }
    public void onLocalReceive_group_message(JSONObject data) {
    }
    @Override
    public void onLocalNetworkChange() {
    }
}
