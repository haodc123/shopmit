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
import com.example.shopmeet.adapter.ListCommentAdapter;
import com.example.shopmeet.adapter.ListNoteAdapter;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.CommentData;
import com.example.shopmeet.model.NoteData;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.NetworkUtil;
import com.example.shopmeet.utils.SQLiteHandler;
import com.example.shopmeet.utils.SessionManager;
import com.example.shopmeet.view.MyExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.manager.IOManager;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Comment extends Fragment implements MainActivity.LocalRtcListener {

    // Members
    private TextView tv_row_alert_comment;
    private MyExpandableListView lv_comment_list;
    private List<CommentData> commentList;
    private ListCommentAdapter commentAdapter;

    private EditText edt_comment_type;
    private TextView tv_status_content, tv_comment_intro, tv_comment_more;
    private Button bt_comment_save;

    // Action bar
    private TextView tvTitle;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private int isAlreadyLoadFromServer;

    private String status_id, status_content;
    private int num_comment = 0;
    //private int last_last_position;
    //private static final int BLOCK_COMMENTS_LOAD = 2;
    private static final int MAX_COMMENTS_LOAD = 50;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_comment, container, false);
        MainActivity.mLocalRtcListener = this;
        setInitView(v);
        getBundle();
        initVariables();

        setData();
        return v;
    }
    private void getBundle() {
        Bundle b = this.getArguments();
        if (b != null) {
            status_id = b.getString("status_id");
            status_content = b.getString("content");
        }
    }
    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBarChat(Functions.getResString(getActivity(), R.string.frg_comment_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        bt_comment_save = (Button)v.findViewById(R.id.bt_comment_save);
        edt_comment_type = (EditText)v.findViewById(R.id.edt_comment_type);
        tv_status_content = (TextView)v.findViewById(R.id.tv_status_content);
        tv_comment_intro = (TextView)v.findViewById(R.id.tv_comment_intro);
        tv_comment_more = (TextView)v.findViewById(R.id.tv_comment_more);
        tv_comment_more.setVisibility(View.GONE);

        bt_comment_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_comment_type.getText().toString().equalsIgnoreCase(""))
                    return;
                else
                    onAdd(edt_comment_type.getText().toString());
            }
        });
        tv_comment_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //last_last_position+=BLOCK_COMMENTS_LOAD;
                //prepareGetList(Variables.userToken, status_id, last_last_position, last_last_position+BLOCK_COMMENTS_LOAD);
                prepareGetList(Variables.userToken, status_id);
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        tv_row_alert_comment = (TextView)v.findViewById(R.id.tv_row_alert_comment);
        lv_comment_list = (MyExpandableListView)v.findViewById(R.id.lv_comment_list);

    }
    private void onAdd(String content) {
        prepareAdd(Variables.userToken, content, status_id);
    }
    public void prepareAdd(String token, String content, String status_id) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("content", content);
        params.put("timeline_id", status_id);
        new RequestServerAdd().execute(Constants.URL_API_COMMENT_ADD, call.createQueryStringForParameters(params));
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
                    edt_comment_type.setText("");

                    JSONObject jAcomments = jObj.getJSONObject("objects");

                    int comment_id = jAcomments.optInt("comment_id");
                    String comment_content = jAcomments.optString("content");
                    String staff_id = jAcomments.optString("staff_id");
                    String staff_fname = jAcomments.optString("first_name");
                    String staff_lname = jAcomments.optString("last_name");
                    String staff_avatar = jAcomments.optString("avatar");
                    String comment_create_at = jAcomments.optString("date_added");
                    String comment_modified = jAcomments.optString("date_modified");

                    CommentData g = new CommentData(
                            String.valueOf(comment_id), staff_id, staff_fname, staff_lname, staff_avatar,
                            comment_create_at, comment_content, comment_modified, 1);
                    commentList.add(g);

                    num_comment++;
                    tv_comment_intro.setText(Functions.getResString(getActivity(), R.string.frg_comment_intro)+" ("+num_comment+")");

                    if (commentList.size() > 0) {
                        commentAdapter.notifyDataSetChanged();
                        lv_comment_list.setVisibility(View.VISIBLE);
                        tv_row_alert_comment.setVisibility(View.GONE);
                    } else {
                        lv_comment_list.setVisibility(View.GONE);
                        tv_row_alert_comment.setVisibility(View.VISIBLE);
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
                Functions.toastString(Constants.ERR_JSON, getActivity());
            }
        }

    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        call = new CallAPIHandler();
        db = new SQLiteHandler(getActivity());
        isAlreadyLoadFromServer = 0;
        //last_last_position = 0;
    }


    public void setData() {
        // TODO Auto-generated method stub
        tv_status_content.setText(status_content);

        if (commentList == null)
            commentList = new ArrayList<CommentData>();
        if (commentAdapter == null)
            commentAdapter = new ListCommentAdapter(getActivity(), commentList, this);
        lv_comment_list.setExpanded(true);
        lv_comment_list.setAdapter(commentAdapter);

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Variables.curFrg = Constants.TAG_FRG_COMMENT;
        MainActivity.setIndicator(Variables.curFrg);

        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
        } else {
            //prepareGetList(Variables.userToken, status_id, last_last_position, last_last_position+BLOCK_COMMENTS_LOAD);
            prepareGetList(Variables.userToken, status_id);
        }
    }

    public void prepareGetList(String token, String status_id) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("timeline_id", status_id);
        params.put("from", String.valueOf(0));
        params.put("to", String.valueOf(MAX_COMMENTS_LOAD));
        new RequestServer().execute(Constants.URL_API_STATUS_DETAIL, call.createQueryStringForParameters(params));
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

                    JSONObject jOO = jObj.getJSONObject("objects");
                    num_comment = jOO.getInt("total_comment");
                    tv_comment_intro.setText(Functions.getResString(getActivity(), R.string.frg_comment_intro)+
                            " ("+String.valueOf(num_comment)+")");

                    JSONObject jAComments = jOO.getJSONObject("comments");
                    JSONArray jAComment = jAComments.getJSONArray("comment");
                    commentList.clear();
                    for (int i = 0; i < jAComment.length(); i++) {
                        JSONObject jElm = jAComment.getJSONObject(i);
                        int comment_id = jElm.optInt("comment_id");
                        String comment_content = jElm.optString("content");
                        String staff_id = jElm.optString("staff_id");
                        String staff_fname = jElm.optString("first_name");
                        String staff_lname = jElm.optString("last_name");
                        String staff_avatar = jElm.optString("avatar");
                        String comment_create_at = jElm.optString("date_added");
                        String comment_modified = jElm.optString("date_modified");

                        CommentData g = new CommentData(
                                String.valueOf(comment_id), staff_id, staff_fname, staff_lname, staff_avatar,
                                comment_create_at, comment_content, comment_modified, 1);
                        commentList.add(g);

                    }
                    if (commentList.size() > 0) {
                        commentAdapter.notifyDataSetChanged();
                        lv_comment_list.setVisibility(View.VISIBLE);
                        tv_row_alert_comment.setVisibility(View.GONE);
                    } else {
                        lv_comment_list.setVisibility(View.GONE);
                        tv_row_alert_comment.setVisibility(View.VISIBLE);
                    }

                    /*if ((last_last_position+BLOCK_COMMENTS_LOAD) >= num_comment) {
                        tv_comment_more.setVisibility(View.GONE);
                    } else {
                        tv_comment_more.setVisibility(View.VISIBLE);
                    }*/

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
                        lv_comment_list.setVisibility(View.GONE);
                        tv_row_alert_comment.setVisibility(View.VISIBLE);
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

    public void setNumComment(int num) {
        num_comment = num;
        tv_comment_intro.setText(Functions.getResString(getActivity(), R.string.frg_comment_intro)+
                " ("+String.valueOf(num_comment)+")");
    }
    public void checkEmptyList() {
        if (commentList.size() > 0) {
            commentAdapter.notifyDataSetChanged();
            lv_comment_list.setVisibility(View.VISIBLE);
            tv_row_alert_comment.setVisibility(View.GONE);
        } else {
            lv_comment_list.setVisibility(View.GONE);
            tv_row_alert_comment.setVisibility(View.VISIBLE);
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
                    //prepareGetList(Variables.userToken, status_id, last_last_position, last_last_position+BLOCK_COMMENTS_LOAD);
                    prepareGetList(Variables.userToken, status_id);
                    isAlreadyLoadFromServer = 1;
                }
                break;
            case NetworkUtil.TYPE_MOBILE:
                Variables.isAlreadyAlertConnection = 0;
                if (isAlreadyLoadFromServer == 0) {
                    //prepareGetList(Variables.userToken, status_id, last_last_position, last_last_position+BLOCK_COMMENTS_LOAD);
                    prepareGetList(Variables.userToken, status_id);
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
