package com.lena.android.db;

import android.content.Context;

import com.lena.android.utils.VerifyUtil;

import java.util.HashSet;
import java.util.Set;

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

    private final static String ACKNOWLEDGE_IDS = "acknowledge_ids";
    public static Set<String> getAcknowledgeIds(Context context) {
        return context.getSharedPreferences(SP_NAME, MODE).getStringSet(ACKNOWLEDGE_IDS, null);
    }
    public static void putAcknowledgeId(Context context, String id) {
        if (VerifyUtil.aNotEmptyString(id)) {
            final Set<String> acknowledgeIds = getAcknowledgeIds(context);

            final HashSet<String> strings = new HashSet<>();
            if (null != acknowledgeIds && !acknowledgeIds.isEmpty()) {
                strings.addAll(acknowledgeIds);
            }
            strings.add(id);

            context.getSharedPreferences(SP_NAME, MODE).edit()
                    .putStringSet(ACKNOWLEDGE_IDS, strings)
                    .apply();
        }
    }
}
