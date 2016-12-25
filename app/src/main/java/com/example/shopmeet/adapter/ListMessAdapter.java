package com.example.shopmeet.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shopmeet.R;
import com.example.shopmeet.fragments.Frg_Chat;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.model.CMessageData;
import com.example.shopmeet.utils.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by UserPC on 6/10/2016.
 */
public class ListMessAdapter extends BaseAdapter {
    private Context context;
    private List<ListMessData> messItems;

    public ListMessAdapter(Context context, List<ListMessData> items) {
        this.context = context;
        this.messItems = items;
    }

    @Override
    public int getCount() {
        return messItems.size();
    }

    @Override
    public Object getItem(int position) {
        return messItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ListMessData m = messItems.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.row_mess, null);

        TextView row_name = (TextView) convertView.findViewById(R.id.row_name);
        TextView row_date = (TextView) convertView.findViewById(R.id.row_date);
        TextView row_mess = (TextView) convertView.findViewById(R.id.row_mess);
        ImageView row_img = (ImageView) convertView.findViewById(R.id.row_img);

        if (!m.getPartner_avatar().equalsIgnoreCase("")) {
            // ImageLoader class instance
            /*ImageLoader imgLoader = new ImageLoader(context.getApplicationContext());
            imgLoader.DisplayImage(Constants.FOLDER_AVATAR+m.getPartner_avatar(), R.drawable.no_avatar, row_img);*/
            new loadRemoteIMG(row_img).execute(Constants.FOLDER_AVATAR + m.getPartner_avatar());
        }

        if (m.getIsPrivate() == 1) {
            row_name.setText(m.getPartner_fname()+" "+m.getPartner_lname());
        } else {
            row_name.setText(m.getG_name());
        }

        if (Functions.getPeriod(m.getDate_last_mess(), "yyyy-MM-dd hh:mm:ss").equalsIgnoreCase("today"))
            row_date.setText(m.getDate_last_mess().substring(11, 19));
        else
            row_date.setText(m.getDate_last_mess());

