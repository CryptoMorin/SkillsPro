package org.skills.utils;

import com.google.common.base.Strings;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A util class providing a little more extra features in addition<br>
 * to Java's {@link Math} class.<br>
 * Uses the latest Java 8 concurrent techniques.
 * @author Crypto Morin
 * @version 2.0.0
 */
public class MathUtils {
    private static final DecimalFormat SHORT_DOUBLE = new DecimalFormat("#");
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)(\\S*)");

    /**
     * Evalutes the given equation using Java's Math class methods.
     * Supports all the methods from {@link Math} class as string.
     * All the mathematical operators and unique charcaters are also supported.
     * An invocation of this method creates an instance of {@link MathEval}.
     * @param eqn the equation to evalute.
     * @return the calculated equation.
     */
    public static double evaluateEquation(String eqn) {
        return MathEval.evaluate(eqn);
    }

    /**
     * Check if the passed argument is an integer value.
     * @param number double
     * @return true if the passed argument is an integer value.
     */
    public static boolean isInteger(double number) {
        // if the modulus(remainder of the division) of the argument(number) with 1 is 0 then return true otherwise false.
        return number % 1 == 0;
    }

    /**
     * Calculate a time in a form of a string.
     * Supports days to milliseconds.
     * @param time the string time with its unit.
     * @return the parsed milliseconds from the time unit.
     */
    public static Long calcMillis(String time, TimeUnit timeUnit) {
        if (Strings.isNullOrEmpty(time)) return null;
        time = StringUtils.deleteWhitespace(time).toLowerCase(Locale.ENGLISH);
        Matcher match = TIME_PATTERN.matcher(time);

        if (!match.matches()) return null;
        if (match.groupCount() > 1) {
            String unit = match.group(2);
            if (unit.length() > 8) return null;

            if (StringUtils.isOneOf(unit, "d", "day", "days")) timeUnit = TimeUnit.DAYS;
            else if (StringUtils.isOneOf(unit, "h", "hr", "hrs", "hour", "hours")) timeUnit = TimeUnit.HOURS;
            else if (StringUtils.isOneOf(unit, "m", "min", "mins", "minute", "minutes")) timeUnit = TimeUnit.MINUTES;
            else if (StringUtils.isOneOf(unit, "s", "sec", "secs", "second", "seconds")) timeUnit = TimeUnit.SECONDS;
        }

        long num = Long.parseUnsignedLong(match.group(1));
        if (num < 1) return 0L;
        return timeUnit.toMillis(num);
    }

    /**
     * Gets a random chance between 0 and 100
     * <p>An invocation of this method behaves in exactly the same way as the invocation
     * <p><blockquote>
     * {@link #randInt}(0, 100) <= percent
     * </blockquote>
     * @param percent the required chance percent.
     * @return true if the chance is equals or greater than the required chance.
     */
    public static boolean hasChance(int percent) {
        return hasChance(percent, 100);
    }

    public static boolean hasChance(int percent, int max) {
        return randInt(0, max) <= percent;
    }

    /**
     * A concurrent thread-safe random number generator.
     * Uses {@link ThreadLocalRandom}
     * @param min the possible minimum amount.
     * @param max the possible maximum amount.
     * @return a random number between the given values (values included as well).
     */
    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * A concurrent thread-safe random number generator.
     * Uses {@link ThreadLocalRandom}
     * @param min the possible minimum amount.
     * @param max the possible maximum amount.
     * @return a random number between the given values (values included as well).
     */
    public static double rand(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max + 1);
    }

    /**
     * Gets the percentage of the given number with a max value.
     * <p>An invocation of this method of the form {@code getPercent(current, max)} yields exactly the same result as the expression
     * <p><blockquote>
     * {@link #getAmountFromAmount}({@code current}, {@code max}, 100)
     * </blockquote>
     * @param max     the max possible value for the given value.
     * @param current the value.
     * @return the percentage of the value.
     */
    public static double getPercent(double current, double max) {
        return getAmountFromAmount(current, max, 100);
    }

    /**
     * Gets the percentage of the given number with a max value.
     * <p>
     * <b>Examples</b>
     * <p><blockquote>
     * tet
     * </blockquote>
     * @see #getPercent(double, double)
     */
    public static double getAmountFromAmount(double current, double max, double amount) {
        return ((current / max) * amount);
    }

    /**
     * What is P percent of X?
     * @param percent
     * @param amount
     * @return
     */
    public static double percentOfAmount(double percent, double amount) {
        return (amount * percent) / 100;
    }

    public static double percentOfComplexAmount(double percent, double amount, double max) {
        return (amount * percent) / max;
    }

    /**
     * Gets the number if the given string is a number.
     * @param number the number to parse as double.
     * @return the parsed double number or null if not a number.
     */
    public static Double getIfNumber(String number) {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Rounds a number to the specified decimal digit.
     * <p>
     * <b>Example</b>
     * <p><blockquote>
     * roundToDigits(1.1674524234, 2);
     * <br>
     * // Output: 1.17
     * </blockquote>
     * @param value     the actual number.
     * @param precision the decimal numbers to keep and round others.
     * @return a rounded decimal.
     */
    public static double roundToDigits(double value, int precision) {
        if (precision <= 0) return Math.round(value);
        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }

    /**
     * Converts big numbers to one decimal numbers with number prefix.
     * Only supports thousand, million and billion.
     * <p>
     * <b>Examples</b>
     * <p><blockquote><pre>
     *     getShortNumber(1255);
     *     // Output: 1.2K
     *
     *     getShortNumber(1560000);
     *     // Output: 1.5mil
     *
     *     getShortNumber(4900000000);
     *     // Output: 4.9bil
     * </pre></blockquote>
     * @param number the number to convert.
     * @return a short formatted number.
     */
    public static String getShortNumber(double number) {
        double quadrillion = 1000000000000000D;
        double trillion = 1000000000000D;
        double billion = 1000000000;
        double million = 1000000;
        double thousand = 1000;

        if (number < thousand) return String.valueOf(number);
        if (number >= quadrillion) return createShortNumber(number) + 'Q';
        if (number >= trillion) return createShortNumber(number) + 'T';
        if (number >= billion) return createShortNumber(number) + 'B';
        if (number >= million) return createShortNumber(number) + 'M';
        return createShortNumber(number) + 'K';
    }

    /**
     * Creates short readable formatted numbers.
     * @return a formatted number.
     */
    private static String createShortNumber(double number) {
        String str = SHORT_DOUBLE.format(number);
        int candiv = str.length() % 3;
        if (candiv == 0) candiv = 3;
        return str.substring(0, candiv) + '.' + str.charAt(candiv);
    }

    /**
     * Gets a random number within the range of the given numbers with<br>
     * a higher chance for higher numbers that <b>x + 1</b> has <b>+1</b> more<br>
     * chance than <b>x</b>.
     * @param min the minimum number in the range.
     * @param max the maximum number in the range.
     * @return a chosen number between the given range.
     */
    public static int increasingRandInt(int min, int max) {
        List<Integer> numbers = new ArrayList<>();

        for (int i = min; i < max; i++)
            for (int j = i; j >= 0; j--)
                numbers.add(j);

        int randInt = randInt(0, numbers.size() - 1);
        return numbers.get(randInt);
    }

    /**
     * Gets a random number within the range of the given numbers with<br>
     * a lower chance for lower numbers that <b>x + 1</b> has <b>-1</b> less<br>
     * chance than <b>x</b>.
     * @param min the minimum number in the range.
     * @param max the maximum number in the range.
     * @return a chosen number between the given range.
     */
    public static int decreasingRandInt(int min, int max) {
        List<Integer> numbers = new ArrayList<>();

        for (int i = min; i < max; i++)
            for (int j = max - i + 1; j >= 0; j--)
                numbers.add(i);

        int randInt = randInt(0, numbers.size() - 1);
        return numbers.get(randInt);
    }

    /**
     * Checks if a number is even.
     * @param number the number to check
     * @return true if the number is even, otherwise false as it's an odd number.
     */
    public static boolean isEven(long number) {
        return (number & 1) == 0;
    }

    public static boolean isPrime(long number) {
        if (number > 2 && isEven(number)) return false;

        // Only odd factors need to be tested up to n^0.5
        for (int i = 3; i * i <= number; i += 2)
            if (number % i == 0) return false;
        return true;
    }
}