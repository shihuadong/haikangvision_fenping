
package com.monitor;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * @author andy.xu @ This class used to Listener net state
 * @version 0.0.1
 **/
public class NetWorkMonitor {

    private final static String NET_WIFI = "WIFI";

    private final static String NET_MOBILE = "MOBILE";

    /**
     * 判断网络是否是WIFI
     **/
    public static boolean isWifiNet(final Context context) {

        if (null == context)
            return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (null == netInfo)
            return false;

        if (netInfo.isAvailable()) {
            if (NET_WIFI.equalsIgnoreCase(netInfo.getTypeName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断网络是否是Mobile
     **/
    public static boolean isMobileNet(final Context context) {

        if (null == context)
            return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (null == netInfo)
            return false;

        if (netInfo.isAvailable()) {
            if (NET_MOBILE.equalsIgnoreCase(netInfo.getTypeName())) {
                return true;
            }
        }

        return false;
    }

    // add andy.xu 2011/11/3
    public static String getIp(final Context context) {

        if (null == context)
            return null;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (null == netInfo)
            return null;

        if (netInfo.isAvailable()) {
            if (NET_WIFI.equalsIgnoreCase(netInfo.getTypeName())) {

                return getIpOfWifi(context);

            } else if (NET_MOBILE.equalsIgnoreCase(netInfo.getTypeName())) {

                return getIpOfMobile(context);
            }
        }

        return null;
    }

    // 获取WIFI连接时的IP地址
    private static String getIpOfWifi(final Context context) {

        if (null == context)
            return null;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (null == wifiManager)
            return null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null == wifiInfo)
            return null;

        return intToIp(wifiInfo.getIpAddress());
    }

    // 对Ip地址进行解析
    private static String intToIp(int i) {

        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
                + (i >> 24 & 0xFF);
    }

    // 获取GPRS连接时的IP地址
    private static String getIpOfMobile(final Context context) {
        if (null == context)
            return null;

        try {
            for (Enumeration<NetworkInterface> networkList = NetworkInterface
                    .getNetworkInterfaces(); networkList.hasMoreElements();) {
                NetworkInterface netInterface = networkList.nextElement();
                if (null == netInterface)
                    continue;

                for (Enumeration<InetAddress> inetList = netInterface.getInetAddresses(); inetList
                        .hasMoreElements();) {
                    InetAddress address = inetList.nextElement();
                    if (null == address)
                        continue;
                    if (!address.isLoopbackAddress())
                        return address.getHostAddress();
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 使用WIFI获取MAC地址

    public static final String getMacAddress(final Context context) {

        if (null == context)
            return null;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (null == wifiManager)
            return null;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null == wifiInfo)
            return null;

        return wifiInfo.getMacAddress();
    }

    // 获取 IMEI 手机唯一标示
    public static final String getIMEIAddress(final Context context) {

        if (null == context)
            return null;

        TelephonyManager telphoneManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (null == telphoneManager)
            return null;

        return telphoneManager.getDeviceId();
    }

    // 获取手机的电话号码
    public static final String getTelphone(final Context context) {
        String resTelphone = null;
        if (null == context)
            return null;

        TelephonyManager telphoneManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (null == telphoneManager)
            return null;
        resTelphone = telphoneManager.getLine1Number();
        if(!TextUtils.isEmpty(resTelphone)){
            if(resTelphone.contains("+86")){
                resTelphone = resTelphone.replace("+86", "");
            }
            if("86".equals(resTelphone.substring(0,2))){
                resTelphone = resTelphone.substring(2);
            }
        }
        return resTelphone;
    }

    public static boolean isConnect(final Context context) {

        boolean bFlag = false;
        if (null == context)
            return bFlag;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivityManager.getActiveNetworkInfo()) {
            bFlag = connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        return bFlag;
    }

    /**
     * */
    public class NetConnect {

        @Override
        protected void finalize() throws Throwable {
            super.finalize();

            mContext = null;
        }

        private Context mContext = null;

        public NetConnect(Context context) {
            mContext = context;
        }

        public boolean isConnect() {

            boolean bFlag = false;
            if (null == mContext)
                return bFlag;

            ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null != connectivityManager.getActiveNetworkInfo()) {
                bFlag = connectivityManager.getActiveNetworkInfo().isAvailable();
            }

            return bFlag;
        }

        public void netSettingOper() {
            showSettingDlg(mContext);
        }
    }

    /**
     * */
    public class NetReceiver extends BroadcastReceiver {

        private Context mContext = null;

        private boolean mIsConnect = false;

        @Override
        public void onReceive(Context context, Intent intent) {

            synchronized (NetReceiver.class) {
                mContext = context;
            }

            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!gprs.isConnected() && !wifi.isConnected()) {

                synchronized (NetReceiver.class) {
                    mIsConnect = false;
                }
            } else {

                synchronized (NetReceiver.class) {
                    mIsConnect = false;
                }
            }
        }

        public synchronized boolean isConnect() {

            synchronized (NetReceiver.class) {
                return mIsConnect;
            }
        }

        public synchronized void netSettingOper() {
            showSettingDlg(mContext);
        }
    }

    public static void showSettingDlg(final Context context) {

        if (null == context)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.net_title);
        builder.setMessage(R.string.net_content);
        builder.setPositiveButton(R.string.gprs_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        builder.setNeutralButton(R.string.wifi_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.cancel_string, null);
        builder.create().show();
    }
}
