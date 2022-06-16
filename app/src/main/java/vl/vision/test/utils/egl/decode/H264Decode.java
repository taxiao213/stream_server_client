package vl.vision.test.utils.egl.decode;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;


import java.io.File;
import java.nio.ByteBuffer;

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
public class H264Decode extends BaseDecode {
    public static final String TAG = H264Decode.class.getSimpleName();

    private boolean mIsRuning = false;
    private Handler mHandler;
    private MediaCodec mEncoder;
    public MediaExtractor mediaExtractor;
    public boolean mUseMediaMuxer = false;// 解码文件类型 false h264 流文件 , true Mp4文件
    boolean mLoop = false;

    public H264Decode(Surface surface, File file) {
        this.mSurface = surface;
        this.mFile = file;
        this.mUseMediaMuxer = true;
        HandlerThread encodeHandler = new HandlerThread("decode_thread");
        encodeHandler.start();
        Looper looper = encodeHandler.getLooper();
        mHandler = new Handler(looper);
    }

    public H264Decode(Surface surface, File file, boolean loop) {
        this(surface, file);
        this.mLoop = loop;
    }

    @Override
    public void onCreate() {
        try {
            MediaFormat videoFormat = null;
            if (mUseMediaMuxer && mFile != null && mFile.exists()) {
                mediaExtractor = new MediaExtractor();
                mediaExtractor.setDataSource(mFile.toString());
                int trackIndex = selectTrack(mediaExtractor);
                LogUtils.d(TAG, "onCreate, trackIndex: " + trackIndex);
                if (trackIndex < 0) {
                    return;
                }
                mediaExtractor.selectTrack(trackIndex);
                videoFormat = mediaExtractor.getTrackFormat(trackIndex);
            }
            mWidth = videoFormat.getInteger(MediaFormat.KEY_WIDTH);
            mHeight = videoFormat.getInteger(MediaFormat.KEY_HEIGHT);
            mFps = videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
            LogUtils.d(TAG, "onCreate, frameRate: " + mFps);
            videoFormat.setInteger(MediaFormat.KEY_MAX_WIDTH, mWidth);
            videoFormat.setInteger(MediaFormat.KEY_MAX_HEIGHT, mHeight);
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
                    Thread.sleep(Math.round(1000f / mFps));
                    int inputBufferindex = mEncoder.dequeueInputBuffer(0);
                    if (inputBufferindex > 0) {
                        ByteBuffer inputBuffer = mEncoder.getInputBuffers()[inputBufferindex];
                        if (mUseMediaMuxer) {
                            int chunkSize = mediaExtractor.readSampleData(inputBuffer, 0);
                            if (chunkSize < 0) {
                                mEncoder.queueInputBuffer(inputBufferindex, 0, 0, 0L,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            } else {
                                long presentationTimeUs = mediaExtractor.getSampleTime();
                                LogUtils.d(TAG, "presentationTimeUs: " + presentationTimeUs);
                                mEncoder.queueInputBuffer(inputBufferindex, 0, chunkSize,
                                        presentationTimeUs, 0);
                                mediaExtractor.advance();
                            }
                        }
                    }

                    // 编码前数据
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int dequeueOutputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 1000);
                    while (dequeueOutputBufferIndex >= 0) {
                        LogUtils.d(TAG, " dequeueOutputBufferIndex: " + dequeueOutputBufferIndex);
                        mEncoder.releaseOutputBuffer(dequeueOutputBufferIndex, true);
                        dequeueOutputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0);
                    }
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        LogUtils.d(TAG, "output EOS");
                        if (mLoop) {
                            mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                            mEncoder.flush();
                        }
                    }
                    LogUtils.d(TAG, " encode end");
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
            if (mediaExtractor != null) {
                mediaExtractor.release();
                mediaExtractor = null;
            }

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

    public void setLoopMode(boolean loopMode) {
        mLoop = loopMode;
    }

    private static int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 寻找指定buffer中h264头的开始位置
     *
     * @param data   数据
     * @param offset 偏移量
     * @param max    需要检测的最大值
     * @return h264头的开始位置 ,-1表示未发现
     */
    private int findHead(byte[] data, int offset, int max) {
        for (int i = offset; i < max - 5; i++) {
            //发现帧头
            if (isHead4(data, i)) {
                return i;
            }
        }

        for (int i = offset; i < max - 4; i++) {
            //发现帧头
            if (isHead3(data, i)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 判断是否是I帧/P帧头:
     * 00 00 00 01 65 (I帧)
     * 00 00 00 01 61 / 41 (P帧)
     *
     * @param data
     * @param offset
     * @return 是否是帧头
     */
    private boolean isHead4(byte[] data, int offset) {
        boolean result = false;
        // 00 00 00 01 x
        if (data[offset] == 0x00 && data[offset + 1] == 0x00
                && data[offset + 2] == 0x00 && data[3] == 0x01 && isVideoFrameHeadType(data[offset + 4])) {
            result = true;
        }
        return result;
    }

    private boolean isHead3(byte[] data, int offset) {
        boolean result = false;
        // 00 00 01 x
        if (data[offset] == 0x00 && data[offset + 1] == 0x00
                && data[offset + 2] == 0x01 && isVideoFrameHeadType(data[offset + 3])) {
            result = true;
        }
        return result;
    }

    private boolean isHead(byte[] data, int offset) {
        boolean result = false;
        // 00 00 00 01 x
        if (data[offset] == 0x00 && data[offset + 1] == 0x00
                && data[offset + 2] == 0x00 && data[3] == 0x01 && isVideoFrameHeadType(data[offset + 4])) {
            result = true;
        }
        // 00 00 01 x
        if (data[offset] == 0x00 && data[offset + 1] == 0x00
                && data[offset + 2] == 0x01 && isVideoFrameHeadType(data[offset + 3])) {
            result = true;
        }
        return result;
    }

    /**
     * I帧或者P帧
     */
    private boolean isVideoFrameHeadType(byte head) {
        return head == (byte) 0x65 || head == (byte) 0x61 || head == (byte) 0x41;
    }

}