        if (m.getType().equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG))
            if (m.getAuthorid_last_mess().equalsIgnoreCase(Variables.userID))
                row_mess.setText("Me: "+Functions.decodeBase64(m.getLast_mess()));
            else
                row_mess.setText(Functions.decodeBase64(m.getLast_mess()));
        else
            row_mess.setText(m.getLast_mess());

        if (m.getUn_seem() > 0) {
            row_mess.setTextColor(context.getResources().getColor(R.color.dark_global));
            row_mess.setTypeface(null, Typeface.BOLD);
        } else {
            row_mess.setTextColor(context.getResources().getColor(R.color.gray_global));
            row_mess.setTypeface(null, Typeface.NORMAL);
        }

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (m.getIsPrivate() == 1) {
                    goChat(m.getIsPrivate(), m.getPartner_id(), m.getPartner_fname() + " " + m.getPartner_lname(), "");
                } else {
                    goChat(m.getIsPrivate(), m.getPartner_id(), m.getG_name(), "");
                }
            }
        });

        return convertView;
    }

    /*protected void displayDialog(final String id, final String name, final String email, String joindate, String url) {
        // TODO Auto-generated method stub
        Dialog mDialogFile = new Dialog(context);
        mDialogFile.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialogFile.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialogFile.setContentView(R.layout.dialog_member);
        mDialogFile.setTitle("");

        TextView tv_dl_name, tv_dl_email, tv_dl_joindate;
        LinearLayout ll_dl_chat, ll_dl_call, ll_dl_video;
        ImageView img_dl_avatar;
        tv_dl_name = (TextView)mDialogFile.findViewById(R.id.tv_dl_name);
        tv_dl_email = (TextView)mDialogFile.findViewById(R.id.tv_dl_email);
        tv_dl_joindate = (TextView)mDialogFile.findViewById(R.id.tv_dl_joindate);
        ll_dl_chat = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_chat);
        ll_dl_call = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_call);
        ll_dl_video = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_video);
        img_dl_avatar = (ImageView)mDialogFile.findViewById(R.id.img_dl_avatar);
        tv_dl_name.setText(name);
        tv_dl_email.setText(email);
        tv_dl_joindate.setText(joindate);
        new loadRemoteIMG(img_dl_avatar).execute(url);

        ll_dl_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goCall(id, name, email);
            }
        });
        ll_dl_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goVideo(id, name, email);
            }
        });
        mDialogFile.show();
    }*/
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
    public void goChat(int isPrivate, String id, String name, String email) {
        Frg_Chat frgChat = new Frg_Chat();

        Bundle b = new Bundle();
        b.putInt("chatType", isPrivate == 1 ? Constants.TYPE_PERSON : Constants.TYPE_GROUP);
        b.putString("partner_id", id);
        b.putString("partner_name", name);
        frgChat.setArguments(b);

        ((Activity)context).getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frgChat, Constants.TAG_FRG_CHAT)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_CHAT;
    }
    public void goCall(String id, String name, String email) {

    }
    public void goVideo(String id, String name, String email) {

    }


    public static class ListMessData {
        private String partner_id, partner_fname, partner_lname, g_name, partner_avatar,
                last_mess, authorid_last_mess, date_last_mess, type, local_conv_id;
        private int un_seem, isPrivate;

        public ListMessData() {
        }

        public ListMessData(String partner_id, String partner_fname, String partner_lname, String g_name, String partner_avatar,
                            String last_mess, String authorid_last_mess, String date_last_mess,
                            int un_seem, int isPrivate, String type, String local_conv_id) {
            this.partner_id = partner_id;
            this.partner_fname = partner_fname;
            this.partner_lname = partner_lname;
            this.g_name = g_name;
            this.partner_avatar = partner_avatar;
            this.last_mess = last_mess;
            this.authorid_last_mess = authorid_last_mess;
            this.date_last_mess = date_last_mess;
            this.un_seem = un_seem;
            this.isPrivate = isPrivate;
            this.type = type;
            this.local_conv_id = local_conv_id;
        }

        public String getLocal_conv_id() {
            return local_conv_id;
        }

        public void setLocal_conv_id(String local_conv_id) {
            this.local_conv_id = local_conv_id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPartner_id() {
            return partner_id;
        }

        public void setPartner_id(String partner_id) {
            this.partner_id = partner_id;
        }

        public String getPartner_fname() {
            return partner_fname;
        }

        public void setPartner_fname(String partner_fname) {
            this.partner_fname = partner_fname;
        }

        public String getPartner_lname() {
            return partner_lname;
        }

        public void setPartner_lname(String partner_lname) {
            this.partner_lname = partner_lname;
        }

        public String getG_name() {
            return g_name;
        }

        public void setG_name(String g_name) {
            this.g_name = g_name;
        }

        public String getPartner_avatar() {
            return partner_avatar;
        }

        public void setPartner_avatar(String partner_avatar) {
            this.partner_avatar = partner_avatar;
        }

        public String getLast_mess() {
            return last_mess;
        }

        public void setLast_mess(String last_mess) {
            this.last_mess = last_mess;
        }

        public String getAuthorid_last_mess() {
            return authorid_last_mess;
        }

        public void setAuthorid_last_mess(String authorid_last_mess) {
            this.authorid_last_mess = authorid_last_mess;
        }

        public String getDate_last_mess() {
            return date_last_mess;
        }

        public void setDate_last_mess(String date_last_mess) {
            this.date_last_mess = date_last_mess;
        }

        public int getUn_seem() {
            return un_seem;
        }

        public void setUn_seem(int un_seem) {
            this.un_seem = un_seem;
        }

        public int getIsPrivate() {
            return isPrivate;
        }

        public void setIsPrivate(int isPrivate) {
            this.isPrivate = isPrivate;
        }
    }
}

