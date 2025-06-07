package loftily.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    public static double round(final double value, final int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be non-negative.");
        if (Double.isNaN(value) || Double.isInfinite(value)) return value;
        
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
