package vl.vision.test.utils.egl.render;

import java.nio.FloatBuffer;

/**
 * Created by hanqq on 2022/3/14
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class TextureModel {
    public VertexArray vertexArray;
    public final float[] VERTEX_DATA = {
            // x,y
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    public TextureModel() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public int getLength() {
        return VERTEX_DATA.length * 4;
    }

    public FloatBuffer getBuffer() {
        return vertexArray.floatBuffer;
    }
}
