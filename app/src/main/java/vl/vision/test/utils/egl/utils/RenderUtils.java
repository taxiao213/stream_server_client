package vl.vision.test.utils.egl.utils;

import android.graphics.Bitmap;
import android.opengl.GLES20;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import vl.vision.test.utils.LogUtils;

/**
 * Created by hanqq on 2022/3/28
 * Email:yin13753884368@163.com
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
public class RenderUtils {
    public static String TAG = RenderUtils.class.getSimpleName();
    /**
     * Saves the EGL surface to a file.
     * <p>
     * Expects that this object's EGL surface is current.
     */
    public static void saveFrame(File file, int width, int height) {
        // glReadPixels fills in a "direct" ByteBuffer with what is essentially big-endian RGBA
        // data (i.e. a byte of red, followed by a byte of green...).  While the Bitmap
        // constructor that takes an int[] wants little-endian ARGB (blue/red swapped), the
        // Bitmap "copy pixels" method wants the same format GL provides.
        //
        // Ideally we'd have some way to re-use the ByteBuffer, especially if we're calling
        // here often.
        //
        // Making this even more interesting is the upside-down nature of GL, which means
        // our output will look upside down relative to what appears on screen if the
        // typical GL conventions are used.
        try {
            String filename = file.toString();
            ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            GLES20.glReadPixels(0, 0, width, height,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
            buf.rewind();
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(filename));
                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buf);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
                bmp.recycle();
            } finally {
                if (bos != null) bos.close();
            }
            LogUtils.d(TAG, "Saved " + width + "x" + height + " frame as '" + filename + "'");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
