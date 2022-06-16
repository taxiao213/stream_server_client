package vl.vision.test.utils;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;

import vl.vision.test.R;


/**
 * Created by hanqq on 2022/6/14
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class RemoteStreamDialog {
    private Context mContext = null;
    private AlertDialog mAlertDialog;
    private RelativeLayout rlRoot = null;


    public RemoteStreamDialog(Context context) {
        this.mContext = context;
        mAlertDialog = new AlertDialog
                .Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                            onDismiss();
                        }
                        return false;
                    }
                }).create();

        if (context instanceof Service || context instanceof Application) {
            Window window = mAlertDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams attributes = window.getAttributes();
                if (attributes != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        attributes.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        attributes.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                    }
                }
                window.setAttributes(attributes);
            }
        }
        mAlertDialog.show();
        init();
    }

    private void init() {
        if (mContext != null) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
            int width = outMetrics.widthPixels;
            Window window = mAlertDialog.getWindow();
            if (window != null) {
                window.setLayout(width / 2, WindowManager.LayoutParams.MATCH_PARENT);
                window.setDimAmount(0f);
                window.setContentView(R.layout.remote_stream);
                window.setGravity(Gravity.TOP | Gravity.LEFT);
                rlRoot = window.findViewById(R.id.rl_root);
            }
        }
    }

    public void onDismiss() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }
}
