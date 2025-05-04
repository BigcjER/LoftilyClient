package loftily.gui.components.interaction;

import loftily.utils.timer.DelayTimer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

public class Scrollable {
    private final double valueMultiplier;
    private final loftily.utils.timer.DelayTimer timer = new DelayTimer();
    public double value, target, max;
    
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
}