package com.lena.android.model;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.lena.android.R;
import com.lena.android.db.ConstantSharedPreferences;
import com.lena.android.utils.Logger;
import com.lena.android.utils.VerifyUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.util.UUID;

public abstract class Phone {
    public final static String BRAND_XIAOMI = "xiaomi";
    public final static String BRAND_REDMI = "redmi";
    public final static String BRAND_SAMSUNG = "samsung";

    public final String mIdentity;
    protected final int logo;
    protected final int androidVersionCode; // SDK_INT
    protected final String androidVersionName;

    protected final String phoneModel;

    protected final Context context;

    public Phone(@NonNull final Context context) {
        this.context = context;

        this.mIdentity = getIdentity(context);
        this.logo = setPhoneLogo();
        this.androidVersionCode = Build.VERSION.SDK_INT;
        this.androidVersionName = Build.VERSION.RELEASE;

        this.phoneModel = getPhoneModel();
    }

    public String getPhoneModel() {
        final String brand = Build.BRAND;
        final String model = Build.MODEL;
        return String.format(Locale.getDefault(), "%s-%s", brand, model);
    }

    public int getPhoneLogo() {
        return this.logo;
    }

    @DrawableRes
    protected int setPhoneLogo() {
        return R.drawable.app_phone;
    }

    /**
     * ramInfo[0]: 可用空间 Byte (更精确计算)
     * ramInfo[1]: 总空间 Byte (修正值)
     * @return memorySize[2]
     */
    public final long[] getRAMInfo() {
        final long[] ramInfo = new long[]{0L, 0L};
        if (null != context) {
            try {
                final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                final ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(memInfo);

                // 修正1: 从/proc/meminfo读取总内存（更准确）
                long totalMem = 0;
                try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("MemTotal:")) {
                            totalMem = Long.parseLong(line.split("\\s+")[1]) * 1024; // KB转Byte
                            break;
                        }
                    }
                } catch (Exception e) {
                    totalMem = memInfo.totalMem; // 回退方案
                }

                // 修正2: 计算真实可用内存（排除缓存）
                ramInfo[0] = memInfo.availMem; // 已经是最小可用内存保证值
                ramInfo[1] = totalMem > 0 ? totalMem : memInfo.totalMem;

            } catch (Exception ignored) {}
        }
        return ramInfo;
    }

    /**
     * romInfo[0]: 可用空间 Byte (修正计算方式)
     * romInfo[1]: 总空间 Byte (修正计算方式)
     * @return romInfo[2]
     */
    public final long[] getROMInfo() {
        final long[] romInfo = new long[]{0L, 0L};
        try {
            final File path = Environment.getDataDirectory();
            final StatFs statFs = new StatFs(path.getPath());

            // 修正1: 使用更可靠的API
            final long blockSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ?
                    statFs.getBlockSizeLong() : (long) statFs.getBlockSize();

            // 修正2: 避免直接相乘导致的溢出
            final long availableBlocks = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ?
                    statFs.getAvailableBlocksLong() : (long) statFs.getAvailableBlocks();

            final long totalBlocks = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ?
                    statFs.getBlockCountLong() : (long) statFs.getBlockCount();

            // 安全计算
            romInfo[0] = safeMultiply(availableBlocks, blockSize);
            romInfo[1] = safeMultiply(totalBlocks, blockSize);

        } catch (Exception ignore) {}
        return romInfo;
    }

    private long safeMultiply(long a, long b) {
        return b == 0 ? 0 : (a > Long.MAX_VALUE / b) ? Long.MAX_VALUE : a * b;
    }

    public final String supportCpuInfo() {
        String cpuModel = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("model name")) {
                    int index = line.indexOf(":");
                    if (index != -1) {
                        cpuModel = line.substring(index + 1).trim();
                    }
                    break;
                }
            }
        } catch (Exception ignore) {}
        return cpuModel;
    }

    private String getIdentity(@NonNull final Context context) {
        final String phoneID = getPhoneID(context);
        if (VerifyUtil.aNotEmptyString(phoneID)) {
            // 成功获取系统提供的Android ID
            return phoneID;
        }

        // 无法获取系统提供的Android ID, 现在随机生成，并记录
        Logger.warn("Phone", "无法获取系统提供的Android ID, 使用随机生成UUID来代替");

        final String storeID = ConstantSharedPreferences.getStoreID(context);
        if (VerifyUtil.aNotEmptyString(storeID)) {
            // 之前已经生成过AndroidID
            return storeID;
        } else {
            // 需要记录本次生成的AndroidID
            final String iAndroidId = generateRandomID(context);
            ConstantSharedPreferences.setStoreID(context, iAndroidId);
            return iAndroidId;
        }
    }

    @SuppressLint("HardwareIds")
    private String getPhoneID(@NonNull final Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String generateRandomID(@NonNull final Context context) {
        return UUID.randomUUID().toString();
    }
}
