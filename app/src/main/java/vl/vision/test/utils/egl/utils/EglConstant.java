package vl.vision.test.utils.egl.utils;

import android.graphics.Point;

import java.util.HashMap;

/**
 * Created by hanqq on 2022/6/11
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class EglConstant {
    public static final int RENDERMODE_WHEN_DIRTY = 0;
    public static final int RENDERMODE_CONTINUOUSLY = 1;

    public static HashMap<BitRate, Float> getBitRate() {
        HashMap<BitRate, Float> bitRateFloatHashMap = new HashMap<>();
        bitRateFloatHashMap.put(BitRate.VERY_LOW_BIT_RATE, 1.0f * 3 / 4);
        bitRateFloatHashMap.put(BitRate.LOW_BIT_RATE, 1.0f * 3 / 2);
        bitRateFloatHashMap.put(BitRate.MEDIUM_BIT_RATE, 1.0f * 3);
        bitRateFloatHashMap.put(BitRate.HIGH_BIT_RATE, 1.0f * 3 * 2);
        bitRateFloatHashMap.put(BitRate.VERY_HIGH_BIT_RATE, 1.0f * 3 * 4);
        return bitRateFloatHashMap;
    }

    public static final int CAMERA_RESOLUTION_1920 = 1920;
    public static final int CAMERA_RESOLUTION_1080 = 1080;
    public static final int CAMERA_RESOLUTION_1280 = 1280;
    public static final int CAMERA_RESOLUTION_720 = 720;
    public static final Point CAMERA_RESOLUTION_1080_POINT = new Point(CAMERA_RESOLUTION_1920, CAMERA_RESOLUTION_1080);
    public static final Point CAMERA_RESOLUTION_720_POINT = new Point(CAMERA_RESOLUTION_1280, CAMERA_RESOLUTION_720);

    // hdmi content 文件存储
    public static final String HDMI_CONTENT_FILE = "hdmi_content";


}
