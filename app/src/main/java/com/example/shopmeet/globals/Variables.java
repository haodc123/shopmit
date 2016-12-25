package com.example.shopmeet.globals;

import com.example.shopmeet.utils.MyDiskCache;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Variables {
	public static MyDiskCache mDCache = null;
	public static String curFrg = "";
	public static String prevFrg = "";

	public static String deviceID = "";
	public static String androidVersion = "";
	public static String deviceName = "";

	// User info
	public static String userToken = "";
	public static String userID = "";
	public static String userAvatar = "";
	public static String userName = "";
	public static String userFName = "";
	public static String userLName = "";
	public static String userEmail = "";
	public static String userTel = "";
	public static String userAddress = "";
	public static String userJoinDate = "";

	// Shop
	public static int curShopID = 0;
	public static String curShopName = "";

	public static int isAlreadyAlertConnection = 0;
	public static int networkState = 0;

	public static int numConversationUnSeen = 0;

}
