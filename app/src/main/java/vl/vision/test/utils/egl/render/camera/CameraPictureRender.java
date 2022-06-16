package vl.vision.test.utils.egl.render.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;

import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.egl.base.egl.BaseIEglRenderInterface;
import vl.vision.test.utils.egl.render.PictureRender;
import vl.vision.test.utils.egl.render.ShaderProgram;


/**
 * camera + picture 组合
 * opengl 渲染
 * Created by hanqq on 2022/3/14
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class CameraPictureRender extends ShaderProgram {
    private String TAG = CameraPictureRender.this.getClass().getSimpleName();

    private CameraEncodeRender cameraEncodeRender;
    private PictureRender pictureRender;
    private SurfaceTexture.OnFrameAvailableListener mListener;

    public CameraPictureRender(Context context) {
        super(context);
        mIsClear = false;
    }

    public void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onSurfaceCreated() {
        cameraEncodeRender = new CameraEncodeRender(mContext);
        cameraEncodeRender.onSurfaceCreated();
        setProperties(cameraEncodeRender);

        pictureRender = new PictureRender(mContext);
        pictureRender.onSurfaceCreated();
        setProperties(pictureRender);

        if (mListener != null) {
            cameraEncodeRender.setOnFrameAvailableListener(mListener);
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        LogUtils.d(TAG, "onSurfaceChanged , width : " + width + " height: " + height);
        if (cameraEncodeRender != null) {
            cameraEncodeRender.onSurfaceChanged(width, height);
        }
        if (pictureRender != null) {
            pictureRender.onSurfaceChanged(width, height);
        }
    }

    @Override
    public void onViewPort() {
        if (cameraEncodeRender != null) {
            cameraEncodeRender.onViewPort();
        }
        if (pictureRender != null) {
            pictureRender.onViewPort();
        }
    }

    @Override
    public void onEncodeViewPort(int width, int height) {
        if (cameraEncodeRender != null) {
            cameraEncodeRender.onEncodeViewPort(width, height);
        }
        if (pictureRender != null) {
            pictureRender.onEncodeViewPort(width, height);
        }
    }

    @Override
    public void onViewChange() {
        if (cameraEncodeRender != null) {
            cameraEncodeRender.onViewChange();
        }
        if (pictureRender != null) {
            pictureRender.onViewChange();
        }
    }

    @Override
    public void onEncodeViewChange(int width, int height) {
        if (cameraEncodeRender != null) {
            cameraEncodeRender.onEncodeViewChange(width, height);
        }
        if (pictureRender != null) {
            pictureRender.onEncodeViewChange(width, height);
        }
    }

    @Override
    public void onEncodeDrawFrame() {
        if (cameraEncodeRender != null) {
            cameraEncodeRender.onEncodeDrawFrame();
        }
        if (pictureRender != null) {
            pictureRender.onEncodeDrawFrame();
        }
    }

    @Override
    public void onDrawFrame() {
        if (cameraEncodeRender != null) {
            cameraEncodeRender.onDrawFrame();
        }
        if (pictureRender != null) {
            pictureRender.onDrawFrame();
        }
    }

    @Override
    public void onSurfaceDestroy() {
        if (cameraEncodeRender != null) {
            cameraEncodeRender.onSurfaceDestroy();
            cameraEncodeRender = null;
        }
        if (pictureRender != null) {
            pictureRender.onSurfaceDestroy();
            pictureRender = null;
        }
        mContext = null;
    }

    private void setProperties(BaseIEglRenderInterface render) {
        if (render != null) {
            render.setGlClear(mIsClear);
            render.setScaleMode(mScaleMode);
            render.setAngle(mAngle);
            render.setZoomScale(mZoomPercent);
        }
    }
}
