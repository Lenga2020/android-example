package com.lena.android.model.phone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.lena.android.R;
import com.lena.android.model.Phone;
import com.lena.android.utils.VerifyUtil;

import java.lang.reflect.Method;
import java.util.Locale;

public class XiaomiPhone extends Phone {

    public XiaomiPhone(@NonNull Context context) {
        super(context);
    }

    // https://dev.mi.com/xiaomihyperos/documentation/detail?pId=1635
    @SuppressLint("PrivateApi")
    @Override
    public final String getPhoneModel() {
        String phoneModel = "";

        try {
            final Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            final Method get = SystemProperties.getDeclaredMethod("get", String.class, String.class);
            phoneModel = (String) get.invoke(SystemProperties, "ro.product.marketname", "");
            if (!VerifyUtil.aNotEmptyString(phoneModel)) {
                phoneModel = (String) get.invoke(SystemProperties, "ro.product.model", "");
            }
        } catch (Exception ignore) {}

        if (!VerifyUtil.aNotEmptyString(phoneModel)) {
            final String brand = Build.BRAND;
            final String model = Build.MODEL;
            return String.format(Locale.getDefault(), "%s-%s", brand, model);
        }

        return phoneModel;
    }

    @Override
    protected int setPhoneLogo() {
        return R.drawable.app_phone_xiaomi;
    }

    @SuppressLint("PrivateApi")
    public final String getCpuInfo() {
        String cpu = "";

        try {
            final Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            final Method get = SystemProperties.getDeclaredMethod("get", String.class, String.class);
            cpu = (String) get.invoke(SystemProperties, "ro.soc.model", "");
            if (!VerifyUtil.aNotEmptyString(cpu)) {
                cpu = supportCpuInfo();
            }
        } catch (Exception ignore) {}

        if (!VerifyUtil.aNotEmptyString(cpu)) {
            cpu = null == context? "": context.getString(R.string.app_text_unknown);
        }
        return cpu;
    }
}
