package vl.vision.test.utils.egl.render.camera;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import vl.vision.test.R;
import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.egl.base.egl.BaseIEglRenderInterface;
import vl.vision.test.utils.egl.render.ShaderProgram;
import vl.vision.test.utils.egl.render.TextureModel;
import vl.vision.test.utils.egl.render.VertexModel;


/**
 * 离屏渲染
 * Created by hanqq on 2021/6/27
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class FBORender extends ShaderProgram implements BaseIEglRenderInterface {
    private String TAG = this.getClass().getSimpleName();

    public static final String U_MATRIX = "u_Matrix";
    public static final String S_TEXTURE = "s_texture";
    // Attribute constants
    public static final String AV_POSITION = "av_Position";
    public static final String AF_POSITION = "af_Position";

    private VertexModel vertexModel;
    private TextureModel textureModel;
    private int av_position;
    private int af_position;
    private int sTexture;
    private int uMatrixLocation;

    public FBORender(Context context) {
        super(context);
    }

    public void onSurfaceCreated() {
        super.init(R.raw.egl_fbo_vertex_image_shader, R.raw.egl_fbo_fragment_image_shader);
        vertexModel = new VertexModel();
        textureModel = new TextureModel();
//        imageID = TextureHelper.loadTexture(mContext, R.mipmap.bg_default_1);
        initOpenGLES();
    }

    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        LogUtils.d(TAG, "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        renderFrame();
    }

    @Override
    public void onSurfaceDestroy() {
        if (vertexModel != null) {
            vertexModel.getBuffer().clear();
        }
        if (textureModel != null) {
            textureModel.getBuffer().clear();
        }
    }

    private void initOpenGLES() {
        if (program > 0) {
            LogUtils.d(TAG, "initOpenGLES");
            av_position = GLES20.glGetAttribLocation(program, AV_POSITION);
            af_position = GLES20.glGetAttribLocation(program, AF_POSITION);
            uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
            sTexture = GLES20.glGetUniformLocation(program, S_TEXTURE);
            GL_DEPTH_TEST();
        }
    }

    private void renderFrame() {
        if (program > 0) {
            GL_DEPTH_TEST_CLEAR();

            Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            // 使用渲染器
            GLES20.glUseProgram(program);
            GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, viewProjectionMatrix, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureID());
            GLES20.glUniform1i(sTexture, 0);

            GLES20.glEnableVertexAttribArray(av_position);
            GLES20.glVertexAttribPointer(av_position, 2, GLES20.GL_FLOAT, false, 8, vertexModel.getBuffer());
            GLES20.glEnableVertexAttribArray(af_position);
            GLES20.glVertexAttribPointer(af_position, 2, GLES20.GL_FLOAT, false, 8, getZoomFloatBuffer(textureModel.getBuffer()));
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }
    }

}
