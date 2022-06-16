package vl.vision.test.utils.egl.base.egl;

import android.opengl.EGLContext;
import android.opengl.EGLSurface;


import java.lang.ref.WeakReference;

import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.egl.utils.EglConstant;


/**
 * Egl 线程
 * Created by hanqq on 2022/3/17
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class EglThread extends Thread {
    private String TAG = this.getClass().getName();

    private EglCore mEglCore;
    private Object mObject;
    private boolean mIsStart;// 控制帧数
    public boolean mIsCreate;// 创建
    public boolean mIsChange;// 改变
    private boolean mIsDraw;// 绘制
    private boolean mIsExit;// 退出
    private int mWidth;
    private int mHeight;
    private long createTime;
    private float intervalTime = 2.6f;// 间隔时间 1.5, 0.8 ,0.5
    private WeakReference<BaseIEglInterface> mWeakReference;
    private BaseIEglCallBackInterface mBaseIEglCallBackInterface;
    private EGLSurface eglSurface;

    public EglThread(BaseIEglInterface baseIEglInterface) {
        setName("opengl_thread");
        mWeakReference = new WeakReference<BaseIEglInterface>(baseIEglInterface);
    }

    @Override
    public void run() {
        mIsStart = false;
        mIsDraw = false;
        mIsExit = false;
        mObject = new Object();
        // android record 预览加实时编码,默认 EglCore.FLAG_RECORDABLE
        int flags = mWeakReference.get().getFlags();
        mEglCore = new EglCore(mWeakReference.get().getEglContext(), flags == 0 ? EglCore.FLAG_RECORDABLE : flags);
        if (mWeakReference.get().externalRender()) {
            if (mBaseIEglCallBackInterface != null)
                mBaseIEglCallBackInterface.onEglCreate(mEglCore);
        } else {
            eglSurface = mEglCore.createWindowSurface(mWeakReference.get().getSurface());
            mEglCore.makeCurrent(eglSurface);
        }
        try {
            while (true) {
                LogUtils.d(TAG, String.format("mIsStart: %b , mIsCreate: %b , mIsChange: %b, mIsDraw: %b  mIsExit: %b time: %d", mIsStart, mIsCreate, mIsChange, mIsDraw, mIsExit, System.currentTimeMillis()));
                createTime = System.currentTimeMillis();
                onCreate();

                onChange();

                onDraw();

                if (mIsExit) {
                    release();
                    break;
                }
                mIsStart = true;
                onStart();

                dormancy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dormancy() {
        long drawEndTime = System.currentTimeMillis();
        if (mWeakReference.get().getRenderMode() == EglConstant.RENDERMODE_CONTINUOUSLY) {
            try {
                if (drawEndTime - createTime > 0) {
                    Thread.sleep((int) (Math.round(1000f / mWeakReference.get().getFps() - (drawEndTime - createTime)) - intervalTime));
                    LogUtils.d(TAG, "onDraw, sleep time: " + (Math.round(1000f / mWeakReference.get().getFps() - (drawEndTime - createTime) - 0.5) + " intervalTime: " + intervalTime));
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.d(TAG, "Exception: " + e.getMessage());
            }
            LogUtils.d(TAG,
                    "dormancyTime: " + (System.currentTimeMillis() - createTime) + " intervalTime: " + intervalTime);
        }
    }

    // 控制 render 帧数
    private void onStart() {
        if (mIsStart) {
            LogUtils.d(TAG, "onStart: ");
            if (mWeakReference != null && mWeakReference.get() != null) {
                int mRenderMode = mWeakReference.get().getRenderMode();
                try {
                    switch (mRenderMode) {
                        case EglConstant.RENDERMODE_WHEN_DIRTY:
                            synchronized (mObject) {
                                mObject.wait();
                                LogUtils.d(TAG, "onStart mRenderMode: RENDERMODE_WHEN_DIRTY");
                            }
                            break;
                        case EglConstant.RENDERMODE_CONTINUOUSLY:
                            LogUtils.d(TAG, "onStart mRenderMode: RENDERMODE_CONTINUOUSLY");
                            break;
                    }
                } catch (Exception e) {
                    LogUtils.d(TAG, " onStart Exception" + e.getMessage());
                }
            }
        }
    }

    private void onCreate() {
        if (mIsCreate && mWeakReference != null && mWeakReference.get() != null && mWeakReference.get().getRender() != null) {
            LogUtils.d(TAG, "onCreate");
            mWeakReference.get().getRender().onSurfaceCreated();
            mIsCreate = false;
        }
    }

    private void onChange() {
        if (mIsChange && mWeakReference != null && mWeakReference.get() != null && mWeakReference.get().getRender() != null) {
            LogUtils.d(TAG, "onChange");
            mWeakReference.get().getRender().onSurfaceChanged(mWidth, mHeight);
            mIsChange = false;
            mIsDraw = true;
            if (mBaseIEglCallBackInterface != null)
                mBaseIEglCallBackInterface.onEglChange(mEglCore.getEglContext(), mWeakReference.get().getRender().getTextureID());
        }
    }

    private void onDraw() {
        if (mIsDraw && mWeakReference != null && mWeakReference.get() != null && mWeakReference.get().getRender() != null && mEglCore != null) {
            long drawStartTime = System.currentTimeMillis();
            LogUtils.d(TAG, "onDraw, drawStartTime: " + drawStartTime);
            if (mWeakReference.get().externalRender()) {
                if (mBaseIEglCallBackInterface != null)
                    mBaseIEglCallBackInterface.onDrawFrame();
            } else {
                mWeakReference.get().getRender().onDrawFrame();
                mEglCore.swapBuffers(eglSurface);
            }
        }
    }

    private void release() {
        LogUtils.d(TAG, "release");
        if (mEglCore != null) {
            mEglCore.destoryEgl();
            mEglCore = null;
            mObject = null;
            mWeakReference.clear();
            mWeakReference = null;
        }
    }

    public void onDestory() {
        mIsExit = true;
        requestRender();
    }

    // 渲染
    public void requestRender() {
        if (mObject != null) {
            LogUtils.d(TAG, "requestRender");
            synchronized (mObject) {
                mObject.notifyAll();
            }
        }
    }

    public EGLContext getInnerEGLContext() {
        if (mEglCore != null) {
            LogUtils.d(TAG, "EglContext: " + mEglCore.getEglContext());
            return mEglCore.getEglContext();
        }
        LogUtils.d(TAG, "EglContext null: ");
        return null;
    }

    public void setWidthAndHeight(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public void setOnEglContextCreate(BaseIEglCallBackInterface eglCallBackInterface) {
        this.mBaseIEglCallBackInterface = eglCallBackInterface;
    }

    public EglCore getEglCore() {
        return mEglCore;
    }
}
