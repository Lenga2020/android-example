package com.lena.android.api;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.lena.android.model.Phone;
import com.lena.android.model.phone.Samsung;
import com.lena.android.model.phone.XiaomiPhone;

import java.util.Locale;

public final class PhoneManager {
    private static boolean instanced = false;
    private volatile static PhoneManager phoneManager = null;

    private final Context mApplicationContext;
    public final Phone mPhone;

    private PhoneManager(@NonNull final Context context) {
        synchronized (PhoneManager.class) {
            if (instanced || null != phoneManager) {
                final String error = String.format(Locale.getDefault(), "instantiate multiple %s(constructor).", this.getClass().getName());
                throw new RuntimeException(error);
            }
            instanced = true;

            this.mApplicationContext = context;

            this.mPhone = generatePhone();

        }
    }

    public static void initPhoneManager(@NonNull final Context context) {
        if (phoneManager == null) {
            synchronized (PhoneManager.class) {
                if (phoneManager == null) {
                    phoneManager = new PhoneManager(context);
                }
            }
        }
    }

    public static PhoneManager getInstance() {
        return getPhoneManager();
    }

    public static PhoneManager getPhoneManager() {
        return phoneManager;
    }

    private Phone generatePhone() {
        if (Phone.BRAND_XIAOMI.equalsIgnoreCase(Build.MANUFACTURER) || Phone.BRAND_REDMI.equalsIgnoreCase(Build.MANUFACTURER)) {
            return new XiaomiPhone(mApplicationContext);
        } else if (Phone.BRAND_SAMSUNG.equalsIgnoreCase(Build.MANUFACTURER)) {
            return new Samsung(mApplicationContext);
        }
        return new Phone(mApplicationContext) {};
    }
}
