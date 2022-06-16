package vl.vision.test.utils.egl.view;

import android.content.Context;
import android.opengl.EGLContext;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import vl.vision.test.utils.egl.base.egl.BaseEglSurfaceView;
import vl.vision.test.utils.egl.base.egl.BaseIEglCallBackInterface;
import vl.vision.test.utils.egl.base.egl.EglCore;


/**
 * surface 渲染
 * Created by hanqq on 2022/3/22
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class CameraEncodeSurfaceView extends BaseEglSurfaceView {
    private EglCore mEglCore;

    public CameraEncodeSurfaceView(Context context) {
        this(context, null);
    }

    public CameraEncodeSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraEncodeSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBaseIEglCallBackInterface(new BaseIEglCallBackInterface() {
            @Override
            public void onEglCreate(EglCore eglCore) {
                mEglCore = eglCore;
                mWindowSurface = EglCore.createEglSurface(mEglCore, mSurface);
                EglCore.makeCurrent(mEglCore, mWindowSurface);
            }

            @Override
            public void onEglChange(EGLContext eglContext, int textureID) {


            }

            @Override
            public void onDrawFrame() {
                doFrame();
                EglCore.swapBuffers(mEglCore, mWindowSurface);
                if (mEncodeSubscribe != null) {
                    mEncodeSubscribe.doFrame(CameraEncodeSurfaceView.this, mEglCore);
                }
                EglCore.makeCurrent(mEglCore, mWindowSurface);
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        if (mEncodeSubscribe != null && mExternalRender) {
            mEncodeSubscribe.onCreate();
            mEncodeSubscribe.onStart();
        }
    }
}
