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
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.fragments.Frg_Note;
import com.example.shopmeet.fragments.Frg_Note_Edit;
import com.example.shopmeet.fragments.Frg_Task_Detail;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.NoteData;
import com.example.shopmeet.utils.CallAPIHandler;
import com.example.shopmeet.utils.ImageLoader;
import com.example.shopmeet.utils.SQLiteHandler;
import com.example.shopmeet.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.chat.video.android.manager.IOManager;

public class ListNoteAdapter extends BaseAdapter {
	private Context context;
    private List<NoteData> noteItems;
    private CallAPIHandler call;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private String today;

    public ListNoteAdapter(Context context, List<NoteData> items) {
        this.context = context;
        this.noteItems = items;
        session = new SessionManager(context);
        call = new CallAPIHandler();
        db = new SQLiteHandler(context);
        // Progress dialog
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(false);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Constants.DATETIME_FORMAT);
        today = df.format(c.getTime());

    }
 
    @Override
    public int getCount() {
        return noteItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return noteItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        final int iPosition = position;
		final NoteData m = noteItems.get(position);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.row_note, null);

        ImageView row_img = (ImageView)convertView.findViewById(R.id.row_img);
        TextView row_note_name = (TextView) convertView.findViewById(R.id.row_note_name);
        TextView row_note_date = (TextView) convertView.findViewById(R.id.row_note_date);
        ImageView img_note_edit = (ImageView) convertView.findViewById(R.id.img_note_edit);
        ImageView img_note_delete = (ImageView) convertView.findViewById(R.id.img_note_delete);
        TextView row_note_content = (TextView) convertView.findViewById(R.id.row_note_content);

        if (!m.getStaff_avatar().equalsIgnoreCase("")) {
            // ImageLoader class instance
            /*ImageLoader imgLoader = new ImageLoader(context.getApplicationContext());
            imgLoader.DisplayImage(Constants.FOLDER_AVATAR+m.getStaff_avatar(), R.drawable.no_avatar, row_img);*/
            new loadRemoteIMG(row_img).execute(Constants.FOLDER_AVATAR+m.getStaff_avatar());
        }

        row_note_name.setText(m.getStaff_fname()+" "+m.getStaff_lname());

        row_note_date.setText(m.getNote_modified().equalsIgnoreCase("") ? "" :
                Functions.getFriendlyDateCreated(today, m.getNote_modified(), Constants.DATETIME_FORMAT));

        row_note_content.setText(m.getNote_content());
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
                    onEdit(m.getNote_id(), m.getNote_content(), m.getNote_modified());
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
                    onDelete(m.getNote_id());
                } else {
                    Functions.toastString(context.getResources().getString(R.string.alert_note_not_permisson), context);
                }
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

    public void onEdit(String note_id, String content, String modified) {
        Frg_Note_Edit frg = new Frg_Note_Edit();

        Bundle b = new Bundle();
        b.putString("note_id", note_id);
        b.putString("content", content);
        b.putString("modified", modified);
        frg.setArguments(b);

        ((Activity)context).getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frg, Constants.TAG_FRG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_NOTE_EDIT;
    }
    public void onDelete(String note_id) {
        lauchAlertDeleteDialog(note_id);
    }
    private void lauchAlertDeleteDialog(final String note_id) {
        // TODO Auto-generated method stub
        AlertDialog.Builder dl_send = new AlertDialog.Builder(context);
        dl_send.setTitle(context.getResources().getString(R.string.app_name));
        dl_send.setMessage(context.getResources().getString(R.string.alert_delete_confirm))
                .setPositiveButton(context.getResources().getString(R.string.bt_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            prepareRequest(Variables.userToken, note_id);
                            dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.bt_no), null);

        AlertDialog dialog = dl_send.create();
        dialog.show();
    }
    private void prepareRequest(String token, String note_id) {
        if (!Functions.hasConnection(context)) {
            Functions.toastString(context.getResources().getString(R.string.alert_no_internet), context);
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("note_id", note_id);
        new RequestServer().execute(Constants.URL_API_NOTE_DEL, call.createQueryStringForParameters(params), note_id);
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
                Functions.toastString(Constants.ERR_NO_DATA_FROM_SERVER, context);
                return;
            }
            try {
                JSONObject jObj = new JSONObject(jsonStr);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    String note_id = jObj.getJSONObject("objects").getString("id");
                    Functions.toastString(context.getResources().getString(R.string.inform_deletenote_success), context);
                    for (NoteData d : noteItems) {
                        if (d.getNote_id().equalsIgnoreCase(note_id)) {
                            int index = noteItems.indexOf(d);
                            noteItems.remove(index);
                            break;
                        }
                    }
                    notifyDataSetChanged();
                    db.delNoteById(note_id);

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
