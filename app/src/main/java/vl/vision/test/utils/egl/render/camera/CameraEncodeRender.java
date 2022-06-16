package vl.vision.test.utils.egl.render.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import vl.vision.test.R;
import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.egl.base.egl.BaseIEglRenderInterface;
import vl.vision.test.utils.egl.camera.CameraEncodeUtils;
import vl.vision.test.utils.egl.render.ShaderProgram;
import vl.vision.test.utils.egl.render.VertexFboModel;
import vl.vision.test.utils.egl.render.VertexModel;
import vl.vision.test.utils.egl.utils.EglConstant;


/**
 * camera 渲染 编码
 * VAO相当于是对很多个VBO的引用，把一些VBO组合在一起作为一个对象统一管理。
 * Created by hanqq on 2022/3/14
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class CameraEncodeRender extends ShaderProgram implements BaseIEglRenderInterface, SurfaceTexture.OnFrameAvailableListener {
    private String TAG = this.getClass().getSimpleName();
    // Uniform
    public static final String U_MATRIX = "u_Matrix";
    public static final String S_TEXTURE = "s_texture";

    // Attribute constants
    public static final String AV_POSITION = "av_Position";
    public static final String AF_POSITION = "af_Position";
    private int avPositionLocation;
    private int afPositionLocation;
    private int uMatrixLocation;

    private int[] vao;
    private int[] vbo;
    private int[] fbo;
    private int[] mOffscreenTexture;
    private int cameraTextureid;

    private SurfaceTexture cameraSurfaceTexture;
    private VertexModel vertexModel;
    private VertexFboModel textureModel;
    private FBORender fboRender;
    private SurfaceTexture.OnFrameAvailableListener mListener;
    private boolean mCameraID = true;

    public CameraEncodeRender(Context context) {
        super(context);
    }

    public void init() {
        super.init(R.raw.egl_vertex_camera_matrix_shader, R.raw.egl_fragment_camera_shader);
        vertexModel = new VertexModel();
        textureModel = new VertexFboModel();
        avPositionLocation = GLES20.glGetAttribLocation(program, AV_POSITION);
        afPositionLocation = GLES20.glGetAttribLocation(program, AF_POSITION);
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        fboRender = new FBORender(mContext);
        setProperties(fboRender);
        GL_DEPTH_TEST();
        initCamera();
    }

    public SurfaceTexture getCameraSurfaceTexture() {
        return cameraSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated() {
        init();
        if (fboRender != null) {
            fboRender.onSurfaceCreated();
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        if (fboRender != null) {
            fboRender.setAngle(mAngle);
            fboRender.setZoomScale(mZoomPercent);
            fboRender.onSurfaceChanged(width, height);
        }
        initFbo(width, height);
    }

    @Override
    public void onViewPort() {
        if (fboRender != null) {
            fboRender.onViewPort();
        }
    }

    @Override
    public void onEncodeViewPort(int width, int height) {
        if (fboRender != null) {
            fboRender.onEncodeViewPort(width, height);
        }
    }

    @Override
    public void onViewChange() {
        if (fboRender != null) {
            fboRender.onViewChange();
        }
    }

    @Override
    public void onEncodeViewChange(int width, int height) {
        if (fboRender != null) {
            fboRender.onEncodeViewChange(width, height);
        }
    }

    @Override
    public void onEncodeDrawFrame() {
        if (program > 0) {
            if (cameraSurfaceTexture != null) {
                cameraSurfaceTexture.updateTexImage();
            }
            if (fboRender != null) {
                fboRender.setTextureID(mOffscreenTexture[0]);
                fboRender.onDrawFrame();
            }
        }
    }

    @Override
    public void onDrawFrame() {
        if (program > 0) {
            GL_DEPTH_TEST_CLEAR();
            if (cameraSurfaceTexture != null) {
                cameraSurfaceTexture.updateTexImage();
            }
            if (fboRender != null) {
                fboRender.setTextureID(mOffscreenTexture[0]);
                fboRender.onDrawFrame();
            }
            // scale zoom 在FBO中使用
            Matrix.setIdentityM(viewProjectionMatrix, 0);
            // 使用渲染器
            GLES20.glUseProgram(program);
            GLES30.glBindVertexArray(vao[0]);

            // 1绑定
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

            GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, viewProjectionMatrix, 0);

            // 绑定图片的纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureid);

            // 使顶点坐标和纹理坐标属性数组有效
            GLES20.glEnableVertexAttribArray(avPositionLocation);
            // 使用VBO 缓存时最后一个参数传0，不使用VBO ,最后一个参数传vertexBuffer
            GLES20.glVertexAttribPointer(avPositionLocation, 2, GLES20.GL_FLOAT, false, 8, 0);

            GLES20.glEnableVertexAttribArray(afPositionLocation);
            // 设置VBO 缓存时最后一个参数传入顶点坐标buffer的内存大小，偏移量
            GLES20.glVertexAttribPointer(afPositionLocation, 2, GLES20.GL_FLOAT, false, 8, vertexModel.getLength());
            // 绘制
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            // 解绑
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES30.glBindVertexArray(0);
        }
    }

    @Override
    public void onSurfaceDestroy() {
        CameraEncodeUtils.getInstance().release();
        if (fboRender != null) {
            fboRender.onSurfaceDestroy();
        }
        if (cameraSurfaceTexture != null) {
            cameraSurfaceTexture.release();
            cameraSurfaceTexture = null;
        }
        if (vertexModel != null) {
            vertexModel.getBuffer().clear();
        }
        if (textureModel != null) {
            textureModel.getBuffer().clear();
        }
        if (fbo != null) {
            GLES20.glDeleteFramebuffers(1, fbo, 0);
        }
        if (mOffscreenTexture != null) {
            GLES20.glDeleteTextures(1, mOffscreenTexture, 0);
        }
        if (vbo != null) {
            GLES20.glDeleteBuffers(1, vbo, 0);
        }
    }

    // 初始化
    public void initCamera() {
        // 创建VAO,管理vbo
        vao = new int[1];
        GLES30.glGenVertexArrays(1, vao, 0);
        GLES30.glBindVertexArray(vao[0]);

        // 创建 vbo
        vbo = new int[1];
        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexModel.getLength() + textureModel.getLength(),
                null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexModel.getLength(), vertexModel.getBuffer());
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexModel.getLength(), textureModel.getLength(), textureModel.getBuffer());
        // 解绑
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES30.glBindVertexArray(0);

       /* // fbo 离屏渲染
        fbo = new int[1];
        GLES20.glGenFramebuffers(1, fbo, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);

        mOffscreenTexture = new int[1];
        GLES20.glGenTextures(1, mOffscreenTexture, 0);
        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture[0]);

        // 设置环绕和过滤方式 环绕（超出纹理坐标范围）：（s==x t==y GL_REPEAT 重复）
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width
                , height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        // 把离屏纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mOffscreenTexture[0], 0);
        // 解绑fbo
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            LogUtils.d(TAG, "fbo wrong");
        } else {
            LogUtils.d(TAG, "fbo success");
        }*/

        int[] cameraTexture = new int[1];
        GLES20.glGenTextures(1, cameraTexture, 0);
        cameraTextureid = cameraTexture[0];

        // 绑定camera纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureid);
        // 设置环绕和过滤方式 环绕（超出纹理坐标范围）：（s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        cameraSurfaceTexture = new SurfaceTexture(cameraTextureid);
        if (mListener != null) {
            mListener.onFrameAvailable(cameraSurfaceTexture);
        }
        cameraSurfaceTexture.setOnFrameAvailableListener(this);
        // 调用相机
        CameraEncodeUtils.getInstance().initOpenGLCamera(mContext, cameraSurfaceTexture, mCameraID, EglConstant.CAMERA_RESOLUTION_720_POINT);
        LogUtils.d(TAG, "bind camera cameraTextureid:" + cameraTextureid + " cameraSurfaceTexture: " + cameraSurfaceTexture.hashCode());
    }

    public void initFbo(int width, int height) {
        // fbo 离屏渲染
        fbo = new int[1];
        GLES20.glGenFramebuffers(1, fbo, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
        mOffscreenTexture = new int[1];
        GLES20.glGenTextures(1, mOffscreenTexture, 0);
        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture[0]);

        // 设置环绕和过滤方式 环绕（超出纹理坐标范围）：（s==x t==y GL_REPEAT 重复）
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height
                , 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        // 把离屏纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mOffscreenTexture[0], 0);
        // 解绑fbo
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            LogUtils.d(TAG, "fbo wrong");
        } else {
            LogUtils.d(TAG, "fbo success");
        }
    }

    public void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener listener) {
        this.mListener = listener;
    }

    public int getCameraID() {
        return cameraTextureid;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        LogUtils.d(TAG, "onFrameAvailable");
    }

    private void setProperties(BaseIEglRenderInterface render) {
        if (render != null) {
            render.setGlClear(mIsClear);
            render.setScaleMode(mScaleMode);
            render.setAngle(mAngle);
            render.setZoomScale(mZoomPercent);
        }
    }

    /**
     * true 前置 false 后置
     * @param isFront
     */
    private void setCameraID(boolean isFront) {
        mCameraID = isFront;
    }
}
