package com.lena.android.widget;

import android.app.Activity;
import android.widget.Toast;

import com.lena.android.utils.VerifyUtil;

public class MyToast {
    private static Toast toast;

    public static void makeText(Activity activity, String msg, int duration) {
        if (null == activity || activity.isFinishing() || activity.isDestroyed()) return;
        if (!VerifyUtil.aNotEmptyString(msg)) return;
        if (duration != Toast.LENGTH_SHORT && duration != Toast.LENGTH_LONG) return;

        activity.runOnUiThread(() -> {
            if (null != toast) toast.cancel();
            toast = Toast.makeText(activity, msg, duration);
            toast.show();
        });
    }
}
