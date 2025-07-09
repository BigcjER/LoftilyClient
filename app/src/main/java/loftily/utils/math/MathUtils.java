package loftily.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtils {
    public static double round(final double value, final int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be non-negative.");
        if (Double.isNaN(value) || Double.isInfinite(value)) return value;
        
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    public static double gaussian(double x, double a, double b, double c) {
        return a * Math.exp(-Math.pow(x - b, 2) / (2 * Math.pow(c, 2)));
    }
}
