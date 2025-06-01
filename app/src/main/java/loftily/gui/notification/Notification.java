package loftily.gui.notification;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.components.MaterialIcons;
import loftily.gui.font.FontManager;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import lombok.Setter;

@Setter
public class Notification {
    public final Animation xAnimation, yAnimation;
    protected final float Width = 150, Height = 40, Radius = 2;
    private final String icon, title, message;
    private final long duration;
    public boolean out;
    protected float x, y;
    private long start;
    
    public Notification(NotificationType type, String title, String message, long duration) {
        switch (type) {
            case Waring:
                icon = MaterialIcons.get("warning");
                break;
            case Success:
                icon = MaterialIcons.get("check_circle");
                break;
            case Info:
            default:
                icon = MaterialIcons.get("info");
                break;
        }
        this.title = title;
        this.message = message;
        this.duration = duration;
        
        this.start = System.currentTimeMillis();
        this.xAnimation = new Animation(Easing.EaseOutExpo, 250);
        this.yAnimation = new Animation(Easing.EaseOutExpo, 300);
    }
    
    public void drawNotification() {
        xAnimation.setDuration(out ? 250 : 200);
        
        //背景
        Runnable backGroundRunnable = () -> {
            RenderUtils.drawRoundedRect(x, y, Width, Height, Radius, Colors.BackGround.color);
            RenderUtils.drawRoundedRect(x + Width - Radius, y, Radius, Height, 0, Colors.BackGround.color);
            RenderUtils.drawRoundedRect(x, y + Height - Radius, Width, Radius, 0, Colors.BackGround.color);
        };
        backGroundRunnable.run();
        
        FontManager.MaterialSymbolsSharp.of(28).drawString(icon, x + 4, y + 5, Colors.Text.color);
        FontManager.NotoSans.of(16).drawString(title, x + 23, y + 6.5F, Colors.Text.color);
        FontManager.NotoSans.of(15).drawString(message, x + 23, y + 21F, Colors.Text.color);
        
        RenderUtils.drawRoundedRect(x - 0.1F, y + Height - .7F, Math.min((int) ((Width / duration) * getTime()), Width), 1.2f, 0, Colors.Active.color);
    }
    
    public boolean isFinished() {
        return getTime() > duration;
    }
    
    public long getTime() {
        return System.currentTimeMillis() - start;
    }
    
}
