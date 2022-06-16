package vl.vision.test.utils.egl.decode;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.egl.base.decode.BaseDecode;


/**
 * 解码 mp4 文件
 * 解码 h264 文件会有问题
 * Created by hanqq on 2022/3/23
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class H264Decode2 extends BaseDecode {
    public static final String TAG = H264Decode2.class.getSimpleName();

    private boolean mIsRuning = false;
    private Handler mHandler;
    private MediaCodec mEncoder;
    private int mWidth;
    private int mHeight;
    private ArrayBlockingQueue<byte[]> mBufferQueue;

    public H264Decode2(Surface surface, int Width, int height) {
        this.mSurface = surface;
        this.mWidth = Width;
        this.mHeight = height;
        this.mBufferQueue = new ArrayBlockingQueue<byte[]>(100);

        HandlerThread encodeHandler = new HandlerThread("decode_thread");
        encodeHandler.start();
        Looper looper = encodeHandler.getLooper();
        mHandler = new Handler(looper);
    }

    public void putBuffer(byte[] bufferInfo) {
        if (mBufferQueue != null) {
//            if (mBufferQueue.size() >= 100) {
//                mBufferQueue.poll();
//            }
            mBufferQueue.add(bufferInfo);
        }
    }

    @Override
    public void onCreate() {
        try {
            MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight);
            mEncoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mEncoder.configure(videoFormat, mSurface, null, 0);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "onCreate, Exception: " + e.getMessage());
        }
    }

    @Override
    public void onStart() {
        LogUtils.d(TAG, "onStart");
        if (mEncoder == null) return;
        mEncoder.start();
        mIsRuning = true;
        mHandler.post(() -> {
            try {
                while (mIsRuning) {
                    if (mBufferQueue.size() > 0) {
                        byte[] byteBuffer = mBufferQueue.poll();
                        if (byteBuffer != null) {
                            int inputBufferindex = mEncoder.dequeueInputBuffer(0);
                            if (inputBufferindex > 0) {
                                ByteBuffer inputBuffer = mEncoder.getInputBuffers()[inputBufferindex];
                                inputBuffer.clear();
                                inputBuffer.put(byteBuffer);
                                mEncoder.queueInputBuffer(inputBufferindex, 0, byteBuffer.length, 0, 0);
                            }
                        }
                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int dequeueOutputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 1000);
                    while (dequeueOutputBufferIndex >= 0) {
                        LogUtils.d(TAG, " dequeueOutputBufferIndex: " + dequeueOutputBufferIndex);
                        mEncoder.releaseOutputBuffer(dequeueOutputBufferIndex, true);
                        dequeueOutputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0);
                    }
//                    LogUtils.d(TAG, " encode end");
                }
                onRelease();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.d(TAG, "onStart Exception : " + e.getMessage());
            }
        });
    }

    @Override
    public void onStop() {
        LogUtils.d(TAG, "onStop");
        mIsRuning = false;
    }

    @Override
    public void onRelease() {
        try {
            if (mEncoder != null) {
                mEncoder.stop();
                mEncoder.release();
                mEncoder = null;
            }

            if (mHandler != null) {
                mHandler.getLooper().quit();
                mHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (mEncoder != null) mEncoder.start();
    }

    public void decode(byte[] byteBuffer) {
        mHandler.post(() -> {
            try {
                if (byteBuffer != null) {
                    int inputBufferindex = mEncoder.dequeueInputBuffer(0);
                    if (inputBufferindex > 0) {
                        ByteBuffer inputBuffer = mEncoder.getInputBuffers()[inputBufferindex];
                        inputBuffer.clear();
                        inputBuffer.put(byteBuffer);
                        mEncoder.queueInputBuffer(inputBufferindex, 0, byteBuffer.length, 0, 0);
                    }
                }

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int dequeueOutputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 1000);
                while (dequeueOutputBufferIndex >= 0) {
                    LogUtils.d(TAG, " dequeueOutputBufferIndex: " + dequeueOutputBufferIndex);
                    mEncoder.releaseOutputBuffer(dequeueOutputBufferIndex, true);
                    dequeueOutputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.d(TAG, "onStart Exception : " + e.getMessage());
            }
        });
    }
}
