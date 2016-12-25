package com.example.shopmeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.SQLiteHandler;
import com.example.shopmeet.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by UserPC on 6/7/2016.
 */
public class LoginActivity extends Activity {
    EditText edt_login_email, edt_login_pass;
    Button bt_login_enter;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private HashMap<String, String> userSaved;
    private CallAPIHandler call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        setInitView(); // initial view object

        call = new CallAPIHandler();

        prepareVariable();

        goNextIfLoggedIn();
    }
    private void prepareVariable() {
        // TODO Auto-generated method stub
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Session manager
        session = new SessionManager(getApplicationContext());
        userSaved = new HashMap<String, String>();
    }
    private void goNextIfLoggedIn() {
        // TODO Auto-generated method stub
        if (session.isLoggedIn()) {
            // Assign logged in info to global variables
            userSaved = db.getUserDetails();

            Variables.userID = userSaved.get(SQLiteHandler.KEY_UID);
            Variables.userName = userSaved.get(SQLiteHandler.KEY_UNAME);
            Variables.userFName = userSaved.get(SQLiteHandler.KEY_UFNAME);
            Variables.userLName = userSaved.get(SQLiteHandler.KEY_ULNAME);
            Variables.userEmail = userSaved.get(SQLiteHandler.KEY_UEMAIL);
            Variables.userTel = userSaved.get(SQLiteHandler.KEY_UTEL);
            Variables.userAddress = userSaved.get(SQLiteHandler.KEY_UADDRESS);
            Variables.userJoinDate = userSaved.get(SQLiteHandler.KEY_UJOINDATE);
            Variables.userToken = userSaved.get(SQLiteHandler.KEY_UTOKEN);
            Variables.userAvatar = userSaved.get(SQLiteHandler.KEY_UAVATAR);

            if (Variables.userID.equalsIgnoreCase("") || Variables.userToken.equalsIgnoreCase("")) {
                session.setLogin(false);
                db.deleteUsers();
            } else {
                Log.d(Constants.TAG_LOGIN, "already logged in");
                // User is already logged in. Take him to other activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
    private void setInitView() {
        // TODO Auto-generated method stub
        edt_login_email = (EditText)findViewById(R.id.edt_login_email);
        edt_login_pass = (EditText)findViewById(R.id.edt_login_pass);
        bt_login_enter = (Button)findViewById(R.id.bt_login_enter);

        bt_login_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onLogIn();
                /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();*/
            }
        });
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    public void onLogIn() {
        String email = edt_login_email.getText().toString().trim();
        String password = edt_login_pass.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {
            if (!Functions.hasConnection(this)) {
                Functions.toastString(Constants.ALERT_NO_INTERNET, this);
            } else {
                // login user
                prepareLogin(email, password);
            }
        } else {
            // Prompt user to enter credentials
            Functions.toastString(Constants.ALERT_FILL_LOGIN, this);
        }
    }

    private void prepareLogin(final String email, final String password) {
        if (!Functions.hasConnection(this)) {
            Functions.toastString(Functions.getResString(this, R.string.alert_no_internet), this);
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("password", password);
        new goLogin().execute(Constants.URL_API_LOGIN, call.createQueryStringForParameters(params));
    }
    private class goLogin extends AsyncTask<String, Void, String> {

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
                Functions.toastString(Constants.ERR_LOGIN, getBaseContext());
                return;
            }
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // user successfully logged in
                    // Create login session
                    session.setLogin(true);

                    JSONObject user = jObj.getJSONObject("staffs");
                    Variables.userID = user.getString("staff_id");
                    Variables.userName = Functions.getCleanEmptyString(user.getString("username"));
                    Variables.userFName = Functions.getCleanEmptyString(user.getString("first_name"));
                    Variables.userLName = Functions.getCleanEmptyString(user.getString("last_name"));
                    Variables.userEmail = user.getString("email");
                    Variables.userTel = Functions.getCleanEmptyString(user.getString("telephone"));
                    Variables.userAddress = Functions.getCleanEmptyString(user.getString("address"));
                    Variables.userJoinDate = Functions.getCleanEmptyString(user.getString("date_added"));
                    Variables.userToken = user.getString("token");
                    Variables.userAvatar = user.getString("staff_avatar");

                    // Inserting row in users table
                    db.addUser(Variables.userID, Variables.userName, Variables.userFName, Variables.userLName, Variables.userEmail,
                            Variables.userTel, Variables.userAddress, Variables.userJoinDate, Variables.userToken, Variables.userAvatar);

                    // Launch activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Error in login. Get the error message
                    String errorMsg = jObj.getString("message");
                    Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                    Functions.toastString(Constants.ERR_LOGIN + ": " + errorMsg, getBaseContext());
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON + ": " + e.getMessage(), getBaseContext());
            }
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }
}
