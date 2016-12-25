package com.example.shopmeet.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.example.shopmeet.R;
import com.example.shopmeet.fragments.Frg_Chat;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.ImageLoader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class ListMemberAdapter extends BaseAdapter {
	private Context context;
    private List<ListMemberData> mbItems;
    
    public ListMemberAdapter(Context context, List<ListMemberData> items) {
        this.context = context;
        this.mbItems = items;
    }
 
    @Override
    public int getCount() {
        return mbItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return mbItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ListMemberData m = mbItems.get(position);
		 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
        convertView = mInflater.inflate(R.layout.row_member, null);
 
        TextView row_title = (TextView) convertView.findViewById(R.id.row_title);
        ImageView row_img = (ImageView) convertView.findViewById(R.id.row_img);
        
        //new loadRemoteIMG(row_img).execute(m.getUrl_avatar());
		// ImageLoader class instance
		ImageLoader imgLoader = new ImageLoader(context.getApplicationContext());
		imgLoader.DisplayImage(m.getUrl_avatar(), R.drawable.no_avatar, row_img);
        	
        row_title.setText(m.getMb_fname() + " " + m.getMb_lname());
        
        convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				displayDialog(m.getMb_id(), m.getMb_fname() + " " + m.getMb_lname(), m.getMb_email(), m.getMb_joindate(), m.getUrl_avatar());
			}
		});
        
        return convertView;
	}
	protected void displayDialog(final String id, final String name, final String email, String joindate, String url) {
		// TODO Auto-generated method stub
		final Dialog mDialogFile = new Dialog(context);
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

		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat(Constants.DATETIME_FORMAT);
		String today = df.format(c.getTime());
		tv_dl_joindate.setText("Joined "+Functions.getFriendlyJoinDate(today, joindate, Constants.DATETIME_FORMAT));

		new loadRemoteIMG(img_dl_avatar).execute(url);
		ll_dl_chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialogFile.dismiss();
				goChat(id, name, email);
			}
		});
		ll_dl_call.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialogFile.dismiss();
				goCall(id, name, email);
			}
		});
		ll_dl_video.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialogFile.dismiss();
				goVideo(id, name, email);
			}
		});
		mDialogFile.show();
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
	public void goChat(String id, String name, String email) {
		Frg_Chat frgChat = new Frg_Chat();

		Bundle b = new Bundle();
		b.putInt("chatType", Constants.TYPE_PERSON);
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
		Frg_Call frgCall = new Frg_Call();

		Bundle b = new Bundle();
		b.putInt("callType", Constants.TYPE_PERSON);
		b.putString("partner_id", id);
		b.putString("partner_name", name);
		frgCall.setArguments(b);

		((Activity)context).getFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, frgCall)
				.addToBackStack(null)
				.commit();

		Variables.curFrg = Constants.TAG_FRG_CALL;
	}
	public void goVideo(String id, String name, String email) {

	}
	
	
	public static class ListMemberData {
    	private String mb_id, mb_uname, mb_fname, mb_lname, url_avatar, mb_email, mb_joindate;
     
        public ListMemberData() {
        }
     
        public ListMemberData(String mb_id, String mb_uname, String mb_fname, String mb_lname,
							  String url_avatar, String mb_email, String mb_joindate) {
            this.mb_id = mb_id;
            this.mb_uname = mb_uname;
			this.mb_fname = mb_fname;
			this.mb_lname = mb_lname;
            this.url_avatar = url_avatar;
			this.mb_email = mb_email;
			this.mb_joindate = mb_joindate;
        }

		public String getMb_fname() {
			return mb_fname;
		}

		public void setMb_fname(String mb_fname) {
			this.mb_fname = mb_fname;
		}

		public String getMb_lname() {
			return mb_lname;
		}

		public void setMb_lname(String mb_lname) {
			this.mb_lname = mb_lname;
		}

		public String getMb_id() {
			return mb_id;
		}

		public void setMb_id(String mb_id) {
			this.mb_id = mb_id;
		}

		public String getMb_uname() {
			return mb_uname;
		}

		public void setMb_uname(String mb_uname) {
			this.mb_uname = mb_uname;
		}

		public String getUrl_avatar() {
			return url_avatar;
		}

		public void setUrl_avatar(String url_avatar) {
			this.url_avatar = url_avatar;
		}

		public String getMb_email() {
			return mb_email;
		}

		public void setMb_email(String mb_email) {
			this.mb_email = mb_email;
		}

		public String getMb_joindate() {
			return mb_joindate;
		}

		public void setMb_joindate(String mb_joindate) {
			this.mb_joindate = mb_joindate;
		}

	}
}
