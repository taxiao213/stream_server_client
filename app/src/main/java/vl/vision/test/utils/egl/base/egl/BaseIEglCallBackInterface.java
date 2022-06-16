package vl.vision.test.utils.egl.base.egl;

import android.opengl.EGLContext;

/**
 * 提供外部接口
 * Created by hanqq on 2022/3/17
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public interface BaseIEglCallBackInterface {
    void onEglCreate(EglCore eglCore);

    void onEglChange(EGLContext eglContext, int textureID);

    void onDrawFrame();

}
