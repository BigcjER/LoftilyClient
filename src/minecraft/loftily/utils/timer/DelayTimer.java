package loftily.utils.timer;

public class DelayTimer {
    private long previousTime;
    
    public DelayTimer() {
        reset();
    }
    
    public boolean hasTimeElapsed(int milliseconds) {
        return System.currentTimeMillis() - previousTime >= milliseconds;
    }
    
    public void reset() {
        previousTime = System.currentTimeMillis();
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - previousTime;
    }
}
