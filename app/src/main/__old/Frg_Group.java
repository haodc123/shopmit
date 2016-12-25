package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.Activity;
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
import com.example.shopmeet.adapter.ListGroupAdapter;
import com.example.shopmeet.adapter.ListGroupAdapter.ListGroupData;
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
public class Frg_Group extends Fragment implements MainActivity.LocalRtcListener {

    private TextView tv_group_title;
    private TextView tv_alert;
    private ImageView img_group_do;
    private Button bt_group_add;
    // Members
    private MyExpandableListView lv_group_list_members;
    private List<ListMemberData> mbList;
    private ListMemberAdapter mbAdapter;

    // Action bar
    private TextView tvTitle;
    // Search bar
    private EditText edt_per_search;
    private Button bt_per_search;

    private CallAPIHandler call;
    private ProgressDialog pDialog;

    private String group_id, group_name, group_create_at, group_avatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_group, container, false);
        getBundle();
        setInitView(v);
        call = new CallAPIHandler();
        setData();
        return v;
    }
    private void getBundle() {
        Bundle b = this.getArguments();
        if (b != null) {
            group_id = b.getString("group_id");
            group_name = b.getString("group_name");
            group_create_at = b.getString("group_create_at");
            group_avatar = b.getString("group_avatar");
        }
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
        tv_group_title = (TextView) v.findViewById(R.id.tv_group_title);
        tv_alert = (TextView) v.findViewById(R.id.tv_alert);
        img_group_do = (ImageView) v.findViewById(R.id.img_group_do);
        lv_group_list_members = (MyExpandableListView)v.findViewById(R.id.lv_group_list_members);

        img_group_do.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                displayDialog(group_id, group_name, group_create_at, group_avatar);
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
    protected void displayDialog(final String id, final String name, final String create_at, final String group_avatar) {
        // TODO Auto-generated method stub
        final Dialog mDialogFile = new Dialog(getActivity());
        mDialogFile.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialogFile.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialogFile.setContentView(R.layout.dialog_group);
        mDialogFile.setTitle("");

        TextView tv_dl_name, tv_dl_create;
        LinearLayout ll_dl_chat, ll_dl_call, ll_dl_video, ll_dl_timeline, ll_dl_sendfile;
        ImageView img_dl_avatar;
        tv_dl_name = (TextView)mDialogFile.findViewById(R.id.tv_dl_name);
        tv_dl_create = (TextView)mDialogFile.findViewById(R.id.tv_dl_create);
        ll_dl_chat = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_chat);
        ll_dl_call = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_call);
        ll_dl_video = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_video);
        ll_dl_timeline = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_timeline);
        ll_dl_sendfile = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_sendfile);
        img_dl_avatar = (ImageView)mDialogFile.findViewById(R.id.img_dl_avatar);
        tv_dl_name.setText(name);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Constants.DATETIME_FORMAT);
        String today = df.format(c.getTime());
        tv_dl_create.setText("Created "+Functions.getFriendlyJoinDate(today, create_at, Constants.DATETIME_FORMAT));

        new loadRemoteIMG(img_dl_avatar).execute(group_avatar);

        ll_dl_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFile.dismiss();
                goChat(id, name);
            }
        });
        ll_dl_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFile.dismiss();
                goCall(id, name);
            }
        });
        ll_dl_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFile.dismiss();
                goVideo(id, name);
            }
        });
        ll_dl_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFile.dismiss();
                goTimeline(id, name);
            }
        });
        ll_dl_sendfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFile.dismiss();
                goSendFile(id, name);
            }
        });
        mDialogFile.show();
    }
    public class loadRemoteIMG extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        public loadRemoteIMG(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }
        @SuppressWarnings("null")
        @Override
        protected Bitmap doInBackground(String... args) {
            // updating UI from Background Thread
            final String imageKey = Functions.getFileName(String.valueOf(args[0]));
            Bitmap bm = null;
            bm = Variables.mDCache.getBitmap(imageKey);
            if (bm == null) {
                if (!Functions.hasConnection(getActivity())) {
                    return null;
                }
                try {
                    URL mUrl = new URL(args[0]);
                    HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    if (is != null) {
                        bm = BitmapFactory.decodeStream(is);
                        Variables.mDCache.put(imageKey, bm);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bm;
        }
        @Override
        protected void onPostExecute(Bitmap bm) {
            if (isCancelled()) {
                bm = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bm != null) {
                        imageView.setImageBitmap(bm);
                    } else {
                        Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.no_img);

                        imageView.setImageDrawable(placeholder);
                    }
                }
            }
        }
    }
    public void goChat(String id, String name) {
        Frg_Chat frgChat = new Frg_Chat();

        Bundle b = new Bundle();
        b.putInt("chatType", Constants.TYPE_GROUP);
        b.putString("partner_id", id);
        b.putString("partner_name", name);
        frgChat.setArguments(b);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frgChat, Constants.TAG_FRG_CHAT)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_CHAT;
    }
    public void goCall(String id, String name) {

    }
    public void goVideo(String id, String name) {

    }
    public void goTimeline(String id, String name) {

    }
    public void goSendFile(String id, String name) {

    }

    public void onSearch(String kw) {
        if (kw.equals(""))
            return;
        for (int i = 0; i < mbList.size(); i++) {
            String curName = mbList.get(i).getMb_fname() + " " + mbList.get(i).getMb_lname();
            if (!curName.toLowerCase().contains(kw.toLowerCase())) {
                lv_group_list_members.getChildAt(i).setVisibility(View.GONE);
                ViewGroup.LayoutParams params=lv_group_list_members.getChildAt(i).getLayoutParams();
                params.height=0;
                lv_group_list_members.getChildAt(i).setLayoutParams(params);
            } else {
                lv_group_list_members.getChildAt(i).setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params=lv_group_list_members.getChildAt(i).getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lv_group_list_members.getChildAt(i).setLayoutParams(params);
            }
        }
    }
    public void refreshListAfterSearch() {
        if (mbList == null)
            return;
        for (int i = 0; i < mbList.size(); i++) {
            if (lv_group_list_members.getChildAt(i) != null) {
                lv_group_list_members.getChildAt(i).setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = lv_group_list_members.getChildAt(i).getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lv_group_list_members.getChildAt(i).setLayoutParams(params);
            }
        }
        if (mbList.size() > 0) {
            lv_group_list_members.setVisibility(View.VISIBLE);
            tv_alert.setVisibility(View.GONE);
        } else {
            lv_group_list_members.setVisibility(View.GONE);
            tv_alert.setVisibility(View.VISIBLE);
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
        if (mbList == null)
            mbList = new ArrayList<ListMemberData>();
        if (mbAdapter == null)
            mbAdapter = new ListMemberAdapter(getActivity(), mbList);
        lv_group_list_members.setExpanded(true);
        lv_group_list_members.setAdapter(mbAdapter);
	}

    @Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        Variables.curFrg = Constants.TAG_FRG_GROUP;
        MainActivity.setIndicator(Variables.curFrg);
        //prepareGetList(Variables.userToken, group_id);
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

    private void prepareGetList(String token, String group_id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("group_id", group_id);
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
                Constants.URL_API_GET_STAFFS_BY_GROUP, new Response.Listener<String>() {

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

                        mbList.clear();

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

                        if (mbList.size() > 0) {
                            mbAdapter.notifyDataSetChanged();
                        } else {
                            lv_group_list_members.setVisibility(View.GONE);
                            tv_alert.setVisibility(View.VISIBLE);
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
