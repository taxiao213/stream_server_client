package vl.vision.test.utils.egl.base.egl;

/**
 * Created by hanqq on 2022/3/22
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public abstract class BaseEglRender implements BaseIEglRenderInterface {
    private int textureID;

    @Override
    public int getTextureID() {
        return textureID;
    }

    @Override
    public void setTextureID(int id) {
        textureID = id;
    }

}
