package vl.vision.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import vl.vision.app.IStreamService;
import vl.vision.test.utils.Function;
import vl.vision.test.utils.LogUtils;
import vl.vision.test.utils.XXPermissionsUtils;
import vl.vision.test.utils.egl.base.decode.DecodeSubscribe;
import vl.vision.test.utils.egl.base.egl.BaseEglSurfaceView;
import vl.vision.test.utils.egl.base.egl.BaseIEglRenderInterface;
import vl.vision.test.utils.egl.base.encode.EncodeSubscribe;
import vl.vision.test.utils.egl.decode.H264Decode;
import vl.vision.test.utils.egl.decode.H264Decode2;
import vl.vision.test.utils.egl.decode.H264Decode3;
import vl.vision.test.utils.egl.encode.H264OpenGLEncode;
import vl.vision.test.utils.egl.render.camera.CameraEncodeRender;
import vl.vision.test.utils.egl.utils.BitRate;
import vl.vision.test.utils.egl.utils.EglConstant;
import vl.vision.test.utils.egl.view.CameraEncodeSurfaceView;

public class TestActivity extends AppCompatActivity {

    private final String TAG = "TestActivity";
    private Context mContext = TestActivity.this;
    private int mFps = 25;
    private BaseEglSurfaceView baseEglSurfaceView;
    private RelativeLayout rl_1;
    private RelativeLayout rl_2;
    public static final String URL = "ws://172.23.0.162:8088/stream_server/websocket";

    WebSocketClient webSocketClient;

    private IStreamService streamService;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Object obj = msg.obj;
            if (h264Decode2 != null) {
                if (streamService != null) {
                    try {
                        streamService.sendStream(new byte[0]);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
//                h264Decode2.putBuffer((byte[]) obj);
            }
        }
    };
    private H264Decode2 h264Decode2;
    private DecodeSubscribe decodeSubscribe;
    private File streamFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        rl_1 = findViewById(R.id.rl_1);
        rl_2 = findViewById(R.id.rl_2);
        Intent intent = getIntent();
        if (intent != null) {
            String filePath = intent.getStringExtra("stream");
            if (!TextUtils.isEmpty(filePath)) {
                LogUtils.d(TAG, "filePath: " + filePath);
                streamFile = new File(filePath);
            }
        }
        connectionWebsocket();
        bindService();
//        playMovie();
    }

    // 本地测试
    private void playMovie() {
//        File file = new File(mContext.getCacheDir(), EglConstant.HDMI_CONTENT_FILE);
//        if (!file.exists()) file.mkdirs();
//        File hdmiContentFile = new File(file, "720.mp4");
        if (streamFile != null && streamFile.exists()) {
            decodeSubscribe = new DecodeSubscribe();
            SurfaceView surfaceView = new SurfaceView(mContext);
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                    Surface surface = surfaceHolder.getSurface();
//                    H264Decode decode = new H264Decode(surface, streamFile, true);
                    H264Decode3 decode = new H264Decode3(surface, streamFile, true);
                    decode.setHandler(handler);
                    decodeSubscribe.registerDecode(decode);
                    decodeSubscribe.onCreate();
                    decodeSubscribe.onStart();
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                    decodeSubscribe.onStop();
                    decodeSubscribe.onRelease();
                }
            });
            rl_2.addView(surfaceView);
        }
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.ntp_service_action));
        intent.setPackage(getResources().getString(R.string.ntp_service_package));
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            streamService = IStreamService.Stub.asInterface(service);
            try {
                streamService.register();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(TAG, " onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, " onServiceDisconnected");
            try {
                streamService.unregister();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (streamService != null) {
                streamService.unregister();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        if (decodeSubscribe != null) {
            decodeSubscribe.onStop();
        }
    }

    /**
     * 连接Websocket
     */
    private void connectionWebsocket() {
        try {
            SurfaceView surfaceView2 = new SurfaceView(mContext);
            surfaceView2.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                    Surface surface = surfaceHolder.getSurface();
                    h264Decode2 = new H264Decode2(surface, 1280, 720);
                    h264Decode2.onCreate();
                    h264Decode2.start();
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                }
            });
            rl_2.addView(surfaceView2);

            webSocketClient = new WebSocketClient(URI.create(URL)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    LogUtils.d(TAG, "onOpen == ");
                    webSocketClient.send("android ");
                }

                @Override
                public void onMessage(String message) {
                    LogUtils.d(TAG, "onMessage == " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LogUtils.d(TAG, "onClose == code " + code + " reason == " + reason + " remote == " + remote);
                }

                @Override
                public void onError(Exception ex) {
                    LogUtils.d(TAG, "onError== " + ex.getMessage());
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    LogUtils.d(TAG, "onMessage ByteBuffer");
//                        outputStream.write(bytes.array());
                    if (h264Decode2 != null && bytes != null) {
                        h264Decode2.decode(bytes.array());
                    }

                    if (streamService != null) {
                        try {
                            streamService.sendStream(bytes.array());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
//                    webSocketClient.send("receive");
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "socket Exception : " + e.getMessage());
        }
    }

}