package com.example.shopmeet.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.shopmeet.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.shopmeet.adapter.ListMemberAdapter.ListMemberData;
import com.example.shopmeet.fragments.Frg_Chat;
import com.example.shopmeet.fragments.Frg_Group;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.view.MyExpandableListView;

public class ListGroupAdapter extends BaseAdapter {
	private Context context;
    private List<ListGroupData> groupsItems;
    
    public ListGroupAdapter(Context context, List<ListGroupData> items) {
        this.context = context;
        this.groupsItems = items;
    }
 
    @Override
    public int getCount() {
        return groupsItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return groupsItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        final int iPosition = position;
		final ListGroupData m = groupsItems.get(position);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = mInflater.inflate(R.layout.row_group, null);

        TextView row_title = (TextView) convertView.findViewById(R.id.row_title);
        ImageView row_img = (ImageView) convertView.findViewById(R.id.row_img);
        TextView row_content = (TextView) convertView.findViewById(R.id.row_content);

        row_title.setText(m.getGroup_name());
        row_content.setText(m.getStaffs_string());
        new loadRemoteIMG(row_img).execute(m.getGroup_avatar());

        convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                goGroup(m.getGroup_id(), m.getGroup_name(), m.getGroup_create_at(), m.getGroup_avatar());
			}
		});

        return convertView;
	}

    protected void goGroup(final String group_id, final String group_name, final String group_create_at, final String group_avatar) {
        Frg_Group frgGroup = new Frg_Group();

        Bundle b = new Bundle();
        b.putString("group_id", group_id);
        b.putString("group_name", group_name);
        b.putString("group_create_at", group_create_at);
        b.putString("group_avatar", group_avatar);
        frgGroup.setArguments(b);

        ((Activity)context).getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frgGroup, Constants.TAG_FRG_GROUP)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_GROUP;
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
    protected void displayDialog(final String id, final String name, final String create_at, final int position) {
        // TODO Auto-generated method stub
        final Dialog mDialogFile = new Dialog(context);
        mDialogFile.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialogFile.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialogFile.setContentView(R.layout.dialog_group);
        mDialogFile.setTitle("");

        TextView tv_dl_name, tv_dl_create;
        LinearLayout ll_dl_chat, ll_dl_call, ll_dl_video, ll_dl_timeline, ll_dl_sendfile;
        ImageView img_dl_avatar;
        tv_dl_name = (TextView)mDialogFile.findViewById(R.id.tv_dl_name);
        tv_dl_create = (TextView)mDialogFile.findViewById(R.id.tv_dl_create);
        ll_dl_chat = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_chat);
        ll_dl_call = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_call);
        ll_dl_video = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_video);
        ll_dl_timeline = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_timeline);
        ll_dl_sendfile = (LinearLayout)mDialogFile.findViewById(R.id.ll_dl_sendfile);
        img_dl_avatar = (ImageView)mDialogFile.findViewById(R.id.img_dl_avatar);
        tv_dl_name.setText(name);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Constants.DATETIME_FORMAT);
        String today = df.format(c.getTime());
        tv_dl_create.setText("Created "+Functions.getFriendlyJoinDate(today, create_at, Constants.DATETIME_FORMAT));

        if (position%2 == 1) {
            img_dl_avatar.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_shop));
        } else {
            img_dl_avatar.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_shop2));
        }
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
        ll_dl_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogFile.dismiss();
                goTimeline(id, name);
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
    public void goChat(String id, String name) {
        Frg_Chat frgChat = new Frg_Chat();

        Bundle b = new Bundle();
        b.putInt("chatType", Constants.TYPE_GROUP);
        b.putString("partner_id", id);
        b.putString("partner_name", name);
        frgChat.setArguments(b);

        ((Activity)context).getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frgChat, Constants.TAG_FRG_CHAT)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_CHAT;
    }
    public void goCall(String id, String name) {

    }
    public void goVideo(String id, String name) {

    }
    public void goTimeline(String id, String name) {

    }
    public void goSendFile(String id, String name) {

    }
	public static class ListGroupData {
    	private String group_id, group_name, group_create_at, staffs_string, group_avatar;
     
        public ListGroupData() {
        }
     
        public ListGroupData(String group_id, String group_name, String group_create_at, String staffs_string, String group_avatar) {
            this.group_id = group_id;
            this.group_name = group_name;
            this.group_create_at = group_create_at;
            this.staffs_string = staffs_string;
            this.group_avatar = group_avatar;
        }

        public String getGroup_create_at() {
            return group_create_at;
        }

        public void setGroup_create_at(String group_create_at) {
            this.group_create_at = group_create_at;
        }

        public String getGroup_id() {
			return group_id;
		}

		public void setGroup_id(String group_id) {
			this.group_id = group_id;
		}

		public String getGroup_name() {
			return group_name;
		}

		public void setGroup_name(String group_name) {
			this.group_name = group_name;
		}

        public String getStaffs_string() {
            return staffs_string;
        }

        public void setStaffs_string(String staffs_string) {
            this.staffs_string = staffs_string;
        }

        public String getGroup_avatar() {
            return group_avatar;
        }

        public void setGroup_avatar(String group_avatar) {
            this.group_avatar = group_avatar;
        }
    }
}
