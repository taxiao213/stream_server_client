package vl.vision.test.utils.egl.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;


import vl.vision.test.R;
import vl.vision.test.utils.egl.base.egl.BaseIEglRenderInterface;
import vl.vision.test.utils.egl.utils.TextureHelper;

import static android.opengl.GLES20.glViewport;


/**
 * Picture 渲染
 * Created by hanqq on 2022/3/14
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class PictureRender extends ShaderProgram implements BaseIEglRenderInterface {
    private String TAG = this.getClass().getSimpleName();

    // Uniform
    public static final String U_MATRIX = "u_Matrix";
    public static final String S_TEXTURE = "s_texture";

    // Attribute constants
    public static final String AV_POSITION = "av_Position";
    public static final String AF_POSITION = "af_Position";

    private VertexModel vertexModel;
    private TextureModel textureModel;
    private int avPositionLocation;
    private int afPositionLocation;
    private int uMatrixLocation;
    private int sTexture;
    private int[] textureid;

    public PictureRender(Context context) {
        super(context);
    }

    public void init() {
        super.init(R.raw.egl_vertex_image_shader, R.raw.egl_fragment_image_shader);
        vertexModel = new VertexModel();
        textureModel = new TextureModel();
        avPositionLocation = GLES20.glGetAttribLocation(program, AV_POSITION);
        afPositionLocation = GLES20.glGetAttribLocation(program, AF_POSITION);
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        sTexture = GLES20.glGetUniformLocation(program, S_TEXTURE);
        GL_DEPTH_TEST();
    }

    /**
     * 设置图片资源
     *
     * @param resourceId
     */
    public void setImageResource(int resourceId) {
        if (textureid == null) {
            textureid = new int[1];
        }
        textureid[0] = TextureHelper.loadTexture(mContext, resourceId);
    }

    @Override
    public void onSurfaceCreated() {
        init();
        if (textureid == null) {
            textureid = new int[1];
        }
        textureid[0] = TextureHelper.loadTexture(mContext, R.mipmap.ic_launcher_round);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        if (program > 0) {
            GL_DEPTH_TEST_CLEAR();

            // 两个矩阵相乘，并保存在第三个矩阵
            // left-hand-side matrix
            // right-hand-side matrix
            Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            // 使用渲染器
            GLES20.glUseProgram(program);
            GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, viewProjectionMatrix, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid[0]);
            GLES20.glUniform1i(sTexture, 0);

            GLES20.glEnableVertexAttribArray(avPositionLocation);
            GLES20.glVertexAttribPointer(avPositionLocation, 2, GLES20.GL_FLOAT, false, 8, vertexModel.getBuffer());
            GLES20.glEnableVertexAttribArray(afPositionLocation);
            GLES20.glVertexAttribPointer(afPositionLocation, 2, GLES20.GL_FLOAT, false, 8, getZoomFloatBuffer(textureModel.getBuffer()));
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
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
}
