package com.example.shopmeet.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;

import com.example.shopmeet.R;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ListMemberSelectAdapter extends BaseAdapter {
	private Context context;
    private List<ListMemberSelectData> mbItems;

    public ListMemberSelectAdapter(Context context, List<ListMemberSelectData> items) {
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
	public int getIsSelected(int position) {
		return mbItems.get(position).getIsSelected();
	}
	public String getArrSelected() {
		String res = "";
		for (int i = 0; i < mbItems.size(); i++) {
			res += String.valueOf(mbItems.get(i).getIsSelected())+", ";
		}
		return res;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ListMemberSelectData m = mbItems.get(position);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (m.getIsDisplay() == 0) {
			convertView = mInflater.inflate(R.layout.row_empty, null);
			return convertView;
		} else {
			convertView = mInflater.inflate(R.layout.row_member_select, null);

			TextView row_title = (TextView) convertView.findViewById(R.id.row_title);
			ImageView row_img = (ImageView) convertView.findViewById(R.id.row_img);
			final CheckBox row_cb = (CheckBox) convertView.findViewById(R.id.row_cb);

			if (m.getIsSelected() == 1)
				row_cb.setChecked(true);
			else
				row_cb.setChecked(false);

			if (!m.getUrl_avatar().equalsIgnoreCase("")) {
				/*ImageLoader imgLoader = new ImageLoader(context.getApplicationContext());
				imgLoader.DisplayImage(Constants.FOLDER_AVATAR+m.getUrl_avatar(), R.drawable.no_avatar, row_img);*/
				new loadRemoteIMG(row_img).execute(Constants.FOLDER_AVATAR+m.getUrl_avatar());
			}

			row_title.setText(m.getMb_fname() + " " + m.getMb_lname());

			convertView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (m.getIsSelected() == 0) {
						m.setIsSelected(1);
						row_cb.setChecked(true);

					} else {
						m.setIsSelected(0);
						row_cb.setChecked(false);
					}
					Log.e("---------click", getArrSelected());
				}
			});
			row_cb.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (m.getIsSelected() == 0) {
						m.setIsSelected(1);
						row_cb.setChecked(true);

					} else {
						m.setIsSelected(0);
						row_cb.setChecked(false);
					}
					Log.e("---------click", getArrSelected());
				}
			});

			return convertView;
		}
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

	
	
	public static class ListMemberSelectData extends ListMemberAdapter.ListMemberData{
     	private int isSelected;

		public ListMemberSelectData() {
			super();
		}

        public ListMemberSelectData(String mb_id, String mb_uname, String mb_fname, String mb_lname,
							  String url_avatar, String mb_email, String mb_joindate, int isDisplay, int isSelected) {
			super(mb_id, mb_uname, mb_fname, mb_lname,
					url_avatar, mb_email, mb_joindate, isDisplay);
			this.isSelected = isSelected;
        }

		public int getIsSelected() {
			return isSelected;
		}

		public void setIsSelected(int isSelected) {
			this.isSelected = isSelected;
		}
	}
}
