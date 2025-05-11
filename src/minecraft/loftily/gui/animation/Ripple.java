package loftily.gui.animation;

import loftily.utils.render.ColorUtils;
import lombok.Setter;

import java.awt.*;

public class Ripple {
    private final float diameter;
    private final Animation animation;
    private final int baseOpacity;
    @Setter
    private float x, y;
    private final Color color;
    
    public Ripple(float x, float y, float diameter, long duration, int baseOpacity, Color color) {
        this.x = x;
        this.y = y;
        this.diameter = diameter;
        this.animation = new Animation(Easing.EaseOutQuint, duration);
        this.baseOpacity = baseOpacity;
        this.color = color;
    }
    
    public Ripple(float x, float y, float diameter, long duration, int baseOpacity) {
        this(x, y, diameter, duration, baseOpacity, new Color(0, 0, 0));
    }
    
    public void drawRippleEffect() {
        animation.run(diameter);
        
        float progress = Math.min(1.0f, (float) animation.getProgress());
        
        float widthAndHeight = (diameter * progress);
        
        float drawX = x - widthAndHeight / 2;
        float drawY = y - widthAndHeight / 2;
        int opacity = (int) (baseOpacity * (1 - Math.pow(progress, 4)));
        
        loftily.utils.render.RenderUtils.drawRoundedRect(
                drawX,
                drawY,
                widthAndHeight,
                widthAndHeight,
                widthAndHeight / 2,
                ColorUtils.colorWithAlpha(color, opacity)
        );
    }
    
    
    public boolean isFinished() {
        return animation.isFinished();
    }
}
