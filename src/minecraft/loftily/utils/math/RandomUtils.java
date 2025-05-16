package loftily.utils.math;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static int randomInt(int start, int end) {
        if (end <= start) return start;
        return start + ThreadLocalRandom.current().nextInt(end - start);
    }
    
    public static double randomDouble(double start, double end) {
        if (end <= start) return start;
        return start + ThreadLocalRandom.current().nextDouble() * (end - start);
    }
    
    public static float randomFloat(float start, float end) {
        if (end <= start) return start;
        return start + ThreadLocalRandom.current().nextFloat() * (end - start);
    }
    
    public static String randomString(char[] chars, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(chars[random.nextInt(chars.length)]);
        }
        return stringBuilder.toString();
    }
    
    public static String randomString(int length) {
        return randomString("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray(), length);
    }
    
}
