package com.wzt.yolov5;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class LSTR {
    static {
        System.loadLibrary("yolov5");  // 存放在yolov5.so中
    }

    public static native void init(AssetManager manager, int yoloType, boolean useGPU);
    public static native Box[] detect(Bitmap bitmap, double threshold, double nms_threshold);
    public static native boolean advancedKeyCheck(long advanced_key);
    public static native long getAdvancedKey(long idx);
    public static native void setFastExp(boolean is_use_fast_exp);
}
