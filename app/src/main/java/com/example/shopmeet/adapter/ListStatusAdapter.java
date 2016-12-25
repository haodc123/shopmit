package com.example.shopmeet.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.fragments.Frg_Comment;
import com.example.shopmeet.fragments.Frg_Note_Edit;
import com.example.shopmeet.fragments.Frg_Status_Edit;
import com.example.shopmeet.fragments.Frg_Wholike;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.NoteData;
import com.example.shopmeet.model.StatusData;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.ImageLoader;
import com.example.shopmeet.utils.SQLiteHandler;
import com.example.shopmeet.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.manager.IOManager;

public class ListStatusAdapter extends BaseAdapter {
	private Context context;
    private List<StatusData> statusItems;
    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;

    public ListStatusAdapter(Context context, List<StatusData> items) {
        this.context = context;
        this.statusItems = items;
        session = new SessionManager(context);
        call = new CallAPIHandler();
        db = new SQLiteHandler(context);
        // Progress dialog
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);

    }
 
    @Override
    public int getCount() {
        return statusItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return statusItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        final int iPosition = position;
		final StatusData m = statusItems.get(position);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.row_status, null);

        ImageView row_img = (ImageView)convertView.findViewById(R.id.row_img);
        TextView row_note_name = (TextView) convertView.findViewById(R.id.row_note_name);
        TextView row_note_date = (TextView) convertView.findViewById(R.id.row_note_date);
        ImageView img_note_edit = (ImageView) convertView.findViewById(R.id.img_note_edit);
        ImageView img_note_delete = (ImageView) convertView.findViewById(R.id.img_note_delete);
        TextView row_note_content = (TextView) convertView.findViewById(R.id.row_note_content);
        Button row_bt_like = (Button)convertView.findViewById(R.id.row_bt_like);
        TextView row_tv_comment = (TextView)convertView.findViewById(R.id.row_tv_comment);
        TextView row_tv_numcomment = (TextView) convertView.findViewById(R.id.row_tv_numcomment);
        TextView row_tv_numlike = (TextView) convertView.findViewById(R.id.row_tv_numlike);
        ImageView row_img_numlike = (ImageView) convertView.findViewById(R.id.row_img_numlike);

        if (!m.getStaff_avatar().equalsIgnoreCase("")) {
            // ImageLoader class instance
            /*ImageLoader imgLoader = new ImageLoader(context.getApplicationContext());
            imgLoader.DisplayImage(Constants.FOLDER_AVATAR+m.getStaff_avatar(), R.drawable.no_avatar, row_img);*/
            new loadRemoteIMG(row_img).execute(Constants.FOLDER_AVATAR + m.getStaff_avatar());
        }

        row_note_name.setText(m.getStaff_fname()+" "+m.getStaff_lname());
        row_note_date.setText(m.getStatus_modified().equalsIgnoreCase("") ? "" : m.getStatus_modified().substring(0, 10));
        row_note_content.setText(m.getStatus_content());
        row_tv_numcomment.setText(String.valueOf(m.getNumComment()));
        row_tv_numlike.setText(String.valueOf(m.getNumLike()));

        if (m.getIsLike() == 1) {
            row_bt_like.setBackgroundResource(R.drawable.ic_like1);
        } else {
            row_bt_like.setBackgroundResource(R.drawable.ic_like0);
        }

        if (m.getStaff_id().equalsIgnoreCase(Variables.userID)) {
            img_note_edit.setVisibility(View.VISIBLE);
            img_note_delete.setVisibility(View.VISIBLE);
        } else {
            img_note_edit.setVisibility(View.GONE);
            img_note_delete.setVisibility(View.GONE);
        }

        img_note_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (m.getStaff_id().equalsIgnoreCase(Variables.userID)) {
                    onEdit(m.getStatus_id(), m.getStatus_content(), m.getStatus_modified());
                } else {
                    Functions.toastString(context.getResources().getString(R.string.alert_note_not_permisson), context);
                }
            }
        });
        img_note_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (m.getStaff_id().equalsIgnoreCase(Variables.userID)) {
                    onDelete(m.getStatus_id());
                } else {
                    Functions.toastString(context.getResources().getString(R.string.alert_note_not_permisson), context);
                }
            }
        });
        row_tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onComment(m.getStatus_id(), m.getStatus_content());
            }
        });
        row_bt_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onLike(m.getStatus_id(), m.getIsLike());
            }
        });
        row_tv_numlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onWholike(m.getStatus_id());
            }
        });
        row_img_numlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onWholike(m.getStatus_id());
            }
        });

        return convertView;
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
                if (!Functions.hasConnection(context)) {
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

    public void onEdit(String status_id, String content, String modified) {
        Frg_Status_Edit frg = new Frg_Status_Edit();

        Bundle b = new Bundle();
        b.putString("status_id", status_id);
        b.putString("content", content);
        b.putString("modified", modified);
        frg.setArguments(b);

        ((Activity)context).getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frg, Constants.TAG_FRG_STATUS_EDIT)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_STATUS_EDIT;
    }
    public void onWholike(String status_id) {
        Frg_Wholike frg = new Frg_Wholike();

        Bundle b = new Bundle();
        b.putString("status_id", status_id);
        frg.setArguments(b);

        ((Activity)context).getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frg, Constants.TAG_FRG_WHOLIKE)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_WHOLIKE;
    }
    public void onComment(String status_id, String content) {
        Frg_Comment frg = new Frg_Comment();

        Bundle b = new Bundle();
        b.putString("status_id", status_id);
        b.putString("content", content);
        frg.setArguments(b);

        ((Activity)context).getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frg, Constants.TAG_FRG_COMMENT)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_COMMENT;
    }
    public void onDelete(String status_id) {
        lauchAlertDeleteDialog(status_id);
    }

    private void lauchAlertDeleteDialog(final String status_id) {
        // TODO Auto-generated method stub
        AlertDialog.Builder dl_send = new AlertDialog.Builder(context);
        dl_send.setTitle(context.getResources().getString(R.string.app_name));
        dl_send.setMessage(context.getResources().getString(R.string.alert_delete_confirm))
                .setPositiveButton(context.getResources().getString(R.string.bt_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            prepareDelete(Variables.userToken, status_id);
                            dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.bt_no), null);

        AlertDialog dialog = dl_send.create();
        dialog.show();
    }
    public void onLike(String status_id, int isLike) {
        prepareLike(Variables.userToken, status_id, isLike);
    }
    private void prepareLike(String token, String status_id, int isLike) {
        if (!Functions.hasConnection(context)) {
            Functions.toastString(context.getResources().getString(R.string.alert_no_internet), context);
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("timeline_id", status_id);
        if (isLike == 0) // hien tai dang unline, nen action se la LIKE
            params.put("action", String.valueOf(StatusData.STATUS_DO_LIKE));
        else
            params.put("action", String.valueOf(StatusData.STATUS_DO_UNLIKE));
        new RequestLike().execute(Constants.URL_API_STATUS_LIKE, call.createQueryStringForParameters(params), status_id);
    }
    private class RequestLike extends AsyncTask<String, Void, String> {
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
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, context);
                return;
            }
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    String status_id = jObj.getJSONObject("objects").getString("timeline_id");
                    int action = jObj.getJSONObject("objects").getInt("like");
                    for (StatusData d : statusItems) {
                        if (d.getStatus_id().equalsIgnoreCase(status_id)) {
                            int index = statusItems.indexOf(d);
                            if (action == StatusData.STATUS_DO_LIKE) {
                                statusItems.get(index).setIsLike(1);
                                statusItems.get(index).setNumLike(statusItems.get(index).getNumLike()+1);
                            } else {
                                statusItems.get(index).setIsLike(0);
                                statusItems.get(index).setNumLike(statusItems.get(index).getNumLike()-1);
                            }
                            break;
                        }
                    }
                    notifyDataSetChanged();
                    db.updateStatusLike(status_id, String.valueOf(action));

                } else {
                    JSONObject jOObjects = jObj.getJSONObject("objects");
                    int is_work = jOObjects.getInt("is_work");
                    if (is_work == 0) {
                        Functions.toastString(context.getResources().getString(R.string.alert_not_working), context);
                        // Logout
                        db.deleteUsers();
                        session.setLogin(false);
                        IOManager.signout();

                        Intent i = new Intent(context, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(i);
                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, context);
                    }
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON, context);
            }
        }

    }
    private void prepareDelete(String token, String status_id) {
        if (!Functions.hasConnection(context)) {
            Functions.toastString(context.getResources().getString(R.string.alert_no_internet), context);
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("timeline_id", status_id);
        new RequestDelete().execute(Constants.URL_API_STATUS_DEL, call.createQueryStringForParameters(params), status_id);
    }
    private class RequestDelete extends AsyncTask<String, Void, String> {
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
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, context);
                return;
            }
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    String status_id = jObj.getJSONObject("objects").getString("timeline_id");
                    Functions.toastString(context.getResources().getString(R.string.inform_deletenote_success), context);
                    for (StatusData d : statusItems) {
                        if (d.getStatus_id().equalsIgnoreCase(status_id)) {
                            int index = statusItems.indexOf(d);
                            statusItems.remove(index);
                            break;
                        }
                    }
                    notifyDataSetChanged();
                    db.delStatusById(status_id);

                } else {
                    JSONObject jOObjects = jObj.getJSONObject("objects");
                    int is_work = jOObjects.getInt("is_work");
                    if (is_work == 0) {
                        Functions.toastString(context.getResources().getString(R.string.alert_not_working), context);
                        // Logout
                        db.deleteUsers();
                        session.setLogin(false);
                        IOManager.signout();

                        Intent i = new Intent(context, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(i);
                    } else {
                        // Error in getting. Get the error message
                        String errorMsg = jObj.getString("message");
                        Log.e(Constants.TAG_API, Constants.ERR_LOGIN + errorMsg);
                        Functions.toastString(Constants.ERR_GETTING + ": " + errorMsg, context);
                    }
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Log.e(Constants.TAG_API, jsonStr.toString() + Constants.ERR_JSON + e.getMessage());
                Functions.toastString(Constants.ERR_JSON, context);
            }
        }

    }
}
