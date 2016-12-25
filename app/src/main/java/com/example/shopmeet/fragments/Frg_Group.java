package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shopmeet.AppController;
import com.example.shopmeet.LoginActivity;
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
public class Frg_Group extends Fragment implements MainActivity.LocalRtcListener {

    //private TextView tv_group_title;
    private TextView tv_group_alert;
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
    private SQLiteHandler db;
    private SessionManager session;
    private HashMap<String, String> groupSaved;
    private int isAlreadyLoadFromServer;

    private String group_id, group_name, group_create_at, group_avatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_group, container, false);
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
            group_id = b.getString("group_id");
            group_name = b.getString("group_name");
            group_create_at = b.getString("group_create_at");
            group_avatar = b.getString("group_avatar");
        }
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        call = new CallAPIHandler();
        db = new SQLiteHandler(getActivity());
        groupSaved = new HashMap<String, String>();
        isAlreadyLoadFromServer = 0;
    }
    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_group_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");
        ((MainActivity)getActivity()).setABTitle(group_name);

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
        //tv_group_title = (TextView) v.findViewById(R.id.tv_group_title);
        tv_group_alert = (TextView) v.findViewById(R.id.tv_group_alert);
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
        mDialogFile.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialogFile.setContentView(R.layout.dialog_group);
        mDialogFile.setTitle("");

        TextView tv_dl_name, tv_dl_create;
        LinearLayout ll_dl_chat, ll_dl_call, ll_dl_video, ll_dl_note, ll_dl_sendfile;
        ImageView img_dl_avatar;
        tv_dl_name = (TextView)mDialogFile.findViewById(R.id.tv_dl_name);
        tv_dl_create = (TextView)mDialogFile.findViewById(R.id.tv_dl_create);
        ll_dl_chat = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_chat);
        ll_dl_call = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_call);
        ll_dl_video = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_video);
        ll_dl_note = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_note);
        ll_dl_sendfile = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_sendfile);
        img_dl_avatar = (ImageView)mDialogFile.findViewById(R.id.img_dl_avatar);
        tv_dl_name.setText(name);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Constants.DATETIME_FORMAT);
        String today = df.format(c.getTime());
        tv_dl_create.setText("Created "+Functions.getFriendlyDateCreated(today, create_at, Constants.DATETIME_FORMAT));

        if (!group_avatar.equalsIgnoreCase(""))
            new loadRemoteIMG(img_dl_avatar).execute(Constants.FOLDER_AVATAR+group_avatar);

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
        ll_dl_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFile.dismiss();
                goNote(id, name);
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
        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_feature_unavailable), getActivity());
    }
    public void goVideo(String id, String name) {
        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_feature_unavailable), getActivity());
    }
    public void goNote(String id, String name) {
        Frg_Note frg = new Frg_Note();

        Bundle b = new Bundle();
        b.putString("group_id", group_id);
        b.putString("group_name", group_name);
        frg.setArguments(b);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frg, Constants.TAG_FRG_NOTE)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_NOTE;
    }
    public void goSendFile(String id, String name) {
        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_feature_unavailable), getActivity());
    }

    public void onSearch(String kw) {
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
            lv_group_list_members.setVisibility(View.VISIBLE);
            tv_group_alert.setVisibility(View.GONE);
        } else {
            lv_group_list_members.setVisibility(View.GONE);
            tv_group_alert.setVisibility(View.VISIBLE);
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
            lv_group_list_members.setVisibility(View.VISIBLE);
            tv_group_alert.setVisibility(View.GONE);
        } else {
            lv_group_list_members.setVisibility(View.GONE);
            tv_group_alert.setVisibility(View.VISIBLE);
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
        //tv_group_title.setText("Group: "+group_name);
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
        if (MainActivity.isResumeWhileCalling == 0) {
            Variables.curFrg = Constants.TAG_FRG_GROUP;
            MainActivity.setIndicator(Variables.curFrg);
            MainActivity.setBackButton(Variables.curFrg);
            if (!Functions.hasConnection(getActivity())) {
                showGroupDetailFromDB(group_id);
                Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            } else {
                prepareGetList(Variables.userToken, group_id);
            }
        }
    }
    private void showGroupDetailFromDB(String group_id) {
        groupSaved = db.getGroupDetails(group_id);

        //tv_group_title.setText(groupSaved.get(SQLiteHandler.KEY_GROUP_NAME));
        String staff_arr = groupSaved.get(SQLiteHandler.KEY_GROUP_STAFF_ARR);

        mbList.clear();
        db.getSomeStaff(mbList, staff_arr);
        if (mbList.size() > 0) {
            mbAdapter.notifyDataSetChanged();
            /*lv_group_list_members.setVisibility(View.VISIBLE);
            tv_group_alert.setVisibility(View.GONE);*/
        } else {
            /*lv_group_list_members.setVisibility(View.GONE);
            tv_group_alert.setVisibility(View.VISIBLE);*/
        }
    }

    private void getListMembers() {
        // TODO Auto-generated method stub
        mbList.clear();
        ListMemberData mb1 = new ListMemberData("81", "uname 1", "fname 1", "lname 1", "http://e-space.vn/files/14.jpg", "ab@gmail.com", "3 months ago", 1);
        ListMemberData mb2 = new ListMemberData("82", "uname 2", "fname 2", "lname 2", "http://e-space.vn/files/17.jpg", "cd@gmail.com", "4 months ago", 1);
        ListMemberData mb3 = new ListMemberData("83", "uname 3", "fname 3", "lname 3", "http://e-space.vn/files/16.jpg", "ef@gmail.com", "5 months ago", 1);
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
        //requestServer(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServer().execute(Constants.URL_API_GET_STAFFS_BY_GROUP, call.createQueryStringForParameters(params));
    }
    /*private void requestServer(final Map<String, String> params) {
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

                            JSONArray jAStaff = jObj.getJSONArray("staff");
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
                            lv_group_list_members.setVisibility(View.GONE);
                            tv_group_alert.setVisibility(View.VISIBLE);
                        }

                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, getActivity());
                        lv_group_list_members.setVisibility(View.GONE);
                        tv_group_alert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.e(Constants.TAG_API, response.toString() + Constants.ERR_JSON + e.getMessage());
                    Functions.toastString(Constants.ERR_JSON + ": " + e.getMessage(), getActivity());
                    lv_group_list_members.setVisibility(View.GONE);
                    tv_group_alert.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                Log.e(Constants.TAG_API, Constants.ERR_NO_DATA_FROM_SERVER + error.getMessage());
                lv_group_list_members.setVisibility(View.GONE);
                tv_group_alert.setVisibility(View.VISIBLE);
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

                    String staff_arr = "";
                    JSONArray jAStaff = jObj.getJSONArray("staff");
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
                        staff_arr += staff_arr.equals("") ? ""+staff_id+"" : ","+staff_id + "";
                    }

                    if (mbList.size() > 0) {
                        lv_group_list_members.setVisibility(View.VISIBLE);
                        tv_group_alert.setVisibility(View.GONE);
                        mbAdapter.notifyDataSetChanged();
                    } else {
                        lv_group_list_members.setVisibility(View.GONE);
                        tv_group_alert.setVisibility(View.VISIBLE);
                    }
                    db.updateGroup(group_id, staff_arr);

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
                        lv_group_list_members.setVisibility(View.GONE);
                        tv_group_alert.setVisibility(View.VISIBLE);
                    }
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON, getActivity());
                lv_group_list_members.setVisibility(View.GONE);
                tv_group_alert.setVisibility(View.VISIBLE);
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
                    prepareGetList(Variables.userToken, group_id);
                    isAlreadyLoadFromServer = 1;
                }
                break;
            case NetworkUtil.TYPE_MOBILE:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    prepareGetList(Variables.userToken, group_id);
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
