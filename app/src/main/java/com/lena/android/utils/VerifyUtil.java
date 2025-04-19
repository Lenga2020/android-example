package com.lena.android.utils;

import java.util.regex.Pattern;

public final class VerifyUtil {
    public static boolean aNotEmptyString(final String string) {
        return null != string && !string.isBlank();
    }

    // 简单判断是否是一个email是否是一个邮箱格式的字符串
    private final static Pattern MAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$");
    public static boolean aValidEmail(final String email) {
        return null != email && !email.isBlank() && MAIL_PATTERN.matcher(email).matches();
    }

    // 简单判断字符串能否作为Retrofit的Base URL
    private final static Pattern BASEURL_PATTERN = Pattern.compile("^https?://((([a-zA-Z0-9_-]+\\.)+[a-zA-Z]{2,})|(localhost)|((\\d{1,3}\\.){3}\\d{1,3}))(:\\d+)?(/.*)?/?$");
    public static boolean aValidBaseUrl(final String baseUrl) {
        return null != baseUrl && !baseUrl.isBlank() && BASEURL_PATTERN.matcher(baseUrl).matches();
    }

    // 简单判断字符串是不是http链接
    private final static Pattern WEB_URL_PATTERN = Pattern.compile("^(https?://)?(www\\.)?[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$");
    public static boolean aValidWebUrl(final String url) {
        return null != url && !url.isBlank() && WEB_URL_PATTERN.matcher(url).matches();
    }
}
