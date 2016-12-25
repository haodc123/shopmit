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
import android.widget.TextView;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter.ListMemberData;
import com.example.shopmeet.adapter.ListTaskAdapter.ListTaskData;
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
public class Frg_Note_Edit extends Fragment implements MainActivity.LocalRtcListener {

    // Action bar
    private TextView tvTitle;

    private TextView row_nedt_created;
    private EditText edt_nedt_content;
    private Button bt_nedt_back, bt_nedt_save;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private HashMap<String, String> noteSaved;
    private SQLiteHandler db;
    private SessionManager session;

    private String note_id, note_modified, note_content;
    private int isAlreadyLoadFromServer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_note_edit, container, false);
        MainActivity.mLocalRtcListener = this;
        getBundle();
        setInitView(v);
        setData();
        initVariables();

        return v;
    }
    private void getBundle() {
        Bundle b = this.getArguments();
        if (b != null) {
            note_id = b.getString("note_id");
            note_modified = b.getString("modified");
            note_content = b.getString("content");
        }
    }
    public void setData() {
        row_nedt_created.setText(note_modified.equalsIgnoreCase("") ? "" : note_modified.substring(0, 10));
        edt_nedt_content.setText(note_content);
    }
    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        call = new CallAPIHandler();
        db = new SQLiteHandler(getActivity());
        noteSaved = new HashMap<String, String>();
        isAlreadyLoadFromServer = 0;
    }
    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBarChat(Functions.getResString(getActivity(), R.string.frg_nedt_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        row_nedt_created = (TextView) v.findViewById(R.id.row_nedt_created);
        edt_nedt_content = (EditText) v.findViewById(R.id.edt_nedt_content);
        bt_nedt_back = (Button) v.findViewById(R.id.bt_nedt_back);
        bt_nedt_save = (Button) v.findViewById(R.id.bt_nedt_save);

        bt_nedt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        bt_nedt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });
    }

    public void onBack() {
        getActivity().getFragmentManager().popBackStack();
    }
    public void onSave() {
        if (edt_nedt_content.getText().toString().equalsIgnoreCase("")) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_fill_content), getActivity());
            return;
        }
        prepareUpdateNote(Variables.userToken, note_id, edt_nedt_content.getText().toString());
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (MainActivity.isResumeWhileCalling == 0) {
            Variables.curFrg = Constants.TAG_FRG_NOTE_EDIT;
            MainActivity.setIndicator(Variables.curFrg);
            if (!Functions.hasConnection(getActivity())) {
                showNoteDetailFromDB(note_id);
                Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            } else {
                prepareGetNoteDetail(Variables.userToken, note_id);
            }
        }
    }
    private void showNoteDetailFromDB(String noteID) {
        noteSaved = db.getNoteDetails(noteID);

        String modified = noteSaved.get(SQLiteHandler.KEY_NOTE_LASTMODIFIED);
        String content = noteSaved.get(SQLiteHandler.KEY_NOTE_CONTENT);

        row_nedt_created.setText(modified.substring(0, 10));
        edt_nedt_content.setText(content);

    }
    private void prepareGetNoteDetail(String token, String noteID) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("note_id", noteID);
        // Using volley library --> seem faster
        //requestServerTaskDetail(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServerNoteDetail().execute(Constants.URL_API_NOTE_DETAIL, call.createQueryStringForParameters(params));
    }
    private class RequestServerNoteDetail extends AsyncTask<String, Void, String> {
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
                    int note_id = jOObjects.optInt("id");
                    String note_content = jOObjects.optString("content");
                    String note_create_at = jOObjects.optString("date_added");
                    String note_modified = jOObjects.optString("last_modified");

                    row_nedt_created.setText(note_modified.equalsIgnoreCase("") ? note_create_at : note_modified.substring(0, 10));
                    edt_nedt_content.setText(note_content);

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

    /**
     * Update Note
     */
    private void prepareUpdateNote(String token, String note_id, String content) {
        if (!Functions.hasConnection(getActivity())) {
            Functions.toastString(Functions.getResString(getActivity(), R.string.alert_no_internet), getActivity());
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("note_id", note_id);
        params.put("content", content);
        // Using volley library --> seem faster
        //requestServerUpdateTask(params);
        // Using HttpURLConnection (CallAPIHandler)
        new RequestServerUpdateNote().execute(Constants.URL_API_NOTE_UPDATE, call.createQueryStringForParameters(params));
    }
    private class RequestServerUpdateNote extends AsyncTask<String, Void, String> {
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

                    Functions.toastString(Functions.getResString(getActivity(), R.string.alert_update_ok), getActivity());

                    getFragmentManager().popBackStack();

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
                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_update_failed), getActivity());
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
    /*private void requestServerUpdateTask(final Map<String, String> params) {
        pDialog.setMessage(Constants.INFORM_WAIT);
        pDialog.setCancelable(false);
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_API_UPDATE_TASK, new Response.Listener<String>() {

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

                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_update_task_ok), getActivity());

                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Functions.getResString(getActivity(), R.string.alert_update_task_failed), getActivity());
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
    }*/
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

    }
    public void onLocalOn_user_online(JSONObject data) {

    }
}
