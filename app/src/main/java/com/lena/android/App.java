package com.lena.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.lena.android.api.PhoneManager;
import com.lena.android.db.ConstantSharedPreferences;
import com.lena.android.utils.Logger;
import com.lena.android.utils.TimeUtil;
import com.lena.android.utils.VerifyUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class App extends Application {
    private final static String TAG = "Application";

    public int notificationIndex = 0;

    private int activities;
    private boolean release;
    private String applicationId;
    private int versionCode;
    private String versionName;

    public final HashSet<String> googleBillingIds = new HashSet<>();

    public static App app;
    public SimpleCache cache;

    @Override
    public void onCreate() {
        super.onCreate();

        release = !BuildConfig.DEBUG;
        applicationId = BuildConfig.APPLICATION_ID;
        versionCode = BuildConfig.VERSION_CODE;
        versionName = BuildConfig.VERSION_NAME;

        cache = initVideoCache();

        Set<String> acknowledgeIds = ConstantSharedPreferences.getAcknowledgeIds(this);
        if (null != acknowledgeIds && !acknowledgeIds.isEmpty()) {
            googleBillingIds.addAll(acknowledgeIds);
        }

        activities = 0;
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                activities += 1;

                final String localClassName = activity.getLocalClassName();
                if (activities == 1) {
                    Logger.info(TAG, localClassName + " launched from background.");
                } else {
                    Logger.info(TAG, localClassName + " onStarted.");
                }

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                activities = Math.max(0, activities-1);

                final String localClassName = activity.getLocalClassName();
                if (activities == 0) {
                    Logger.info(TAG, localClassName + " back to background.");
                } else {
                    Logger.info(TAG, localClassName + " onStopped.");
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });

        app = this;
        PhoneManager.initPhoneManager(getApplicationContext());

        final long timeMillis = TimeUtil.getCurrentTimeMillis();
        final String timeZoneId = TimeUtil.getTimeZoneId();
        final String logger = String.format(Locale.getDefault(), "App launched at: %s(%s)", TimeUtil.getDateTime(timeMillis, timeZoneId), timeZoneId);
        Logger.debug(TAG, logger);


    }

    public boolean isReleaseMode() {
        return release;
    }

    public boolean isDebugMode() {
        return !isReleaseMode();
    }

    @NonNull
    public String getApplicationId() {
        return VerifyUtil.aNotEmptyString(applicationId)? applicationId: "UNKNOWN";
    }

    public int getVersionCode() {
        return versionCode;
    }

    @NonNull
    public String getVersionName() {
        return VerifyUtil.aNotEmptyString(versionName)? versionName: "UNKNOWN";
    }

    private final static String CACHE_DIR = "videos";
    private final static long CACHE_SIZE = 2L * 1000L * 1000L * 1000L; //2GB; 1000bytes=1kb, 1000*1000bytes=1mb, 1000*1000*1000bytes=1GB
    private SimpleCache initVideoCache() {
        final File cacheDir = new File(getCacheDir(), CACHE_DIR);
        final LeastRecentlyUsedCacheEvictor cacheEvict = new LeastRecentlyUsedCacheEvictor(CACHE_SIZE);
        final StandaloneDatabaseProvider databaseProvider = new StandaloneDatabaseProvider(this);
        return new SimpleCache(cacheDir, cacheEvict, databaseProvider);
    }
}
