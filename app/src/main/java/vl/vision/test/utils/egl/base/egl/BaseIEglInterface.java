package vl.vision.test.utils.egl.base.egl;

import android.opengl.EGLContext;
import android.view.Surface;

/**
 * 提供外部接口
 * Created by hanqq on 2022/3/17
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public interface BaseIEglInterface {
    // 外部传入
    EGLContext getEglContext();

    Surface getSurface();

    int getRenderMode();

    // 帧数
    int getFps();

    // 初始化 egl flags
    int getFlags();

    // 是否在外部渲染，encode 时使用，默认false
    boolean externalRender();

    BaseIEglRenderInterface getRender();

}
