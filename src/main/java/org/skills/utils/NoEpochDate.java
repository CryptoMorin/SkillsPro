package org.skills.utils;

import com.google.common.base.Strings;
import org.skills.main.locale.MessageHandler;

import java.util.concurrent.TimeUnit;

public class NoEpochDate {
    public final long millis;

    public final long absoluteSeconds;
    public final long absoluteMinutes;
    public final long absoluteHours;
    public final long absoluteDays;
    public final long absoluteWeeks;
    public final long absoluteMonths;
    public final long years;

    public final long seconds;
    public final long minutes;
    public final long hours;
    public final long days;
    public final long weeks;
    public final long months;

    public NoEpochDate(long time) {
        this(time, TimeUnit.MILLISECONDS);
    }

    public NoEpochDate(long time, TimeUnit unit) {
        millis = unit.toMillis(Math.abs(time));
        absoluteSeconds = millis / 1000;
        absoluteMinutes = absoluteSeconds / 60;
        absoluteHours = absoluteMinutes / 60;
        absoluteDays = absoluteHours / 24;

        absoluteWeeks = absoluteDays / 7;
        absoluteMonths = absoluteDays / 30;
        years = absoluteDays / 356;


        seconds = (long) Math.floor(absoluteSeconds % 60);
        minutes = (long) Math.floor(absoluteMinutes % 60);
        hours = (long) Math.floor(absoluteHours % 24);
        days = (long) Math.floor(absoluteDays % 30);
        weeks = (long) Math.floor(absoluteWeeks % 7);
        months = (long) Math.floor(absoluteMonths % 12);
    }

    private static String replaceVariables(String str, Object... edits) {
        for (int i = edits.length; i > 0; i -= 2) {
            String variable = String.valueOf(edits[i - 2]);
            String replacement = String.valueOf(edits[i - 1]);
            str = MessageHandler.replace(str, variable, replacement);
        }
        return str;
    }

    private static String format(long number) {
        return number >= 10 ? Long.toString(number) : "0" + number;
    }

    @Override
    public String toString() {
        return years + " " + absoluteMonths + ' ' + absoluteWeeks + ' ' + absoluteDays + ' ' + absoluteHours + ' ' + absoluteMinutes + ' ' + absoluteSeconds
                + ' ' + months + ' ' + weeks + ' ' + days + ' ' + hours + ' ' + minutes + ' ' + seconds + ' ' + millis;
    }

    public String format(String format) {
        if (Strings.isNullOrEmpty(format)) return format;
        if (format.equalsIgnoreCase("managed")) return formatManaged();

        return replaceVariables(format, "yyyy", this.years,
                "MM", format(this.months), "MMA", this.absoluteMonths,
                "wwww", this.weeks, "wwwwa", this.absoluteWeeks,
                "dd", format(this.days), "dda", this.absoluteDays,
                "hh", format(this.hours), "hhf", this.hours, "hha", format(this.absoluteHours), "hhaf", this.absoluteHours,
                "mm", format(this.minutes), "mmf", this.minutes, "mma", format(this.absoluteMinutes), "mmaf", format(this.absoluteMinutes),
                "ss", format(this.seconds), "ssf", this.seconds, "ssa", format(this.absoluteSeconds), "ssaf", this.absoluteSeconds,
                "mss", this.millis);
    }

    public String formatManaged() {
        if (absoluteMinutes == 0) return format("00:00:ssa");
        if (absoluteHours == 0) return format("00:mma:ss");
        if (absoluteDays == 0) return format("hha:mm:ss");
        if (absoluteWeeks == 0) return format("dda days, hh:mm:ss");
        if (absoluteMonths == 0) return format("wwwwa weeks, dd days, hh:mm:ss");
        return format("MMA months, wwww weeks, dd days, hh:mm:ss");
    }
}
