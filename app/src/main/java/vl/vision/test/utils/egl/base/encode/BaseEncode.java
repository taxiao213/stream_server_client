package vl.vision.test.utils.egl.base.encode;

import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.view.Surface;

import vl.vision.test.utils.egl.base.egl.BaseIEglInterface;
import vl.vision.test.utils.egl.base.egl.BaseIEglRenderInterface;
import vl.vision.test.utils.egl.base.egl.EglCore;
import vl.vision.test.utils.egl.utils.BitRate;


/**
 * encode 基类
 * Created by hanqq on 2022/3/22
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public abstract class BaseEncode implements BaseIEncodeInterface, BaseIEglInterface {
    public Surface mSurface;
    public EGLSurface mEglSurface;
    public int mFps;
    public Enum<BitRate> mBitRate = BitRate.LOW_BIT_RATE;
    public int mWidth;
    public int mHeight;

    @Override
    public EGLContext getEglContext() {
        return null;
    }

    @Override
    public Surface getSurface() {
        return null;
    }

    @Override
    public Surface getInputSurface() {
        return mSurface;
    }

    @Override
    public int getRenderMode() {
        return 0;
    }

    @Override
    public int getFps() {
        return mFps;
    }

    @Override
    public int getFlags() {
        return 0;
    }

    @Override
    public boolean externalRender() {
        return false;
    }

    @Override
    public BaseIEglRenderInterface getRender() {
        return null;
    }

    @Override
    public void createEglSurface(EglCore eglCore) {
        if (mEglSurface == null) {
            mEglSurface = EglCore.createEglSurface(eglCore, mSurface);
        }
    }

    @Override
    public void makeCurrent(EglCore eglCore) {
        createEglSurface(eglCore);
        EglCore.makeCurrent(eglCore, mEglSurface);
    }

    @Override
    public void makeCurrent(EglCore eglCore, EGLSurface readSurface) {
        createEglSurface(eglCore);
        EglCore.makeCurrentReadFrom(eglCore, mEglSurface, readSurface);
    }

    @Override
    public void swapBuffers(EglCore eglCore) {
        EglCore.swapBuffers(eglCore, mEglSurface);
    }

    @Override
    public EGLSurface getEGLSurface() {
        return mEglSurface;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        onRelease();
    }
}
