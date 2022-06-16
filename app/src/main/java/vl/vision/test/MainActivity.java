package vl.vision.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;


import java.io.File;

import vl.vision.test.utils.FileUtils;
import vl.vision.test.utils.Function;
import vl.vision.test.utils.RemoteStreamDialog;
import vl.vision.test.utils.XXPermissionsUtils;

public class MainActivity extends AppCompatActivity {

    private Context mContext = MainActivity.this;
    String FILE_NAME = "decodeCrash.mp4";
    private String file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XXPermissionsUtils.getInstances().hasCameraPermission(new Function<Boolean>() {
            @Override
            public void action(Boolean var) {

            }
        }, mContext);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File cacheDir = mContext.getCacheDir();
                File streamFile = new File(cacheDir, FILE_NAME);
                if (!streamFile.exists()) {
                    Toast.makeText(mContext, "请先读取文件", Toast.LENGTH_SHORT).show();
                    return;
                }
                XXPermissionsUtils.getInstances().hasCameraPermission(new Function<Boolean>() {
                    @Override
                    public void action(Boolean var) {
                        if (var != null && var) {
                            Intent intent = new Intent(mContext, TestActivity.class);
                            intent.putExtra("stream", streamFile.getAbsolutePath());
                            startActivity(intent);
                        }
                    }
                }, mContext);
            }
        });

        findViewById(R.id.test2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                file = FileUtils.copyAssetAndWrite(mContext, FILE_NAME);
                if (!TextUtils.isEmpty(file)) {
                    Toast.makeText(mContext, "读取成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "读取失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.test3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File cacheDir = mContext.getCacheDir();
                File streamFile = new File(cacheDir, FILE_NAME);
                if (streamFile.exists()) {
                    streamFile.delete();
                    Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}