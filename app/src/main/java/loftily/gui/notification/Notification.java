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
    protected final float HEIGHT = 40, RADIUS = 2;
    protected float width = 150;
    private final String icon, title, message;
    private final long duration;
    public boolean out;
    protected float x, y;
    private long start;
    
    public Notification(NotificationType type, String title, String message, long duration) {
        switch (type) {
            case WARING:
                icon = MaterialIcons.get("warning");
                break;
            case SUCCESS:
                icon = MaterialIcons.get("check_circle");
                break;
            case INFO:
            default:
                icon = MaterialIcons.get("info");
                break;
        }
        this.title = title;
        this.message = message;
        this.duration = duration != 0 ? duration : FontManager.NotoSans.of(15).getStringWidth(message) * 30L;
        
        this.start = System.currentTimeMillis();
        this.xAnimation = new Animation(Easing.EaseOutExpo, 250);
        this.yAnimation = new Animation(Easing.EaseOutExpo, 300);
        
        this.width = Math.max(FontManager.NotoSans.of(15).getStringWidth(message) + 50, width);
    }
    
    public void drawNotification() {
        xAnimation.setDuration(out ? 250 : 200);
        
        //背景
        Runnable backGroundRunnable = () -> {
            RenderUtils.drawRoundedRect(x, y, width, HEIGHT, RADIUS, Colors.BackGround.color);
            RenderUtils.drawRoundedRect(x + width - RADIUS, y, RADIUS, HEIGHT, 0, Colors.BackGround.color);
            RenderUtils.drawRoundedRect(x, y + HEIGHT - RADIUS, width, RADIUS, 0, Colors.BackGround.color);
        };
        backGroundRunnable.run();
        
        FontManager.MaterialSymbolsSharp.of(28).drawString(icon, x + 4, y + 5, Colors.Text.color);
        FontManager.NotoSans.of(16).drawString(title, x + 23, y + 6.5F, Colors.Text.color);
        FontManager.NotoSans.of(15).drawString(message, x + 23, y + 21F, Colors.Text.color);
        
        RenderUtils.drawRoundedRect(x - 0.1F, y + HEIGHT - .7F, Math.min((int) ((width / duration) * getTime()), width), 1.2f, 0, Colors.Active.color);
    }
    
    public boolean isFinished() {
        return getTime() > duration;
    }
    
    public long getTime() {
        return System.currentTimeMillis() - start;
    }
    
}
