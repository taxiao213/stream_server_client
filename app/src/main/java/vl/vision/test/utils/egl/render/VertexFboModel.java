package vl.vision.test.utils.egl.render;

import java.nio.FloatBuffer;

/**
 * Created by hanqq on 2022/3/14
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class VertexFboModel {
    public VertexArray vertexArray;

    // 纹理坐标 FBO 离屏绘制和手机正常坐标不同
    // FBO离屏渲染的纹理坐标系以左下角为（0，0），左上角（0，1），右上角（1，1），右下角（1，0）
    // 手机正常的纹理坐标系以左下角为（0，1），左上角（0，0），右上角（1，0），右下角（1，1）
    public final float[] VERTEX_DATA = {
            // x,y
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f
    };

    public VertexFboModel() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public int getLength() {
        return VERTEX_DATA.length * 4;
    }

    public FloatBuffer getBuffer() {
        return vertexArray.floatBuffer;
    }
}
