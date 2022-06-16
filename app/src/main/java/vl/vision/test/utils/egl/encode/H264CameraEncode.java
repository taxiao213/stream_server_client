package vl.vision.test.utils.egl.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;


import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.egl.base.encode.BaseEncode;
import vl.vision.test.utils.egl.utils.BitRate;
import vl.vision.test.utils.egl.utils.EglConstant;
import vl.vision.test.utils.egl.utils.MediacodecUtils;


/**
 * camera 采集到的数据直接 h264 编码
 * Created by hanqq on 2022/3/15
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 * <p>
 * SurfaceView view = new SurfaceView(mContext);
 * baseEncodeCamera = new EncodeSubscribe();
 * H264CameraEncode encode1 = new H264CameraEncode(1280, 720, 25, true, true, file2.getAbsolutePath(), BitRate.LOW_BIT_RATE);
 * baseEncodeCamera.registerEncode(encode1);
 * CameraEncodeUtils.getInstance().initCamera(mContext, view, false, 20, 1280, 720, baseEncodeCamera);
 */
public class H264CameraEncode extends BaseEncode {

    public static final String TAG = H264CameraEncode.class.getSimpleName();
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;    // H.264 Advanced Video Coding

    private int mTrackIndex = -1;
    private int TIMEOUT_USEC = 10000;
    private int generateIndex = 0;
    private boolean mIsSave = false;
    private boolean mUseMediaMuxer = false;// false h264 文件 , true 保存为Mp4文件
    private boolean mIsRuning = false;
    private byte[] configbyte;
    private Handler mHandler;
    private MediaCodec mEncoder;
    private MediaMuxer mMuxer;
    private FileOutputStream outputStream = null;
    private String mFileName;
    private ArrayBlockingQueue<byte[]> mYuvQueue;

    public H264CameraEncode(int width, int height, int fps, Enum<BitRate> bitRate) {
        this.mWidth = width;
        this.mHeight = height;
        this.mFps = fps;
        this.mBitRate = bitRate;
        HandlerThread encodeHandler = new HandlerThread("encode_thread");
        encodeHandler.start();
        Looper looper = encodeHandler.getLooper();
        mHandler = new Handler(looper);
        mYuvQueue = new ArrayBlockingQueue<>(10);
    }

    public H264CameraEncode(int width, int height, int fps, boolean isSave, boolean useMediaMuxer, String fileName, Enum<BitRate> bitRate) {
        this(width, height, fps, bitRate);
        this.mFileName = fileName;
        this.mIsSave = isSave;
        this.mUseMediaMuxer = useMediaMuxer;
    }

