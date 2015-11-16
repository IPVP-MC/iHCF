package org.ipvp.hcf;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeFormats {

    private DateTimeFormats() {
    }

    public static final TimeZone SERVER_TIME_ZONE = TimeZone.getTimeZone("EST");
    public static final ZoneId SERVER_ZONE_ID = SERVER_TIME_ZONE.toZoneId();
    public static final FastDateFormat DAY_MTH_HR_MIN_SECS = FastDateFormat.getInstance("dd/MM HH:mm:ss", SERVER_TIME_ZONE, Locale.ENGLISH);
    public static final FastDateFormat DAY_MTH_YR_HR_MIN_AMPM = FastDateFormat.getInstance("dd/MM/yy hh:mma", SERVER_TIME_ZONE, Locale.ENGLISH);
    public static final FastDateFormat DAY_MTH_HR_MIN_AMPM = FastDateFormat.getInstance("dd/MM hh:mma", SERVER_TIME_ZONE, Locale.ENGLISH);
    public static final FastDateFormat HR_MIN_AMPM = FastDateFormat.getInstance("hh:mma", SERVER_TIME_ZONE, Locale.ENGLISH);
    public static final FastDateFormat HR_MIN_AMPM_TIMEZONE = FastDateFormat.getInstance("hh:mma z", SERVER_TIME_ZONE, Locale.ENGLISH);
    public static final FastDateFormat HR_MIN = FastDateFormat.getInstance("hh:mm", SERVER_TIME_ZONE, Locale.ENGLISH);
    public static final FastDateFormat MIN_SECS = FastDateFormat.getInstance("mm:ss", SERVER_TIME_ZONE, Locale.ENGLISH);

    public static final FastDateFormat KOTH_FORMAT = FastDateFormat.getInstance("m:ss", SERVER_TIME_ZONE, Locale.ENGLISH);

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
