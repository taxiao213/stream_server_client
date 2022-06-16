package vl.vision.test.utils.egl.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.egl.base.egl.BaseEglRender;
import vl.vision.test.utils.egl.utils.ScaleMode;
import vl.vision.test.utils.egl.utils.ShaderUtils;


/**
 * Created by hanqq on 2022/3/14
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public abstract class ShaderProgram extends BaseEglRender {
    private String TAG = this.getClass().getSimpleName();
    public int program;
    public Context mContext;
    public float[] viewProjectionMatrix = new float[16];
    public float[] viewMatrix = new float[16];
    public float[] projectionMatrix = new float[16];
    public int mWidth;
    public int mHeight;
    public int mAngle = 0;
    private int SIZEOF_FLOAT = 4;
    public float mScaleX;
    public float mScaleY;
    public float mZoomPercent = 0f;
    private FloatBuffer mZoomFloatBuffer;
    public boolean mIsClear = true;
    public ScaleMode mScaleMode = ScaleMode.CENTER_INSIDE;

    public ShaderProgram(Context context) {
        mContext = context;
    }

    public void init(int vetexShaderResourceId, int fragmentShaderResourceId) {
        String vertexSource = ShaderUtils.readRawTxt(mContext, vetexShaderResourceId);
        String fragmentSource = ShaderUtils.readRawTxt(mContext, fragmentShaderResourceId);
        program = ShaderUtils.createProgram(vertexSource, fragmentSource);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
        onViewChange();
    }

    @Override
    public void onSurfaceDestroy() {
        program = -1;
        viewProjectionMatrix = null;
        viewMatrix = null;
        projectionMatrix = null;
    }

    @Override
    public void onViewPort() {
        onEncodeViewPort(mWidth, mHeight);
    }

    @Override
    public void onEncodeViewPort(int width, int height) {
        LogUtils.d(TAG, "onViewPort width: " + width + " height: " + height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onViewChange() {
        onEncodeViewChange(mWidth, mHeight);
    }

    @Override
    public void onEncodeViewChange(int width, int height) {
        if (width == 0 || height == 0) return;
        LogUtils.d(TAG, "onSurfaceChanged , width: " + width + " height: " + height);
        Matrix.setIdentityM(projectionMatrix, 0);
        // 创建正交投影矩阵
        Matrix.orthoM(projectionMatrix, 0, 0, width, 0, height, -1, 1);
        setRotation(width, height);
        LogUtils.d(TAG, "onSurfaceChanged , width: " + width + " height: " + height + " mScaleX: " + mScaleX + " mScaleY: " + mScaleY);
    }

    @Override
    public void onEncodeDrawFrame() {
        onDrawFrame();
    }

    public void useProgram() {
        GLES20.glUseProgram(program);
    }

    @Override
    public void setAngle(int angle) {
        mAngle = angle;
    }

    public void setRotation(int width, int height) {
        // 缩放系数
        if (mScaleMode == ScaleMode.CENTER_INSIDE) {
            if (width < height) {
                mScaleX = width / 2f;
                mScaleY = mScaleX * 9.0f / 16;
            } else {
                mScaleY = height / 2f;
                mScaleX = mScaleY * 16.0f / 9;
            }
        } else if (mScaleMode == ScaleMode.FIT_XY) {
            if (width > height) {
                mScaleX = width / 2f;
                mScaleY = mScaleX * 9.0f / 16;
            } else {
                mScaleY = height / 2f;
                mScaleX = mScaleY * 16.0f / 9;
            }
        }

        while (mAngle >= 360.0f) {
            mAngle -= 360.0f;
        }
        while (mAngle <= -360.0f) {
            mAngle += 360.0f;
        }
        // 初始化
        Matrix.setIdentityM(viewMatrix, 0);
        // 移动
        Matrix.translateM(viewMatrix, 0, width / 2f, height / 2f, 0.0f);
        // 旋转
        Matrix.rotateM(viewMatrix, 0, mAngle, 0.0f, 0.0f, 1.0f);
        // 缩放
        Matrix.scaleM(viewMatrix, 0, mScaleX, mScaleY, 1.0f);
    }

    /**
     * 范围在0-100
     *
     * @param zoomPercent
     */
    @Override
    public void setZoomScale(float zoomPercent) {
        mZoomPercent = zoomPercent;
    }

    public FloatBuffer getZoomFloatBuffer(FloatBuffer vertexBuffer) {
        float zoomFactor = 1.0f - (mZoomPercent / 100.0f);
        if (zoomFactor < 0.0f || zoomFactor > 1.0f) {
            throw new RuntimeException("invalid scale " + zoomFactor);
        }
        int count = vertexBuffer.capacity();
        if (mZoomFloatBuffer == null) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(count * SIZEOF_FLOAT);
            byteBuffer.order(ByteOrder.nativeOrder());
            mZoomFloatBuffer = byteBuffer.asFloatBuffer();
        }
        // Texture coordinates range from 0.0 to 1.0, inclusive.  We do a simple scale
        // here, but we could get much fancier if we wanted to (say) zoom in and pan
        // around.
        for (int i = 0; i < count; i++) {
            float fl = vertexBuffer.get(i);
            fl = ((fl - 0.5f) * zoomFactor) + 0.5f;
            mZoomFloatBuffer.put(i, fl);
        }
        return mZoomFloatBuffer;
    }

    @Override
    public void setScaleMode(ScaleMode scaleMode) {
        mScaleMode = scaleMode;
    }

    @Override
    public void setGlClear(boolean isClear) {
        mIsClear = isClear;
    }

    // 深度测试
    public void GL_DEPTH_TEST() {
        if (mIsClear) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
    }

    public void GL_DEPTH_TEST_CLEAR() {
        if (mIsClear) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        }
    }

}
