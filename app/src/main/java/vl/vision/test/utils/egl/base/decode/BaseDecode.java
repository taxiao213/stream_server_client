package vl.vision.test.utils.egl.base.decode;

import android.view.Surface;

import java.io.File;


/**
 * 打开camera id 1 ，通过 hdmi 发送 content
 * 解码 h264 文件会有问题
 * Created by hanqq on 2020/7/15
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public abstract class BaseDecode implements BaseIDecodeInterface {
    public static final String TAG = BaseDecode.class.getSimpleName();

    public Surface mSurface;
    public File mFile;
    public int mWidth;
    public int mHeight;
    public int mFps;

    @Override
    public void onFrame(byte[] bytes) {

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        onRelease();
    }
}
