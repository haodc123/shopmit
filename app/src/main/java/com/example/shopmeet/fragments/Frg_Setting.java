package com.example.shopmeet.fragments;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.manager.IOManager;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Setting extends Fragment implements MainActivity.LocalRtcListener {

    // Profile
    private ImageView img_personal_avatar;
    private TextView tv_personal_name, tv_personal_email;
    private LinearLayout ll_personal_profile;

    private TextView tv_setting_logout, tv_setting_exit;

    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_setting, container, false);
        MainActivity.mLocalRtcListener = this;

        setInitView(v);
        initVariables();
        setData();
        return v;
    }
    private void setInitView(View v) {
        // TODO Auto-generated method stub
        ((MainActivity)getActivity()).setCustomActionBar(Functions.getResString(getActivity(), R.string.frg_seting_title));
        ((MainActivity)getActivity()).setDisplayCall(0, "", "", "");

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        img_personal_avatar = (ImageView)v.findViewById(R.id.img_personal_avatar);
        tv_personal_name = (TextView)v.findViewById(R.id.tv_personal_name);
        tv_personal_email = (TextView)v.findViewById(R.id.tv_personal_email);
        ll_personal_profile = (LinearLayout)v.findViewById(R.id.ll_personal_profile);

        tv_setting_logout = (TextView) v.findViewById(R.id.tv_setting_logout);
        tv_setting_exit = (TextView) v.findViewById(R.id.tv_setting_exit);

        tv_setting_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogout();
            }
        });
        tv_setting_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExit();
            }
        });
    }

    private void initVariables() {
        // TODO Auto-generated method stub
        session = new SessionManager(getActivity());
        db = new SQLiteHandler(getActivity());
        call = new CallAPIHandler();
    }

    public void onLogout() {
        lauchAlertLogoutDialog();
    }
    public void onExit() {
        lauchAlertExitDialog();
    }
    private void lauchAlertLogoutDialog() {
        // TODO Auto-generated method stub
        AlertDialog.Builder dl_send = new AlertDialog.Builder(getActivity());
        dl_send.setTitle(getResources().getString(R.string.app_name));
        dl_send.setMessage(getResources().getString(R.string.alert_logout_title))
                .setPositiveButton(getResources().getString(R.string.bt_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        db.deleteUsers();
                        session.setLogin(false);
                        IOManager.signout();

                        Intent i = new Intent(getActivity(), LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.bt_no), null);

        AlertDialog dialog = dl_send.create();
        dialog.show();
    }
    private void lauchAlertExitDialog() {
        // TODO Auto-generated method stub
        AlertDialog.Builder dl_send = new AlertDialog.Builder(getActivity());
        dl_send.setTitle(getResources().getString(R.string.app_name));
        dl_send.setMessage(getResources().getString(R.string.alert_exit_title))
                .setPositiveButton(getResources().getString(R.string.bt_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        getActivity().finishAffinity();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.bt_no), null);

        AlertDialog dialog = dl_send.create();
        dialog.show();
    }
    public void setData() {
        // TODO Auto-generated method stub
        tv_personal_name.setText(Variables.userFName+" "+Variables.userLName);
        tv_personal_email.setText(Variables.userEmail);
        new loadRemoteIMG(img_personal_avatar).execute(Constants.FOLDER_AVATAR + Variables.userAvatar);
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

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Variables.curFrg = Constants.TAG_FRG_SETTING;
        MainActivity.setIndicator(Variables.curFrg);
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
