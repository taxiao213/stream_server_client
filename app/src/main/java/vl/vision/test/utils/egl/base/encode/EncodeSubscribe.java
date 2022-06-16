package vl.vision.test.utils.egl.base.encode;

import android.opengl.EGLSurface;
import android.view.Surface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import vl.vision.test.utils.egl.base.egl.BaseEglSurfaceView;
import vl.vision.test.utils.egl.base.egl.EglCore;


/**
 * 编码器注册
 * Created by hanqq on 2022/3/8
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class EncodeSubscribe implements BaseIEncodeRegisterInterface {

    private ArrayList<BaseIEncodeInterface> encodeList;

    public EncodeSubscribe() {
        encodeList = new ArrayList<>();
    }

    @Override
    public void registerEncode(BaseIEncodeInterface encode) {
        WeakReference<BaseIEncodeInterface> encodeWeakReference = new WeakReference<>(encode);
        encodeList.add(encodeWeakReference.get());
    }

    @Override
    public void unRegisterEncode() {
        try {
            if (encodeList != null) {
                while (encodeList.iterator().hasNext()) {
                    encodeList.iterator().remove();
                }
            }
            encodeList = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.onCreate();
        }
    }

    @Override
    public void onStart() {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.onStart();
        }
    }

    @Override
    public void onStop() {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.onStop();
        }
        unRegisterEncode();
    }

    @Override
    public void onFrame(byte[] bytes) {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.onFrame(bytes);
        }
    }

    @Override
    public void onRelease() {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.onRelease();
        }
    }

    @Override
    public Surface getInputSurface() {
        return null;
    }

    @Override
    public EGLSurface getEGLSurface() {
        return null;
    }

    @Override
    public void createEglSurface(EglCore eglCore) {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.createEglSurface(eglCore);
        }
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getFrameIndex() {
        return 0;
    }

    @Override
    public int getFps() {
        return 0;
    }

    public void makeCurrent(EglCore eglCore) {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.makeCurrent(eglCore);
        }
    }

    @Override
    public void makeCurrent(EglCore eglCore, EGLSurface readSurface) {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.makeCurrent(eglCore, readSurface);
        }
    }

    public void swapBuffers(EglCore eglCore) {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.swapBuffers(eglCore);
        }
    }

    // 编码
    public void doFrame(BaseEglSurfaceView baseEglSurfaceView, EglCore eglCore) {
        for (BaseIEncodeInterface baseEncode : encodeList) {
            baseEncode.makeCurrent(eglCore);
            // TODO: 2022/3/29 时间戳
//            eglCore.setPresentationTime(baseEncode.getEGLSurface(), MediacodecUtils.computePresentationTime(baseEncode.getFrameIndex(), baseEncode.getFps()) * 1000);
            baseEglSurfaceView.doFrame(baseEncode.getWidth(), baseEncode.getHeight());
            baseEncode.swapBuffers(eglCore);
        }
    }
}
