package com.lena.android.utils;

import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class TimeUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()));
    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()));

    public static long getCurrentTimeMillis() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                return SystemClock.currentNetworkTimeClock().millis();
            } catch (Exception ignore) {}
        }
        return System.currentTimeMillis();
    }

    public static String getTimeZoneId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                final ZoneId zoneId = SystemClock.currentNetworkTimeClock().getZone();
                return zoneId != null ? zoneId.getId() : TimeZone.getDefault().getID();
            } catch (Exception ignore) {}
        }
        return TimeZone.getDefault().getID();
    }

    public static String getDate() {
        return getDate(getCurrentTimeMillis());
    }

    public static String getDate(final long timeMillis) {
        return getDate(timeMillis, getTimeZoneId());
    }

    public static String getDate(final String timeZoneId) {
        return getDate(getCurrentTimeMillis(), timeZoneId);
    }

    public static String getDate(final long timeMillis, final String timeZoneId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getLocalDateOrTime(timeMillis, getZoneId(timeZoneId), DATE_FORMAT);
        } else {
            return getCalendarDateOrTime(timeMillis, getTimeZone(timeZoneId), DATE_FORMAT);
        }
    }

    public static String getDateTime() {
        return getDateTime(getCurrentTimeMillis());
    }

    public static String getDateTime(final long timeMillis) {
        return getDateTime(timeMillis, getTimeZoneId());
    }

    public static String getDateTime(final String timeZoneId) {
        return getDateTime(getCurrentTimeMillis(), timeZoneId);
    }

    public static String getDateTime(final long timeMillis, final String timeZoneId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getLocalDateOrTime(timeMillis, getZoneId(timeZoneId), DATE_TIME_FORMAT);
        } else {
            return getCalendarDateOrTime(timeMillis, getTimeZone(timeZoneId), DATE_TIME_FORMAT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String getLocalDateOrTime(final long timeMillis, @NonNull final ZoneId zoneId, @NonNull final String formatter) {
        final Instant instant = Instant.ofEpochMilli(timeMillis);
        final LocalDateTime localTime = instant.atZone(zoneId).toLocalDateTime();
        return localTime.format(DateTimeFormatter.ofPattern(formatter));
    }

    private static String getCalendarDateOrTime(final long timeMillis, @NonNull final TimeZone timeZone, @NonNull final String formatter) {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(timeMillis);
        final SimpleDateFormat simpleDateFormat = formatter.equals(DATE_TIME_FORMAT)? DATE_TIME_FORMATTER.get(): DATE_FORMATTER.get();
        if (null != simpleDateFormat) simpleDateFormat.setTimeZone(timeZone);
        return null == simpleDateFormat? "01/01/1970": simpleDateFormat.format(calendar.getTime());
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static ZoneId getZoneId(String timeZoneId) {
        if (null == timeZoneId || timeZoneId.isBlank()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(timeZoneId);
        } catch (Exception ignore) {
            return ZoneId.systemDefault();
        }
    }

    private static TimeZone getTimeZone(String timeZoneId) {
        if (null == timeZoneId || timeZoneId.isBlank()) {
            return TimeZone.getDefault();
        }
        return TimeZone.getTimeZone(timeZoneId);
    }
}
