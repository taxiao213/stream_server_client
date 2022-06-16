package vl.vision.test.utils.egl.base.egl;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import vl.vision.test.utils.egl.base.encode.EncodeSubscribe;
import vl.vision.test.utils.egl.utils.EglConstant;


/**
 * Created by hanqq on 2022/3/15
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class BaseEglSurfaceView extends SurfaceView implements BaseIEglInterface, SurfaceHolder.Callback {
    public static final String TAG = BaseEglSurfaceView.class.getSimpleName();

    private int mRenderMode = EglConstant.RENDERMODE_CONTINUOUSLY;
    public int mFps = 25;
    private int mFlags;
    public boolean mExternalRender;
    private EglThread mEglThread;
    public Surface mSurface;
    private EGLContext mEGLContext;
    private BaseIEglRenderInterface mRender;
    private BaseIEglCallBackInterface mBaseIEglCallBackInterface;
    public EncodeSubscribe mEncodeSubscribe;
    public EGLSurface mWindowSurface;

    public BaseEglSurfaceView(Context context) {
        this(context, null);
    }

    public BaseEglSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseEglSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mSurface == null) {
            mSurface = holder.getSurface();
        }
        mEglThread = new EglThread(this);
        mEglThread.setOnEglContextCreate(mBaseIEglCallBackInterface);
        mEglThread.mIsCreate = true;
        mEglThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mEglThread != null) {
            mEglThread.setWidthAndHeight(width, height);
            mEglThread.mIsChange = true;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEglThread != null) {
            mEglThread.onDestory();
            mEglThread = null;
            mSurface = null;
            mEGLContext = null;
        }
    }

    @Override
    public EGLContext getEglContext() {
        return mEGLContext;
    }

    @Override
    public Surface getSurface() {
        return mSurface;
    }

    @Override
    public int getRenderMode() {
        return mRenderMode;
    }

    @Override
    public int getFps() {
        return mFps;
    }

    @Override
    public int getFlags() {
        return mFlags;
    }

    @Override
    public boolean externalRender() {
        return mExternalRender;
    }

    @Override
    public BaseIEglRenderInterface getRender() {
        return mRender;
    }

    public BaseEglSurfaceView setRender(BaseIEglRenderInterface render) {
        this.mRender = render;
        return this;
    }

    public BaseEglSurfaceView setRenderMode(int renderMode) {
        this.mRenderMode = renderMode;
        return this;
    }

    public BaseEglSurfaceView setSurface(Surface surface) {
        this.mSurface = surface;
        return this;
    }

    public BaseEglSurfaceView setFps(int fps) {
        this.mFps = fps;
        return this;
    }

    public BaseEglSurfaceView setSurfaceAndEglContext(Surface surface, EGLContext eglContext) {
        this.mSurface = surface;
        this.mEGLContext = eglContext;
        return this;
    }

    public BaseEglSurfaceView setEGLContext(EGLContext eglContext) {
        this.mEGLContext = eglContext;
        return this;
    }

    public BaseEglSurfaceView setFlags(int mFlags) {
        this.mFlags = mFlags;
        return this;
    }

    public BaseEglSurfaceView setExternalRender(boolean mExternalRender) {
        this.mExternalRender = mExternalRender;
        return this;
    }

    public BaseEglSurfaceView registerEncode(EncodeSubscribe baseEncode) {
        this.mEncodeSubscribe = baseEncode;
        return this;
    }

    public EGLContext getInnerEGLContext() {
        if (mEglThread != null) {
            return mEglThread.getInnerEGLContext();
        }
        return null;
    }

    public int getRenderTextureId() {
        if (mRender != null) {
            return mRender.getTextureID();
        }
        return 0;
    }

    public void requestRender() {
        if (mEglThread != null) {
            mEglThread.requestRender();
        }
    }

    public void doFrame() {
        if (mRender != null) {
            mRender.onViewPort();
            mRender.onViewChange();
            mRender.onDrawFrame();
        }
    }

    public void doFrame(int width, int height) {
        if (mRender != null) {
            mRender.onEncodeViewPort(width, height);
            mRender.onEncodeViewChange(width, height);
            mRender.onEncodeDrawFrame();
        }
    }

    public void onSurfaceChanged(int width, int height) {
        if (mRender != null) {
            mRender.onSurfaceChanged(width, height);
        }
    }

    public void setBaseIEglCallBackInterface(BaseIEglCallBackInterface eglCallBackInterface) {
        this.mBaseIEglCallBackInterface = eglCallBackInterface;
    }

    public void onRelease() {
        try {
            if (mRender != null) {
                mRender.onSurfaceDestroy();
                mRender = null;
            }
            if (mEncodeSubscribe != null) {
                mEncodeSubscribe.onStop();
                mEncodeSubscribe = null;
            }
            if (mEglThread != null) {
                mEglThread.onDestory();
                mEglThread = null;
            }
        } catch (Exception e) {
            Log.d(TAG, " release: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
