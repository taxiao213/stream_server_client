package vl.vision.test.utils.egl.base.egl;


import vl.vision.test.utils.egl.utils.ScaleMode;

/**
 * Created by hanqq on 2021/12/10
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public interface BaseIEglRenderInterface {

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onSurfaceDestroy();

    void onDrawFrame();

    // 编码时使用 绘制
    void onEncodeDrawFrame();

    // 编码时使用 外部传入 width height
    void onEncodeViewPort(int width, int height);

    void onViewPort();

    // 编码时使用 view 改变
    void onEncodeViewChange(int width, int height);

    void onViewChange();

    // 获得纹理
    int getTextureID();

    // 设置纹理
    void setTextureID(int id);

    // 设置旋转角度
    void setAngle(int angle);

    // zoom 缩放
    void setZoomScale(float zoomPercent);

    // 缩放模式
    void setScaleMode(ScaleMode scaleMode);

    // 是否清除
    void setGlClear(boolean isClear);

}
