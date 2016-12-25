package com.example.shopmeet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shopmeet.utils.NetworkUtil;

/**
 * Created by UserPC on 7/5/2016.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public interface NetworkStateListener {
        void onNetChange(int status);
    }

    private NetworkStateListener mOnNetworkStateListener;

    public void setNetworkStateListener(NetworkStateListener listener) {
        mOnNetworkStateListener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        int status = NetworkUtil.getConnectivityStatus(context);
        if (mOnNetworkStateListener != null)
            mOnNetworkStateListener.onNetChange(status);
    }

}