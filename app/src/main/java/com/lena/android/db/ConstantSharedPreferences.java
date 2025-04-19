package com.lena.android.db;

import android.content.Context;

import com.lena.android.utils.VerifyUtil;

public class ConstantSharedPreferences {
    private final static String SP_NAME = "app_sp_info";
    private final static int MODE = Context.MODE_PRIVATE;

    private final static String STORE_ID = "android_id";
    public static String getStoreID(Context context) {
        return context.getSharedPreferences(SP_NAME, MODE).getString(STORE_ID, "");
    }
    public static void setStoreID(Context context, String androidID) {
        if (VerifyUtil.aNotEmptyString(androidID)) context.getSharedPreferences(SP_NAME, MODE)
                .edit()
                .putString(STORE_ID, androidID)
                .apply();
    }

}
