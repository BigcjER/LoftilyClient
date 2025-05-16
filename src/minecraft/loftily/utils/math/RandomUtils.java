package loftily.utils.math;

public class RandomUtils {
    public static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min) + min);
    }
    public static double randomDouble(double min, double max) {
        return (Math.random() * (max - min) + min);
    }
}
