package vl.vision.test.utils.egl.utils;

/**
 * 缩放模式
 * Created by hanqq on 2022/3/12
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public enum ScaleMode {
    FIT_XY(0),
    CENTER_INSIDE(1);

    ScaleMode(int ni) {
        scaleMode = ni;
    }

    final int scaleMode;
}
