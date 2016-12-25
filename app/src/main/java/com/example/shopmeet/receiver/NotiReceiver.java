package com.example.shopmeet.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.shopmeet.LoginActivity;
import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.utils.MyNotificationUtils;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by UserPC on 6/15/2016.
 */
public class NotiReceiver extends GcmListenerService {
    private static final String TAG = NotiReceiver.class.getSimpleName();
    private MyNotificationUtils mNotiUtils;

    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        String data = bundle.getString("com.nifty.Data");

        // app is in background. show the message in notification tray
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        generateTextNoti(getApplicationContext(), data, resultIntent);

		/*try {
			json = new JSONObject(intent.getExtras()
					.getString("com.nifty.Data"));
			Iterator itr = json.keys();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				// user-defined value to log output
				Log.d(TAG, "... " + key + "=>" + json.getString(key));
			}
			pushkey = json.getString("type");
            title = json.getString("title");
			body = json.getString("body");
		} catch (JSONException e) {
			// error
		}
		if (json != null && json.length() > 0)
			generateNotification(context, pushkey, title, body);*/
    }
    private void generateTextNoti(Context context, String data, Intent resultIntent) {
        // TODO Auto-generated method stub
        mNotiUtils = new MyNotificationUtils(context);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mNotiUtils.showNotificationMessage("title", data, resultIntent);
    }
    /*private void generateNotification(Context context, String akey,
                                      String atitle, String abody) {
        manager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        Intent intent = new Intent("com.example.shopmeet.receiver.NotiReceiver");

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, 0);

        Notification.Builder builder = new Notification.Builder(context);

        builder.setAutoCancel(false);
        builder.setTicker("this is ticker text");
        builder.setContentTitle(atitle);
        builder.setContentText(abody);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setNumber(100);
        builder.build();

        myNotication = builder.getNotification();

        //// type != 1 to display notification
        if (Integer.valueOf(akey) != 1
                && !Functions.isActivityRunning(context, MainActivity.class)) {
            manager.notify(Constants.NOTIFICATION_ID, myNotication);
        }
    }*/
}