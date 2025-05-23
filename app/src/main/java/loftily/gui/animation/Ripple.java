package loftily.gui.animation;

import loftily.utils.render.ColorUtils;
import loftily.utils.render.RenderUtils;
import lombok.Setter;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Ripple extends CopyOnWriteArrayList<Ripple.RippleAnimation> {
    
    public void draw() {
        for (RippleAnimation rippleAnimation : this) {
            if (rippleAnimation.isFinished()) remove(rippleAnimation);
            rippleAnimation.drawRippleEffect();
        }
    }
    
    public boolean add(float x, float y, float diameter, long duration, int baseOpacity, Color color) {
        return super.add(new RippleAnimation(x, y, diameter, duration, baseOpacity, color));
    }
    
    public boolean add(float x, float y, float diameter, long duration, int baseOpacity) {
        return this.add(x, y, diameter, duration, baseOpacity, new Color(0, 0, 0));
    }
    
    
    protected static class RippleAnimation {
        private final float diameter;
        private final Animation animation;
        private final int baseOpacity;
        private final Color color;
        @Setter
        private float x, y;
        
        public RippleAnimation(float x, float y, float diameter, long duration, int baseOpacity, Color color) {
            this.x = x;
            this.y = y;
            this.diameter = diameter;
            this.animation = new Animation(Easing.EaseOutQuint, duration);
            this.baseOpacity = baseOpacity;
            this.color = color;
        }
        
        public RippleAnimation(float x, float y, float diameter, long duration, int baseOpacity) {
            this(x, y, diameter, duration, baseOpacity, new Color(0, 0, 0));
        }
        
        public void drawRippleEffect() {
            animation.run(diameter);
            
            float progress = Math.min(1.0f, (float) animation.getProgress());
            
            float widthAndHeight = (diameter * progress);
            
            float drawX = x - widthAndHeight / 2;
            float drawY = y - widthAndHeight / 2;
            int opacity = (int) (baseOpacity * (1 - Math.pow(progress, 4)));
            
            RenderUtils.drawRoundedRect(
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
}
