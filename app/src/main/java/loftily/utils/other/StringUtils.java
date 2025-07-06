package loftily.utils.other;

import java.time.Duration;
import java.util.regex.Pattern;

public class StringUtils {
    public static final Pattern PATTERN_WHITESPACE = Pattern.compile("\\s");
    
    public static String convertMillis(long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        
        
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        
        long seconds = duration.getSeconds();
        
        return String.format("%dh %dmin %ds", hours, minutes, seconds);
    }
}
