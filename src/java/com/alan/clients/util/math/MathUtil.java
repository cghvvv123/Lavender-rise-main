package com.alan.clients.util.math;

import lombok.experimental.UtilityClass;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Patrick, Hazsi
 * @since 11/17/2021
 */
@UtilityClass
public class MathUtil {
    public static Random random;
    /**
     * Method which returns a double between two input numbers
     *
     * @param min minimal number
     * @param max maximal number
     * @return random between both numbers
     */
    public double getRandom(double min, double max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return min + ThreadLocalRandom.current().nextDouble() * (max - min);
    }
    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
    public static int getRandom2(final int min, final int max) {
        if (max < min) {
            return 0;
        }
        return min + MathUtil.random.nextInt(max - min + 1);
    }
    public static int getRandomNumberUsingNextInt(final int min, final  int max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }
    public static int interpolateInt(final int oldValue, final int newValue, final double interpolationValue) {
        return (int) interpolate(oldValue, newValue, (float)interpolationValue);
    }
    public static float interpolate(final double oldValue, final double newValue, final double interpolationValue) {
        return (float) (oldValue + (newValue - oldValue) * interpolationValue);
    }
    public double round(final double value, final int places) {
        final BigDecimal bigDecimal = BigDecimal.valueOf(value);

        return bigDecimal.setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
    public static int getRandomInRange(final int min, final int max) {
        return (int)(Math.random() * (max - min) + min);
    }
    public static double getRandomInRange(final double min, final double max) {
        final SecureRandom random = new SecureRandom();
        return (min == max) ? min : (random.nextDouble() * (max - min) + min);
    }
    public double round(final double value, final int scale, final double inc) {
        final double halfOfInc = inc / 2.0;
        final double floored = Math.floor(value / inc) * inc;

        if (value >= floored + halfOfInc) {
            return new BigDecimal(Math.ceil(value / inc) * inc)
                    .setScale(scale, RoundingMode.HALF_UP)
                    .doubleValue();
        } else {
            return new BigDecimal(floored)
                    .setScale(scale, RoundingMode.HALF_UP)
                    .doubleValue();
        }
    }

    public double roundWithSteps(final double value, final double steps) {
        double a = ((Math.round(value / steps)) * steps);
        a *= 1000;
        a = (int) a;
        a /= 1000;
        return a;
    }

    public double lerp(final double a, final double b, final double c) {
        return a + c * (b - a);
    }

    public float lerp(final float a, final float b, final float c) {
        return a + c * (b - a);
    }

    /**
     * Gets the distance to the position. Args: x, y, z
     */
    public double getDistance(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double d0 = x2 - x1;
        final double d1 = y2 - y1;
        final double d2 = z2 - z1;
        return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
    }

    /**
     * Clamps a number, n, to be within a specified range
     * @param min The minimum permitted value of the input
     * @param max The maximum permitted value of the input
     * @param n The input number to clamp
     * @return The input, bounded by the specified minimum and maximum values
     */
    public double clamp(double min, double max, double n) {
        return Math.max(min, Math.min(max, n));
    }
}
