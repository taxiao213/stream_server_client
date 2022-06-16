package vl.vision.test.utils.egl.base.decode;


import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * 解码器注册
 * Created by hanqq on 2022/3/8
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class DecodeSubscribe implements BaseIDecodeRegisterInterface {

    private ArrayList<BaseIDecodeInterface> decodeList;

    public DecodeSubscribe() {
        decodeList = new ArrayList<>();
    }

    @Override
    public void registerDecode(BaseIDecodeInterface decode) {
        WeakReference<BaseIDecodeInterface> encodeWeakReference = new WeakReference<>(decode);
        decodeList.add(encodeWeakReference.get());
    }

    @Override
    public void unRegisterDecode() {
        try {
            if (decodeList != null) {
                while (decodeList.iterator().hasNext()) {
                    decodeList.iterator().remove();
                }
            }
            decodeList = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        for (BaseIDecodeInterface baseDecode : decodeList) {
            baseDecode.onCreate();
        }
    }

    @Override
    public void onStart() {
        for (BaseIDecodeInterface baseDecode : decodeList) {
            baseDecode.onStart();
        }
    }

    @Override
    public void onStop() {
        for (BaseIDecodeInterface baseDecode : decodeList) {
            baseDecode.onStop();
        }
        unRegisterDecode();
    }

    @Override
    public void onFrame(byte[] bytes) {
        for (BaseIDecodeInterface baseDecode : decodeList) {
            baseDecode.onFrame(bytes);
        }
    }

    @Override
    public void onRelease() {
        for (BaseIDecodeInterface baseDecode : decodeList) {
            baseDecode.onRelease();
        }
    }

}
