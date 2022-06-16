package vl.vision.test.utils.egl.utils;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.text.TextUtils;

import vl.vision.test.utils.LogUtils;


/**
 * mediacodec 工具类
 * Created by hanqq on 2022/3/22
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class MediacodecUtils {

    public static String TAG = MediacodecUtils.class.getSimpleName();

    public static MediaCodecInfo selectCodec(String mimeType) {
        if (!TextUtils.isEmpty(mimeType)) {
            int numCodecs = MediaCodecList.getCodecCount();
            for (int i = 0; i < numCodecs; i++) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                if (!codecInfo.isEncoder()) {
                    continue;
                }
                String[] types = codecInfo.getSupportedTypes();
                for (int j = 0; j < types.length; j++) {
                    if (types[j].equalsIgnoreCase(mimeType)) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    //查询支持的输入格式
    public static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        if (codecInfo == null) {
            return -1;
        }
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        LogUtils.d(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return 0;
    }

    public static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar://对应Camera预览格式I420(YV21/YUV420P)
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar: //对应Camera预览格式NV12
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar://对应Camera预览格式NV21
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar: ////对应Camera预览格式YV12
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    public static void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    // 计算当前时间
    public static long computePresentationTime(int frameIndex, int fps) {
        return 132 + frameIndex * 1000000 / fps;
//        return System.nanoTime();
    }

    public static long computePresentationTime(long startTime) {
        return (System.nanoTime() - startTime) / 1000;
    }
}
