package vl.vision.test.utils.egl.base.encode;

import android.opengl.EGLSurface;
import android.view.Surface;

import vl.vision.test.utils.egl.base.egl.EglCore;


/**
 * Created by hanqq on 2022/3/8
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public interface BaseIEncodeInterface {

    void onCreate();

    void onStart();

    void onStop();

    void onFrame(byte[] bytes);

    void onRelease();

    Surface getInputSurface();

    EGLSurface getEGLSurface();

    void createEglSurface(EglCore eglCore);

    void makeCurrent(EglCore eglCore);

    void makeCurrent(EglCore eglCore, EGLSurface readSurface);

    void swapBuffers(EglCore eglCore);

    int getWidth();

    int getHeight();

    int getFrameIndex();

    int getFps();
}
