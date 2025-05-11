package loftily.gui.interaction;

import loftily.utils.timer.DelayTimer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

public class Scrollable {
    private final double valueMultiplier;
    private final loftily.utils.timer.DelayTimer timer = new DelayTimer();
    private double target;
    @Getter
    private double value;
    @Setter
    private double max;
    
    public Scrollable(double valueMultiplier) {
        this.valueMultiplier = valueMultiplier;
    }
    
    public void updateScroll() {
        int wheel = Mouse.getDWheel();
        
        if (wheel != 0) {
            target += wheel / 2D;
            target = MathHelper.clamp(target, -max, 0);
        }
        
        value += (target - value) * valueMultiplier * timer.getElapsedTime() / 1000D;
        
        timer.reset();
    }
    
    
    public float getValuef() {
        return (float) value;
    }
}