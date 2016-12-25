package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListNoteAdapter;
import com.example.shopmeet.adapter.ListTaskAdapter;
import com.example.shopmeet.adapter.ListTaskAdapter.ListTaskData;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.NoteData;
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
public class Frg_Note extends Fragment implements MainActivity.LocalRtcListener {

    // Members
    private TextView tv_row_alert_note;
    private MyExpandableListView lv_note_list;
    private List<NoteData> noteList;
    private ListNoteAdapter noteAdapter;

    private EditText edt_note_type;
    private Button bt_note_save;

    // Action bar
    private TextView tvTitle;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private int isAlreadyLoadFromServer;

    private String group_id, group_name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_note, container, false);
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
        }
    }
    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBarChat(Functions.getResString(getActivity(), R.string.frg_note_title)+" "+group_name);
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        bt_note_save = (Button)v.findViewById(R.id.bt_note_save);
        edt_note_type = (EditText)v.findViewById(R.id.edt_note_type);

        bt_note_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_note_type.getText().toString().equalsIgnoreCase(""))
                    return;
                else
                    onAdd(edt_note_type.getText().toString());
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        tv_row_alert_note = (TextView)v.findViewById(R.id.tv_row_alert_note);
        lv_note_list = (MyExpandableListView)v.findViewById(R.id.lv_note_list);

    }
    private void onAdd(String content) {
        prepareAdd(Variables.userToken, content, group_id);
    }
    public void prepareAdd(String token, String content, String group_id) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("content", content);
        params.put("group_id", group_id);
        new RequestServerAdd().execute(Constants.URL_API_NOTE_ADD, call.createQueryStringForParameters(params));
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
                    edt_note_type.setText("");

                    JSONArray jANotes = jObj.getJSONArray("objects");
                    noteList.clear();
                    db.deleteNote();
                    for (int i = 0; i < jANotes.length(); i++) {
                        JSONObject jElm = jANotes.getJSONObject(i);
                        int note_id = jElm.optInt("id");
                        String note_content = jElm.optString("content");
                        String staff_id = jElm.optString("staff_id");
                        String staff_fname = jElm.optString("first_name");
                        String staff_lname = jElm.optString("last_name");
                        String staff_avatar = jElm.optString("avatar");
                        String note_create_at = jElm.optString("date_added");
                        String note_modified = jElm.optString("last_modified");
                        String cur_group_id = jElm.optString("group_id");

                        if (cur_group_id.equalsIgnoreCase(group_id)) {
                            NoteData g = new NoteData(
                                    String.valueOf(note_id), staff_id, staff_fname, staff_lname, staff_avatar,
                                    note_create_at, note_modified, note_content, 1);
                            noteList.add(g);
                        }
                        db.addNote(String.valueOf(note_id), staff_id, staff_fname, staff_lname,
                                note_create_at, note_modified, note_content, cur_group_id);

                    }
                    if (noteList.size() > 0) {
                        noteAdapter.notifyDataSetChanged();
                        lv_note_list.setVisibility(View.VISIBLE);
                        tv_row_alert_note.setVisibility(View.GONE);
                    } else {
                        lv_note_list.setVisibility(View.GONE);
                        tv_row_alert_note.setVisibility(View.VISIBLE);
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
    }


    public void setData() {
        // TODO Auto-generated method stub
        if (noteList == null)
            noteList = new ArrayList<NoteData>();
        if (noteAdapter == null)
            noteAdapter = new ListNoteAdapter(getActivity(), noteList);
        lv_note_list.setExpanded(true);
        lv_note_list.setAdapter(noteAdapter);

        /*noteList.clear();
        NoteData mb1 = new NoteData("81", "133", "fname 1", "lname 1", "http://e-space.vn/files/14.jpg", "ab@gmail.com", "3 months ago", 1);
        NoteData mb2 = new NoteData("82", "uname 2", "fname 2", "lname 2", "http://e-space.vn/files/17.jpg", "cd@gmail.com", "4 months ago", 1);
        NoteData mb3 = new NoteData("83", "uname 3", "fname 3", "lname 3", "http://e-space.vn/files/16.jpg", "ef@gmail.com", "5 months ago", 1);
        noteList.add(mb1);
        noteList.add(mb2);
        noteList.add(mb3);
        noteAdapter.notifyDataSetChanged();*/
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Variables.curFrg = Constants.TAG_FRG_NOTE;
        MainActivity.setIndicator(Variables.curFrg);

        if (!Functions.hasConnection(getActivity())) {
            showListNoteFromDB();
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
        } else {
            prepareGetList(Variables.userToken);
        }
    }
    private void showListNoteFromDB() {
        // TODO Auto-generated method stub
        noteList.clear();
        db.getNoteByGroup(noteList, group_id);
        if (noteList.size() > 0) {
            noteAdapter.notifyDataSetChanged();
            lv_note_list.setVisibility(View.VISIBLE);
            tv_row_alert_note.setVisibility(View.GONE);
        } else {
            lv_note_list.setVisibility(View.GONE);
            tv_row_alert_note.setVisibility(View.VISIBLE);
        }
    }

    public void prepareGetList(String token) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        new RequestServer().execute(Constants.URL_API_NOTE_LIST, call.createQueryStringForParameters(params));
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

                    JSONArray jANotes = jObj.getJSONArray("objects");
                    noteList.clear();
                    db.deleteNote();
                    for (int i = 0; i < jANotes.length(); i++) {
                        JSONObject jElm = jANotes.getJSONObject(i);
                        int note_id = jElm.optInt("id");
                        String note_content = jElm.optString("content");
                        String staff_id = jElm.optString("staff_id");
                        String staff_fname = jElm.optString("first_name");
                        String staff_lname = jElm.optString("last_name");
                        String staff_avatar = jElm.optString("avatar");
                        String note_create_at = jElm.optString("date_added");
                        String note_modified = jElm.optString("last_modified");
                        String cur_group_id = jElm.optString("group_id");

                        if (cur_group_id.equalsIgnoreCase(group_id)) {
                            NoteData g = new NoteData(
                                    String.valueOf(note_id), staff_id, staff_fname, staff_lname, staff_avatar,
                                    note_create_at, note_modified, note_content, 1);
                            noteList.add(g);
                        }
                        db.addNote(String.valueOf(note_id), staff_id, staff_fname, staff_lname,
                                note_create_at, note_modified, note_content, cur_group_id);

                    }
                    if (noteList.size() > 0) {
                        noteAdapter.notifyDataSetChanged();
                        lv_note_list.setVisibility(View.VISIBLE);
                        tv_row_alert_note.setVisibility(View.GONE);
                    } else {
                        lv_note_list.setVisibility(View.GONE);
                        tv_row_alert_note.setVisibility(View.VISIBLE);
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
                        lv_note_list.setVisibility(View.GONE);
                        tv_row_alert_note.setVisibility(View.VISIBLE);
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
    }
}
