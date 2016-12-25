package com.example.shopmeet.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shopmeet.R;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.model.CMessageData;

public class CMessageAdapter extends RecyclerView.Adapter<CMessageAdapter.MViewHolder> {
	
	private static String TAG = CMessageAdapter.class.getSimpleName();

    private List<CMessageData> messageList;
    private static String today;
    private Context context;
    private int chatType = Constants.TYPE_PERSON; // 0 - chat 1-1, 1- chat group
    private String myUserID; // my user id
    private int SELF = -1;
    private int HIDE = -2;
    private Bitmap mBitmap;
    private ProgressDialog pDialog;

 // For download file Document
 	private Uri pathDocOnDevice;
 	private int downloadedSize = 0, totalsize;
 	float per = 0;
    
    private int position;
    
    public CMessageAdapter() {
		// TODO Auto-generated constructor stub
	}
    public CMessageAdapter(Context ct, List<CMessageData> data, String uID, int chatType) {
    	this.context = ct;
    	this.messageList = data;
        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        this.myUserID = uID;
        this.chatType = chatType;
    }
    
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
 
    public class MViewHolder extends RecyclerView.ViewHolder {
        TextView row_tv, row_timestamp, row_sender;
        ImageView row_img;
 
        public MViewHolder(View view) {
            super(view);
            row_tv = (TextView) itemView.findViewById(R.id.row_tv);
            row_timestamp = (TextView) itemView.findViewById(R.id.row_timestamp);
            if (chatType == Constants.TYPE_GROUP) {// chat group
                row_sender = (TextView) itemView.findViewById(R.id.row_sender);
            }
            row_img = (ImageView)itemView.findViewById(R.id.row_img);
            row_img.setMaxWidth(900); // only display,so, for best performance, need to resize bitmap below
        	row_img.setMaxHeight(700);
        	pDialog = new ProgressDialog(context);
            pDialog.setCancelable(true);
        }
    }
    
    @Override
    public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        // view type is to identify where to render the chat message
        // left or right
        if (viewType == HIDE) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_empty_chat, parent, false);
        } else if (viewType == SELF) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_chat_item_self, parent, false);
        } else {
            if (chatType == Constants.TYPE_GROUP) {// chat group
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_chatg_item_other, parent, false);
            } else {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_chat_item_other, parent, false);
            }
        }
        return new MViewHolder(itemView);
    }
    @Override
    public int getItemViewType(int position) {
        CMessageData message = messageList.get(position);
        if (message.getIsDisplay() == 0) {
            return HIDE;
        }
        if (message.getSender().getId().equals(myUserID)) {
            return SELF;
        }
        return position;
    }
    @Override
    public void onBindViewHolder(final MViewHolder holder, int position) {
        final CMessageData data = messageList.get(position);

        if (data.getIsDisplay() == 1) {
            holder.row_timestamp.setText(Functions.getChatTimeStamp(today, data.getCreatedAt(), Constants.DATETIME_FORMAT));
            if (chatType == Constants.TYPE_GROUP && !data.getSender().getId().equalsIgnoreCase(myUserID)) {// chat group and other
                holder.row_sender.setText(data.getSender().getName().equals("") ? data.getSender().getId() : data.getSender().getName());
            }

            final String content = data.getMessage();
            if (data.getContentType().equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_MSG) ||
                    data.getContentType().equalsIgnoreCase(CMessageData.CHAT_CONTENT_TYPE_CALL)) { // Text
                holder.row_tv.setText(content);
                holder.row_img.setVisibility(View.GONE);
                holder.row_tv.setVisibility(View.VISIBLE);
            }
        }

    }
	protected void onDisplay(String filename, int isImg) {
		// TODO Auto-generated method stub
		String url = Constants.URL_FILE_FOLDER_CHAT+filename;
		if (isImg == 2) {
			new loadRemoteDOCDialog().execute(url, filename);
		}

	}
	public class loadRemoteDOCDialog extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Loading...");
            showDialog();
        }
        @Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
        	File f = downloadDoc(args[0], args[1]);
        	if (f != null)
        		pathDocOnDevice = Uri.fromFile(f);
            
            return null;
        }
        @Override       
        protected void onPostExecute(String args) {
        	hideDialog();
        	if (pathDocOnDevice != null || !pathDocOnDevice.toString().equals("")) {
	        	try {
	                Intent intent = new Intent(Intent.ACTION_VIEW);
	                Functions.findDataType(intent, pathDocOnDevice);
	                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                context.startActivity(intent);
	
	            } catch (ActivityNotFoundException e) {
	            	e.printStackTrace();
	            }
        	} else {
        		Functions.toastString("File does not exist", context);
        	}
        }
    }
	
	File downloadDoc(String urlOnServer, String filenamOnServer) {
        File file = null;
        try {
            URL url = new URL(urlOnServer);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
 
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            // connect
            urlConnection.connect();
 
            // set the path where we want to save the file
            File SDCardRoot = Environment.getExternalStorageDirectory();
            // create a new file, to save the downloaded file
            file = new File(SDCardRoot, filenamOnServer);
 
            FileOutputStream fileOutput = new FileOutputStream(file);
            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
 
            // create a buffer...
            byte[] buffer = new byte[1024 * 1024];  
            int bufferLength = 0;
 
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                per = ((float) downloadedSize / totalsize) * 100;
            }
            // close the output stream when complete //
            fileOutput.close();
            
 
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
        	e.printStackTrace();
        } catch (final Exception e) {
        	e.printStackTrace();
        }
        return file;
    }
	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}
	 
	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}

	@Override
    public int getItemCount() {
        return messageList.size();
    }

}