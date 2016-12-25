package com.example.shopmeet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.MyLinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by UserPC on 6/17/2016.
 */
public class ShopChoosing extends Activity {
    Spinner sp_sch_main;
    Button bt_sch_refr, bt_sch_enter;

    private ProgressDialog pDialog;
    private CallAPIHandler call;
    private MyLinkedHashMap<Integer, String> listShop;
    ArrayAdapter<String> shopAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopchoosing);
        listShop = new MyLinkedHashMap<>();

        setInitView(); // initial view object
        call = new CallAPIHandler();

        prepareGetList(Variables.userToken);
        Log.d("---", Variables.userToken);
    }

    public void setInitView() {
        setCustomActionBar(getResources().getString(R.string.title_sch));
        sp_sch_main = (Spinner)findViewById(R.id.sp_sch_main);
        bt_sch_refr = (Button)findViewById(R.id.bt_sch_refr);
        bt_sch_enter = (Button)findViewById(R.id.bt_sch_enter);
        bt_sch_refr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                prepareGetList(Variables.userToken);
            }
        });
        bt_sch_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Variables.curShopName = sp_sch_main.getSelectedItem().toString();
                Variables.curShopID = listShop.getKey(sp_sch_main.getSelectedItemPosition());
                Intent intent = new Intent(ShopChoosing.this, MainActivity.class);
                startActivity(intent);
                //finish();
            }
        });
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }
    public void setCustomActionBar(String title) {
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
        getActionBar().setCustomView(R.layout.actionbar_title);
        TextView tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }
    private void prepareGetList(String token) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        new GetList().execute(Constants.URL_API_GET_SHOPS, call.createQueryStringForParameters(params));
    }
    private class GetList extends AsyncTask<String, Void, String> {

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
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, getBaseContext());
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
                        listShop.put(s_id, s_name);
                    }
                    List<String> sArray =  new ArrayList<String>();
                    for (int i = 0; i <listShop.size(); i++) {
                        sArray.add(listShop.getValue(i));
                    }
                    shopAdapter = new ArrayAdapter<String>(
                            getBaseContext(), android.R.layout.simple_spinner_item, sArray);
                    sp_sch_main.setAdapter(shopAdapter);
                    shopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                } else {
                    // Error in getting. Get the error message
                    String errorMsg = jObj.getString("message");
                    Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                    Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, getBaseContext());
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON + ": " + e.getMessage(), getBaseContext());
            }
        }

    }
}
