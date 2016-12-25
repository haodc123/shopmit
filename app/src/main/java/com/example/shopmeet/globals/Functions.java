package com.example.shopmeet.globals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.shopmeet.R;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Functions {

	public static void toastString(String msg, Context context) {
		Toast mToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		mToast.show();
	}
	public static void toastInt(int msg, Context context) {
		Toast mToast = Toast.makeText(context, String.valueOf(msg), Toast.LENGTH_LONG);
		mToast.show();
	}

	public static long getFileLength(String mPath) {
	    File f = new File(mPath);
	    return f.length()/1024; // in KB
	}
	public static String getFileName(String mPath) {
		File f = new File(mPath);
		return f.getName();
	}
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
		    String[] proj = {MediaStore.Images.Media.DATA};
		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
		    if (cursor == null)
		    	return ""; // Not media file
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		} finally {
		    if (cursor != null) {
		    	cursor.close();
		    }
		}
	}

	public static boolean isActivityRunning(Context mcontext,
											Class<?> activityClass) {
		ActivityManager activityManager = (ActivityManager) mcontext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = activityManager
				.getRunningTasks(Integer.MAX_VALUE);

		for (ActivityManager.RunningTaskInfo task : tasks) {
			if (activityClass.getCanonicalName().equalsIgnoreCase(
					task.baseActivity.getClassName()))
				return true;
		}

		return false;
	}
	public static boolean isAppIsInBackground(Context context) {
		boolean isInBackground = true;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
			List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
				if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					for (String activeProcess : processInfo.pkgList) {
						if (activeProcess.equals(context.getPackageName())) {
							isInBackground = false;
						}
					}
				}
			}
		} else {
			List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			ComponentName componentInfo = taskInfo.get(0).topActivity;
			if (componentInfo.getPackageName().equals(context.getPackageName())) {
				isInBackground = false;
			}
		}
		return isInBackground;
	}

	public static void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size=1024;
		try
		{
			byte[] bytes=new byte[buffer_size];
			for(;;)
			{
				int count=is.read(bytes, 0, buffer_size);
				if(count==-1)
					break;
				os.write(bytes, 0, count);
			}
		}
		catch(Exception ex){}
	}

	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}
	public static int pxToDp(int px) {
		return (int) (px / Resources.getSystem().getDisplayMetrics().density);
	}

	public static String getPeriod(String sDate, String aFormat) {
		try {
			Date date = new SimpleDateFormat(aFormat, Locale.ENGLISH).parse(sDate);
			long milliDate = date.getTime();
			Calendar milliseconds = Calendar.getInstance();
			milliseconds.setTimeInMillis(milliDate);
			int yearDate = milliseconds.get(milliseconds.YEAR);
			int weekDate = milliseconds.get(milliseconds.WEEK_OF_YEAR);

			Calendar now = Calendar.getInstance();
			int yearNow = now.get(now.YEAR);
			int weekNow = now.get(now.WEEK_OF_YEAR);
			long milliNow = now.getTimeInMillis();
			if(now.get(Calendar.DATE) == milliseconds.get(Calendar.DATE) ) {
				return "today";
			} else if (yearDate == yearNow && weekDate == weekNow) {
				return "week";
			} else if ((milliNow - milliDate) <= (1000 * 60 * 60 * 24 * 10)) {
				return "10day";
			}
		} catch (ParseException e) {

		}
		return "";
	}
	public static String decodeBase64(String base64) {
		if (base64.equalsIgnoreCase(""))
			return "";
		byte[] data = Base64.decode(base64, Base64.DEFAULT);
		String text = "";
		try {
			text = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {

		}
		return text;
	}
	public static String getChatTimeStamp(String today, String dateStr, String aformat) {
		if (dateStr.equalsIgnoreCase("sending..."))
			return dateStr;
		String chat_time = "";

		if (!aformat.equalsIgnoreCase("")) {
			SimpleDateFormat format = new SimpleDateFormat(aformat);

			today = today.length() < 2 ? "0" + today : today;
			try {
				Date date = format.parse(dateStr);
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
				String dateToday = dateFormat.format(date);
				format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
				String date1 = format.format(date);
				chat_time = date1.toString();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else { // aformat = "" ~ timestamp
			aformat = Constants.DATETIME_FORMAT;
			dateStr = getDate(Long.parseLong(dateStr), aformat);
			SimpleDateFormat format = new SimpleDateFormat(aformat);

			today = today.length() < 2 ? "0" + today : today;
			try {
				Date date = format.parse(dateStr);
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
				String dateToday = dateFormat.format(date);
				format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
				String date1 = format.format(date);
				chat_time = date1.toString();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return chat_time;
	}
	private static String getDate(long timeStamp, String aformat){

		try{
			DateFormat sdf = new SimpleDateFormat(aformat);
			Date netDate = (new Date(timeStamp));
			return sdf.format(netDate);
		}
		catch(Exception ex){
			return "unknown time";
		}
	}

	/**
	 * Hiển thị phần joindate của group, staff theo kiểu:
	 * Nếu cách thời điểm hiện tại quá 1 năm, hiển thị ngày tháng năm như aformat
	 * Nếu cách thời điểm hiện tại không quá 1 năm, hiển thị số tháng cách đây
	 * Nếu cách thời điểm hiện tại không quá 1 tháng, hiển thị số ngày cách đây
	 * Nếu cách thời điểm hiện tại không quá 1 ngày, hiển thị số "today"
	 * @param today ngày hiện tại
	 * @param dateStr ngày joindate
	 * @param aformat format của today và dateStr
	 */
	public static String getFriendlyDateCreated(String today, String dateStr, String aformat) {
		DateFormat formatter = new SimpleDateFormat(aformat);
		Long sDate = 0L;
		Long sToday = 0L;
		try {
			Date date = (Date) formatter.parse(dateStr);
			sDate = date.getTime();
			Date todayDate = (Date) formatter.parse(today);
			sToday = todayDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Long distance = (sToday - sDate)/1000; // number of seconds
		if (distance > (60*60*24*365)) { // more than year
			try {
				DateFormat format = new SimpleDateFormat(aformat);
				Date date = format.parse(dateStr);
				format = new SimpleDateFormat("yyyy-MM-dd");
				return format.format(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (distance < (60*60*24*365) && distance > (60*60*24*30)) {
			return "about "+String.valueOf(distance/(60*60*24*30))+" months ago";
		} else if (distance < (60*60*24*30) && distance > (60*60*24)) {
			return "about "+String.valueOf(distance/(60*60*24))+" days ago";
		} else {
			return "today";
		}
		return null;
	}
	public static String getResString(Context ct, int resID) {
		return ct.getResources().getString(resID);
	}
	public static void playSound(Context ct, Uri uriAlarmSound, Ringtone mRingtone) {
		try {
			if (mRingtone == null)
				mRingtone = RingtoneManager.getRingtone(ct, uriAlarmSound);
			mRingtone.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void stopSound(Ringtone mRingtone) {
		try {
			if (mRingtone != null)
				mRingtone.stop();
			else
				return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void findDataType(Intent intent, Uri filename) {
		// TODO Auto-generated method stub
		if (filename.toString().contains(".doc") || filename.toString().contains(".docx")) {
			// Word document
			intent.setDataAndType(filename, "application/msword");
		} else if(filename.toString().contains(".pdf")) {
			// PDF file
			intent.setDataAndType(filename, "application/pdf");
		} else if(filename.toString().contains(".ppt") || filename.toString().contains(".pptx")) {
			// Powerpoint file
			intent.setDataAndType(filename, "application/vnd.ms-powerpoint");
		} else if(filename.toString().contains(".xls") || filename.toString().contains(".xlsx")) {
			// Excel file
			intent.setDataAndType(filename, "application/vnd.ms-excel");
		} else if(filename.toString().contains(".zip") || filename.toString().contains(".rar")) {
			// WAV audio file
			intent.setDataAndType(filename, "application/x-wav");
		} else if(filename.toString().contains(".rtf")) {
			// RTF file
			intent.setDataAndType(filename, "application/rtf");
		} else if(filename.toString().contains(".wav") || filename.toString().contains(".mp3")) {
			// WAV audio file
			intent.setDataAndType(filename, "audio/x-wav");
		} else if(filename.toString().contains(".gif")) {
			// GIF file
			intent.setDataAndType(filename, "image/gif");
		} else if(filename.toString().contains(".jpg") || filename.toString().contains(".jpeg") || filename.toString().contains(".png")) {
			// JPG file
			intent.setDataAndType(filename, "image/jpeg");
		} else if(filename.toString().contains(".txt")) {
			// Text file
			intent.setDataAndType(filename, "text/plain");
		} else if(filename.toString().contains(".3gp") || filename.toString().contains(".mpg") || filename.toString().contains(".mpeg") || filename.toString().contains(".mpe") || filename.toString().contains(".mp4") || filename.toString().contains(".avi")) {
			// Video files
			intent.setDataAndType(filename, "video/*");
		} else {
			//if you want you can also define the intent type for any other file

			//additionally use else clause below, to manage other unknown extensions
			//in this case, Android will show all applications installed on the device
			//so you can choose which application to use
			intent.setDataAndType(filename, "*/*");
		}
	}

	public static void setStatusBarColor(Context ct, int color) {
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = ((Activity) ct).getWindow();
			// clear FLAG_TRANSLUCENT_STATUS flag:
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			// finally change the color
			window.setStatusBarColor(ct.getResources().getColor(color));
		}*/
	}
	// Check Network (not Internet)
	public static synchronized boolean hasConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		NetworkInfo wifiNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}
		@SuppressWarnings("deprecation")
		NetworkInfo mobileNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}
		return false;
	}
	public static String getDeviceID(Context context) {
    	//final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String androidId;
        //tmDevice = "" + tm.getDeviceId();
        //tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        //UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        //return deviceUuid.toString();
        return androidId;
    }

	public static Bitmap getBitmapFromPath(String path, int inSampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(path, options);
	}
	public static String getCleanEmptyString(String s) {
		if (s.equalsIgnoreCase("null") || s == null)
			return "";
		return s;
	}

	public static void setActionBarHeight(int px, Context ct) {
		Window window = ((Activity)ct).getWindow();
		View v = window.getDecorView();
		int actionBarId = ct.getResources().getIdentifier("action_bar", "id", "android");
		ViewGroup actionBarView = (ViewGroup) v.findViewById(actionBarId);
		try {
			Field f = actionBarView.getClass().getSuperclass().getDeclaredField("mContentHeight");
			f.setAccessible(true);
			f.set(actionBarView, px);
		} catch (NoSuchFieldException e) {

		} catch (IllegalAccessException e) {

		}
	}
}