    @Override
    public void onCreate() {
        LogUtils.d(TAG, "onCreate, width: " + mWidth + " height: " + mHeight);
        MediaCodecInfo codecInfo = MediacodecUtils.selectCodec(MIME_TYPE);
        if (codecInfo == null) {
            LogUtils.d(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        int colorFormat = MediacodecUtils.selectColorFormat(codecInfo, MIME_TYPE);
        LogUtils.d(TAG, "found colorFormat: " + colorFormat);
        MediaFormat videoFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFps);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, (int) (mWidth * mHeight * EglConstant.getBitRate().get(mBitRate)));
        if (mFps >= 25) {
            videoFormat.setInteger("svc-layer", 31);
        } else if (mFps >= 15) {
            videoFormat.setInteger("svc-layer", 2);
        } else {
            videoFormat.setInteger("svc-layer", 1);
        }

//        videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
//        videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41);
//        videoFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        try {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
            Bundle params = new Bundle();
            params.putInt("time_scale", 40);
            params.putInt("num_units_in_tick", 1);
            params.putInt("timing_info_present_flag", 1);
            mEncoder.setParameters(params);
            mEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            if (mIsSave && !mFileName.isEmpty()) {
                if (mUseMediaMuxer) {
                    mMuxer = new MediaMuxer(mFileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                } else {
                    outputStream = new FileOutputStream(mFileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    long startTime = 0;

    @Override
    public void onStart() {
        if (mEncoder == null) return;
        mEncoder.start();
        mIsRuning = true;
        mHandler.post(() -> {
            try {
                startTime = System.nanoTime();
                while (mIsRuning) {
                    byte[] input = null;
                    if (mYuvQueue.size() > 0) {
                        input = mYuvQueue.poll();
                        byte[] yuv420sp = new byte[mWidth * mHeight * 3 / 2];
                        // 必须要转格式，否则录制的内容播放出来为绿屏
                        MediacodecUtils.NV21ToNV12(input, yuv420sp, mWidth, mHeight);
                        input = yuv420sp;
                    }
                    if (input != null) {
                        // 编码前数据
//                        Thread.sleep(1000/15);
                        int inputBufferindex = mEncoder.dequeueInputBuffer(TIMEOUT_USEC);
                        if (inputBufferindex > 0) {
                            long ptsUsec = MediacodecUtils.computePresentationTime(generateIndex, mFps);
//                            long ptsUsec = MediacodecUtils.computePresentationTime(startTime);
                            ByteBuffer inputBuffer = mEncoder.getInputBuffers()[inputBufferindex];
                            inputBuffer.clear();
                            inputBuffer.put(input);
                            LogUtils.d(TAG, "inputBufferindex: " + inputBufferindex + " ptsUsec: " + ptsUsec);
                            mEncoder.queueInputBuffer(inputBufferindex, 0, input.length, ptsUsec, 0);
                            generateIndex++;
                        }
                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int dequeueOutputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                    if (dequeueOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        LogUtils.d(TAG, "no output from encoder available");
                    } else if (dequeueOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        LogUtils.d(TAG, "encoder output buffers changed");
                    } else if (dequeueOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (mIsSave && mUseMediaMuxer) {
                            MediaFormat newFormat = mEncoder.getOutputFormat();
                            mTrackIndex = mMuxer.addTrack(newFormat);
                            LogUtils.d(TAG, "encoder output format changed: " + newFormat + " mTrackIndex: " + mTrackIndex);
                            mMuxer.start();
                        }
                    } else if (dequeueOutputBufferIndex < 0) {
                        LogUtils.d(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + dequeueOutputBufferIndex);
                    } else {
                        // dequeueOutputBufferIndex >= 0
                        LogUtils.d(TAG, " dequeueOutputBufferIndex: " + dequeueOutputBufferIndex);
                        ByteBuffer outputBuffer = mEncoder.getOutputBuffers()[dequeueOutputBufferIndex];
                        if (mIsSave && mUseMediaMuxer) {
                            if (mMuxer != null) {
                                mMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo);
                            }
                        } else {
                            byte[] outData = new byte[bufferInfo.size];
                            outputBuffer.get(outData);
                            if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                                configbyte = outData;
                            } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                                byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                                System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                                System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                                if (mIsSave && outputStream != null) {
                                    outputStream.write(keyframe, 0, keyframe.length);
                                }
                            } else {
                                if (mIsSave && outputStream != null) {
                                    outputStream.write(outData, 0, outData.length);
                                }
                            }
                        }

                        mEncoder.releaseOutputBuffer(dequeueOutputBufferIndex, false);
                    }

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
    public void onFrame(byte[] bytes) {
        if (mYuvQueue.size() >= 10) {
            mYuvQueue.poll();
        }
        mYuvQueue.add(bytes);
    }

    @Override
    public int getFrameIndex() {
        return 0;
    }

    // 停止编解码器并释放资源
    @Override
    public void onRelease() {
        try {
            if (mMuxer != null) {
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
            }

            if (mEncoder != null) {
                mEncoder.stop();
                mEncoder.release();
                mEncoder = null;
            }

            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            }

            if (mHandler != null) {
                mHandler.getLooper().quit();
                mHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
