package com.doctordark.hcf;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DateTimeFormats {

    private static final AtomicBoolean loaded = new AtomicBoolean(false);

    public static FastDateFormat DAY_MTH_HR_MIN_SECS;
    public static FastDateFormat DAY_MTH_YR_HR_MIN_AMPM;
    public static FastDateFormat DAY_MTH_HR_MIN_AMPM;
    public static FastDateFormat HR_MIN_AMPM;
    public static FastDateFormat HR_MIN_AMPM_TIMEZONE;
    public static FastDateFormat HR_MIN;
    public static FastDateFormat KOTH_FORMAT;

    private DateTimeFormats() {
    }

    public static void reload(TimeZone timeZone) throws IllegalStateException {
        Preconditions.checkArgument(!loaded.getAndSet(true), "Already loaded");

        DAY_MTH_HR_MIN_SECS = FastDateFormat.getInstance("dd/MM HH:mm:ss", timeZone, Locale.ENGLISH);
        DAY_MTH_YR_HR_MIN_AMPM = FastDateFormat.getInstance("dd/MM/yy hh:mma", timeZone, Locale.ENGLISH);
        DAY_MTH_HR_MIN_AMPM = FastDateFormat.getInstance("dd/MM hh:mma", timeZone, Locale.ENGLISH);
        HR_MIN_AMPM = FastDateFormat.getInstance("hh:mma", timeZone, Locale.ENGLISH);
        HR_MIN_AMPM_TIMEZONE = FastDateFormat.getInstance("hh:mma z", timeZone, Locale.ENGLISH);
        HR_MIN = FastDateFormat.getInstance("hh:mm", timeZone, Locale.ENGLISH);
        KOTH_FORMAT = FastDateFormat.getInstance("m:ss", timeZone, Locale.ENGLISH);
    }

    // The format used to show one decimal without a trailing zero.
    public static final ThreadLocal<DecimalFormat> REMAINING_SECONDS = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("0.#");
        }
    };

    public static final ThreadLocal<DecimalFormat> REMAINING_SECONDS_TRAILING = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("0.0");
        }
    };
}
