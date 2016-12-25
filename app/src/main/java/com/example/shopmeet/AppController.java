package com.example.shopmeet;

import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.MyDiskCache;
/*import com.nifty.cloud.mb.NCMB;
import com.nifty.cloud.mb.NCMBAnalytics;
import com.nifty.cloud.mb.NCMBException;
import com.nifty.cloud.mb.NCMBInstallation;
import com.nifty.cloud.mb.NCMBPush;
import com.nifty.cloud.mb.NCMBQuery;
import com.nifty.cloud.mb.RegistrationCallback;*/

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

public class AppController extends Application {
 
    public static final String TAG = AppController.class.getSimpleName();
 
    private static AppController mInstance;

    //private RequestQueue mRequestQueue;
 
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        setInitCache();
        //setInitNifty();
    }

	private void setInitCache() {
		// TODO Auto-generated method stub
        if (Variables.mDCache == null)
		    Variables.mDCache = new MyDiskCache(this, Constants.APP_NAME, Constants.DISK_CACHE_SIZE);
	}

	public static synchronized AppController getInstance() {
        return mInstance;
    }

    /*private void setInitNifty() {
        // TODO Auto-generated method stub
        Variables.deviceID = Functions.getDeviceID(this);
        Variables.androidVersion = android.os.Build.VERSION.RELEASE;
        Variables.deviceName = android.os.Build.MANUFACTURER + android.os.Build.PRODUCT;
        NCMB.initialize(this, Constants.KEY_SERVER_NIFTY, Constants.KEY_CLIENT_NIFTY);
        NCMBPush.setDefaultPushCallback(this, MainActivity.class);

        final NCMBInstallation installation = NCMBInstallation.getCurrentInstallation();
        installation.getRegistrationIdInBackground(Constants.KEY_SENDERID_GCM, new RegistrationCallback(){
            @Override
            public void done(NCMBException e) {
                NCMBInstallation prevInstallation = null;
                if (e == null) {
                    try {
                        NCMBQuery<NCMBInstallation> query = NCMBInstallation
                                .getQuery();

                        query.whereEqualTo("device_id", Variables.deviceID);
                        prevInstallation = query.getFirst();
                        if (prevInstallation != null) {
                            if (!prevInstallation
                                    .getString("device_id")
                                    .trim()
                                    .equals(Variables.deviceID
                                            .trim())) {}
                        }
                        if (prevInstallation == null) {
                            installation.put("device_id",
                                    Variables.deviceID.trim());
                            //installation.put("channels", Constants.NIFTY_CHANNEL);
                            installation.put("osversion", Variables.androidVersion.trim());
                            installation.put("device_name", Variables.deviceName.trim());
                            installation.save();
                        }
                    } catch (NCMBException le) {}
                } else {
                    e.printStackTrace();
                }
            }
        });
        // プッシュ開封登録の実施
        NCMBAnalytics.trackAppOpened(new Intent(getBaseContext(), AppController.class));
    }*/

    // ------ Volley library
    /*public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }*/
}