package com.nainghtweoo.pocketmic;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by NaingHtweOo on 10/10/16.
 */
public class Utility {
    public static String getAppVersion() {
        String version = "v- ";
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        return version.concat(versionName);
    }

//    public static void showInfoDialog(Context context) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage("");
//        builder.setCancelable(true);
//        AlertDialog dialog = builder.create();
//        // display dialog
//        dialog.show();
//    }

    public static void showInfoDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Please connect any speaker!!");
        View view = LayoutInflater.from(context).inflate(R.layout.info_layout, new FrameLayout(context), false);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
}
