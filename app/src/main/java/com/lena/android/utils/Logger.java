package com.lena.android.utils;

import android.util.Log;

import com.lena.android.App;

import java.util.Locale;

public final class Logger {
    private final static String TAG_PREFIX = "AE";
    private final static int DEFAULT_TAG_LENGTH = 16;
    private final static int DEFAULT_MESSAGE_LENGTH = 1024;

    private static boolean getLoggerAble() {
        return null != App.app && !App.app.isReleaseMode();
    }

    private static String getTag(final String tag) {
        final String tagSuffix = !VerifyUtil.aNotEmptyString(tag) || tag.length() > DEFAULT_TAG_LENGTH? "TAG": tag.trim();
        return String.format(Locale.getDefault(), "%s-%s", TAG_PREFIX, tagSuffix);
    }

    private static String getMessage(final String message) {
        if (VerifyUtil.aNotEmptyString(message)) {
            final String trim = message.trim();
            final int length = trim.length();
            if (length > DEFAULT_MESSAGE_LENGTH) {
                final int partLength = DEFAULT_MESSAGE_LENGTH / 2;
                return String.format(Locale.getDefault(), "%s...%s", trim.substring(0, partLength), trim.substring(length-partLength));
            }
            return trim;
        }
        return "null";
    }

    public static void debug(String tag, String msg) {
        if (getLoggerAble()) {
            Log.d(getTag(tag), getMessage(msg));
        }
    }

    public static void info(String tag, String msg) {
        if (getLoggerAble()) {
            Log.i(getTag(tag), getMessage(msg));
        }
    }

    public static void warn(String tag, String msg) {
        warn(tag, msg, null);
    }

    public static void warn(String tag, String msg, Throwable throwable) {
        if (getLoggerAble()) {
            Log.w(getTag(tag), getMessage(msg), throwable);
        }
    }

    public static void error(String tag, String msg) {
        error(tag, msg, null);
    }

    public static void error(String tag, String msg, Throwable throwable) {
        if (getLoggerAble()) {
            Log.e(getTag(tag), getMessage(msg), throwable);
        }
    }
}
