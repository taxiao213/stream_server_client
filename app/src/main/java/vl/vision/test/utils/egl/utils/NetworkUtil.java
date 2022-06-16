package vl.vision.test.utils.egl.utils;

/**
 * Created by hanqq on 2022/3/25
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class NetworkUtil {
    public static String TAG = NetworkUtil.class.getSimpleName();

    /**
     * 得到当前网络类型
     *
     * @param context 上下文
     * @return 返回当前网络
     */
//    public static Network getCurrentNetwork(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        if (cm == null) {
//            LogUtils.d(TAG, "ConnectivityManager is null");
//            return Network.NT_NONE;
//        }
//
//        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
//
//        if (networkInfo != null) {
//            int type = networkInfo.getType();
//            int subtype = networkInfo.getSubtype();
//            return isConnectionFast(type, subtype);
//        } else {
//            Log.w(TAG, "networkInfo is null");
//            return Network.NT_NONE;
//        }
//    }
//
//
//    private static Network isConnectionFast(int type, int subType) {
//        if (type == ConnectivityManager.TYPE_WIFI) {
//            return Network.NT_WIFI;
//        } else if (type == ConnectivityManager.TYPE_MOBILE) {
//            switch (subType) {
//                case TelephonyManager.NETWORK_TYPE_1xRTT:
//                    return Network.NT_2G; // ~ 50-100 kbps
//                case TelephonyManager.NETWORK_TYPE_CDMA:
//                    return Network.NT_2G; // ~ 14-64 kbps
//                case TelephonyManager.NETWORK_TYPE_EDGE:
//                    return Network.NT_2G; // ~ 50-100 kbps
//                case TelephonyManager.NETWORK_TYPE_GPRS:
//                    return Network.NT_2G; // ~ 100 kbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_0:
//                    return Network.NT_3G; // ~25 kbps
//                case TelephonyManager.NETWORK_TYPE_LTE:
//                    return Network.NT_4G; // ~ 400-1000 kbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_A:
//                    return Network.NT_3G; // ~ 600-1400 kbps
//                case TelephonyManager.NETWORK_TYPE_HSDPA:
//                    return Network.NT_3G; // ~ 2-14 Mbps
//                case TelephonyManager.NETWORK_TYPE_HSPA:
//                    return Network.NT_3G; // ~ 700-1700 kbps
//                case TelephonyManager.NETWORK_TYPE_HSUPA:
//                    return Network.NT_3G; // ~ 1-23 Mbps
//                case TelephonyManager.NETWORK_TYPE_UMTS:
//                    return Network.NT_3G; // ~ 400-7000 kbps
//                case TelephonyManager.NETWORK_TYPE_EHRPD:
//                    return Network.NT_3G; // ~ 1-2 Mbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_B:
//                    return Network.NT_3G; // ~ 5 Mbps
//                case TelephonyManager.NETWORK_TYPE_HSPAP:
//                    return Network.NT_3G; // ~ 10-20 Mbps
//                case TelephonyManager.NETWORK_TYPE_IDEN:
//                    return Network.NT_2G; // ~ 10+ Mbps
//                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
//                default:
//                    return Network.NT_NONE;
//            }
//        } else {
//            return Network.NT_NONE;
//        }
//    }
}
