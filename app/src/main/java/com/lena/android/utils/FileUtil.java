package com.lena.android.utils;

import androidx.annotation.NonNull;

public class FileUtil {
    public enum Unit {
        Bit("bit"), Byte("byte"), KB("kb"), MB("MB"), GB("GB");

        final String value;
        Unit(String value) {
            this.value = value;
        }
    }

    public static long convert(final long inValue, @NonNull final Unit inUnit, @NonNull final Unit toUnit) {
        if (inValue <= 0) {
            return inValue;
        }

        final long valueInBits;
        switch (inUnit) {
            case Bit:
                valueInBits = inValue;
                break;
            case Byte:
                valueInBits = inValue * 8L;
                break;
            case KB:
                valueInBits = inValue * 8L * 1024L;
                break;
            case MB:
                valueInBits = inValue * 8L * 1024L * 1024L;
                break;
            case GB:
                valueInBits = inValue * 8L * 1024L * 1024L * 1024L;
                break;
            default:
                throw new IllegalArgumentException("Unknown input unit");
        }

        final long toValue;
        switch (toUnit) {
            case Bit:
                toValue = valueInBits;
                break;
            case Byte:
                toValue = valueInBits / 8L;
                break;
            case KB:
                toValue = valueInBits / (8 * 1024L);
                break;
            case MB:
                toValue = valueInBits / (8L * 1024L * 1024L);
                break;
            case GB:
                toValue = valueInBits / (8L * 1024L * 1024L * 1024L);
                break;
            default:
                throw new IllegalArgumentException("Unknown target unit");
        }
        return toValue;
    }
}
