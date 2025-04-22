package com.lena.android.widget;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.lena.android.utils.VerifyUtil;

public class MySnackBar {
    private static Snackbar snackbar;

    public static void show(Activity activity, View view, String msg, int duration) {
        if (null == activity || activity.isFinishing() || activity.isDestroyed()) return;
        if (null == view || null == view.getWindowToken()) return;
        if (!VerifyUtil.aNotEmptyString(msg)) return;
        if (duration != Snackbar.LENGTH_SHORT && duration != Snackbar.LENGTH_LONG) return;

        activity.runOnUiThread(() -> {
            if (null != snackbar) snackbar.dismiss();
            snackbar = Snackbar.make(activity, view, msg, duration);
            snackbar.show();
        });
    }
}
