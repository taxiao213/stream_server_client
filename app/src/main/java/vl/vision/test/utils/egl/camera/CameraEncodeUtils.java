package vl.vision.test.utils.egl.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


import java.io.IOException;
import java.util.List;


import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.egl.base.encode.BaseIEncodeInterface;

import static android.content.Context.WINDOW_SERVICE;


/**
 * 打开camera id 1 ，通过 hdmi 发送 content
 * Created by hanqq on 2020/7/15
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class CameraEncodeUtils {
    public static final String TAG = CameraEncodeUtils.class.getSimpleName();
    private static volatile CameraEncodeUtils mHexCameraUtils;
    private Camera camera;
    private SurfaceHolder.Callback callback;
    private SurfaceHolder holder;
    private BaseIEncodeInterface mEncodeInterface;
    private HandlerThread mCameraHandler;
    int mFps = 20;
    boolean mIsFront; //true 前置 false 后置  默认前置
    Context mContext;
    int mWidth;
    int mHeight;
    private int cameraID;

    public static CameraEncodeUtils getInstance() {
        if (mHexCameraUtils == null) {
            synchronized (CameraEncodeUtils.class) {
                if (mHexCameraUtils == null) {
                    mHexCameraUtils = new CameraEncodeUtils();
                }
            }
        }
        return mHexCameraUtils;
    }

    /**
     * 初始化相机
     *
     * @param context     上下文
     * @param surfaceView SurfaceView
     * @param isFront     true 前置 false 后置  默认前置
     */
    public void initCamera(Context context, SurfaceView surfaceView, boolean isFront, int fps, Point point, BaseIEncodeInterface encodeInterface) {
        if (!checkCameraHardware(context)) return;
        this.mFps = fps;
        this.mContext = context;
        mIsFront = isFront;
        mWidth = point.x;
        mHeight = point.y;
        mEncodeInterface = encodeInterface;
        holder = surfaceView.getHolder();
        callback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogUtils.d(TAG, "surfaceCreated thread " + Thread.currentThread().getName() + " id: " + Thread.currentThread().getId() + " loop :" + (Looper.myLooper() == Looper.getMainLooper()));
                createCamera(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LogUtils.d(TAG, "surfaceChanged ");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogUtils.d(TAG, "surfaceDestroyed ");
            }
        };
        holder.addCallback(callback);
    }

    /**
     * 初始化相机
     *
     * @param holder SurfaceHolder
     */
    private void createCamera(final SurfaceHolder holder) {
        LogUtils.d(TAG, "6 params createCamera , currentThreadName: " + Thread.currentThread().getName());
        try {
            mCameraHandler = new HandlerThread("camera1");
            mCameraHandler.start();
            Looper looper = mCameraHandler.getLooper();
            Handler handler = new Handler(looper);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // 切换到子线程
                    if (mEncodeInterface != null) {
                        mEncodeInterface.onCreate();
                    }
                    start(holder);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化相机
     *
     * @param holder SurfaceHolder
     */
    private void start(SurfaceHolder holder) {
        try {
            int cameraID = getCameraID(mIsFront);
            LogUtils.d(TAG, " cameraID: " + cameraID);
            camera = Camera.open(cameraID);
            if (camera != null) {
                Camera.Parameters parameters = camera.getParameters();
                if (parameters != null) {
                    choosePreviewSize(parameters, mWidth, mHeight);
                    int mCameraPreviewThousandFps = chooseFixedPreviewFps(parameters, mFps * 1000);
                    // Give the camera a hint that we're recording video.  This can have a big
                    // impact on frame rate.
//                    parameters.setRecordingHint(true);
                    camera.setParameters(parameters);
                    Camera.Size cameraPreviewSize = parameters.getPreviewSize();
                    String previewFacts = cameraPreviewSize.width + "x" + cameraPreviewSize.height +
                            " @" + (mCameraPreviewThousandFps / 1000.0f) + "fps";
                    LogUtils.d(TAG, "previewFacts : " + previewFacts);
                    WindowManager windowManagerService = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
                    if (windowManagerService != null) {
                        Display display = windowManagerService.getDefaultDisplay();
                        if (display != null) {
                            LogUtils.d(TAG, "display Rotation : " + display.getRotation());
                            if (display.getRotation() == Surface.ROTATION_0) {
                                camera.setDisplayOrientation(90);
//                        layout.setAspectRatio((double) cameraPreviewSize.height / cameraPreviewSize.width);
                            } else if (display.getRotation() == Surface.ROTATION_270) {
//                        layout.setAspectRatio((double) cameraPreviewSize.height / cameraPreviewSize.width);
                                camera.setDisplayOrientation(180);
                            } else {
                                // Set the preview aspect ratio.
//                        layout.setAspectRatio((double) cameraPreviewSize.width / cameraPreviewSize.height);
                            }
                        }
                    }
                }

                if (holder != null) {
                    camera.setPreviewDisplay(holder);
                }
                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if (mEncodeInterface != null) {
                            mEncodeInterface.onFrame(data);
                        }
                        LogUtils.d(TAG, "onPreviewFrame :"+data.length);
                    }
                });
                if (mEncodeInterface != null) {
                    mEncodeInterface.onStart();
                }
                camera.startPreview();
                LogUtils.d(TAG, "startPreview :");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "exception " + e.getMessage());
        }
    }

    /**
     * 获取cameraID
     * true 前置 0
     * false 后置 1
     */
    private int getCameraID(boolean isFront) {
        // ve260 hdmi 默认cameraID 是1
        int cameraID = 0;
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras == 1) {
            // 如果只有一个摄像头，默认是0
            cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (numberOfCameras == 2) {
            if (isFront) {
                cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }
        return cameraID;
    }

    /**
     * 是否有摄像头
     */
    private boolean checkCameraHardware(Context context) {
        if (context == null) return false;
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
//            ToastUtils.show(context.getString(R.string.camera_not_exist));
            return false;
        }
    }

    public void choosePreviewSize(Camera.Parameters parms, int width, int height) {
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
        if (ppsfv != null) {
            LogUtils.d(TAG, "Camera preferred preview size for video is " +
                    ppsfv.width + "x" + ppsfv.height);
        }

        for (Camera.Size size : parms.getSupportedPreviewSizes()) {
            LogUtils.d(TAG, "supported: " + size.width + "x" + size.height);
        }

        for (Camera.Size size : parms.getSupportedPreviewSizes()) {
            if (size.width == width && size.height == height) {
                parms.setPreviewSize(width, height);
                return;
            }
        }

        LogUtils.d(TAG, "Unable to set preview size to " + width + "x" + height);
        if (ppsfv != null) {
            parms.setPreviewSize(ppsfv.width, ppsfv.height);
        }
        // else use whatever the default size is
    }

    public int chooseFixedPreviewFps(Camera.Parameters parms, int desiredThousandFps) {
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            LogUtils.d(TAG, "entry: " + entry[0] + " - " + entry[1]);
            if ((entry[0] == entry[1]) && (entry[0] == desiredThousandFps)) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }

        int[] tmp = new int[2];
        parms.getPreviewFpsRange(tmp);
        int guess;
        if (tmp[0] == tmp[1]) {
            guess = tmp[0];
        } else {
            guess = tmp[1] / 2;     // shrug
        }

        LogUtils.d(TAG, "Couldn't find match for " + desiredThousandFps + ", using " + guess);
        return guess;
    }

    /**
     * 释放资源
     */
    public void release() {
        LogUtils.d(TAG, "release");
        try {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
            if (holder != null && callback != null) {
                holder.removeCallback(callback);
                callback = null;
                holder = null;
            }

            if (mCameraHandler != null) {
                mCameraHandler.getLooper().quit();
                mCameraHandler.quit();
                mCameraHandler = null;
            }
            if (mEncodeInterface != null) {
                mEncodeInterface.onStop();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "exception surfaceDestroyed " + e.getMessage());
        }
    }

    public void initOpenGLCamera(Context context, SurfaceTexture surfaceTexture, boolean isFront, Point point) {
        try {
            if (!checkCameraHardware(context)) return;
            this.mContext = context;
            this.mIsFront = isFront;
//            mCameraHandler = new HandlerThread("camera1");
//            mCameraHandler.start();
//            Looper looper = mCameraHandler.getLooper();
//            Handler handler = new Handler(looper);
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
            // 切换到子线程
            initCamera(context, surfaceTexture, point);
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initCamera(Context context, SurfaceTexture surfaceTexture, Point point) {
        LogUtils.d(TAG, "initCamera inner width:" + point.x + " height:" + point.y);
        try {
            cameraID = getCameraID(mIsFront);
            camera = Camera.open(cameraID);
            if (camera != null) {
                Camera.Parameters parameters = camera.getParameters();
                if (parameters != null) {
//                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    parameters.setPreviewSize(point.x, point.y);
                    // Give the camera a hint that we're recording video.  This can have a big
                    // impact on frame rate.
//                    parameters.setRecordingHint(true);
                    camera.setParameters(parameters);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "exception " + e.getMessage());
        } finally {
            if (camera != null) {
                try {
                    camera.setPreviewTexture(surfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
                LogUtils.d(TAG, "camera.startPreview ");
            }
        }
    }

    public int getCameraID() {
        return cameraID;
    }
}
