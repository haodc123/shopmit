package com.example.shopmeet.utils;

import java.util.Arrays;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.shopmeet.AppController;
import com.example.shopmeet.R;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;

import java.util.Arrays;
import java.util.List;

/**
 * Created by UserPC on 6/15/2016.
 */
public class MyNotificationUtils {
    private final String TAG = MyNotificationUtils.class.getSimpleName();
    private Context ct;
    public MyNotificationUtils() {

    }
    public MyNotificationUtils(Context ct) {
        this.ct = ct;
    }

    public void showNotificationMessage(final String title, final String msg, final Intent intent) {
        // Check for empty push message
        if (TextUtils.isEmpty(msg))
            return;
        final int mIcon = R.drawable.ic_launcher;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent mPendingIntent = PendingIntent.getActivity(ct, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ct);
        final Uri uriAlarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + ct.getPackageName() + "/raw/notification");

        showSmallNotification(mBuilder, mIcon, title, msg, mPendingIntent, uriAlarmSound);

    }
    private void showSmallNotification(Builder mBuilder, int mIcon, String title, String msg,
                                       PendingIntent mPendingIntent, Uri uriAlarmSound) {
        NotificationCompat.InboxStyle mInboxStyle = new NotificationCompat.InboxStyle();

        mInboxStyle.addLine(msg);

        NotificationManager mNotiManager = (NotificationManager)ct.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification mNotification;
        mNotification = mBuilder.setSmallIcon(mIcon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(mPendingIntent)
                .setSound(uriAlarmSound)
                .setStyle(mInboxStyle)
                .setSmallIcon(mIcon)
                .setLargeIcon(BitmapFactory.decodeResource(ct.getResources(), mIcon))
                .setContentText(msg)
                .build();
        mNotification.ledARGB = 0xFF00ff00;
        mNotification.flags = Notification.FLAG_SHOW_LIGHTS;
        mNotification.ledOnMS = 1000;
        mNotification.ledOffMS = 1000;

        mNotiManager.notify(Constants.NOTIFICATION_ID, mNotification);
    }

    public static void clearNotifications() {
        NotificationManager mNotiManager = (NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotiManager.cancelAll();
    }
}
