package vl.vision.test.utils.egl.base.decode;

/**
 * Created by hanqq on 2022/3/8
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public interface BaseIDecodeInterface {

    void onCreate();

    void onStart();

    void onStop();

    void onFrame(byte[] bytes);

    void onRelease();
}
