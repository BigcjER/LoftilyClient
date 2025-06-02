package loftily.gui.interaction;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.utils.client.ClientUtils;
import loftily.utils.render.RenderUtils;
import lombok.Getter;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Draggable implements ClientUtils {
    private final int margin;
    private final Runnable afterDrag;
    private boolean isDragging = false;
    private int prevX = 0, prevY = 0;
    @Getter
    private int posX, posY;
    private int width, height;
    
    private final Animation dragEffectAnimation = new Animation(Easing.EaseOutExpo, 250);
    
    public Draggable(int startX, int startY, int margin, Runnable afterDrag) {
        this.posX = startX;
        this.posY = startY;
        this.margin = margin;
        this.afterDrag = afterDrag;
    }
    
    public Draggable(int startX, int startY, int margin) {
        this(startX, startY, margin, null);
    }
    
    public void updateDrag(int mouseX, int mouseY, int dragWidth, int dragHeight, int width, int height, int screenWidth, int screenHeight) {
        this.width = width;
        this.height = height;
        
        if (Mouse.isButtonDown(0)) {
            if (!isDragging && RenderUtils.isHovering(mouseX, mouseY, posX, posY, dragWidth, dragHeight)) {
                isDragging = true;
                prevX = mouseX - posX;
                prevY = mouseY - posY;
            }
            
            if (isDragging) {
                posX = mouseX - prevX;
                posY = mouseY - prevY;
                
                posX = Math.max(0, Math.min(posX, screenWidth - width));
                posY = Math.max(0, Math.min(posY, screenHeight - height));
                if (afterDrag != null) {
                    afterDrag.run();
                }
            }
        } else {
            isDragging = false;
        }
        posX = Math.max(margin, Math.min(posX, screenWidth - width - margin));
        posY = Math.max(margin, Math.min(posY, screenHeight - height - margin));
    }
    
    public void applyDragEffect(Runnable runnable) {
        dragEffectAnimation.run(isDragging ? 1 : 0);
        
        float scale = 1.0f - 0.08f * dragEffectAnimation.getValuef();
        float centerX = posX + width / 2.0f;
        float centerY = posY + height / 2.0f;
        
        if (!dragEffectAnimation.isFinished() || isDragging) {
            RenderUtils.drawRoundedRectOutline(
                    posX,
                    posY,
                    width,
                    height,
                    5,
                    0.8F,
                    new Color(0, 0, 0, 0),
                    new Color(255, 255, 255, (int) (255 * dragEffectAnimation.getValuef())));
        }
        
        GL11.glPushMatrix();
        
        GL11.glTranslatef(centerX, centerY, 0);
        GL11.glScalef(scale, scale, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);
        
        runnable.run();
        
        GL11.glPopMatrix();
    }
    
    
    public void updateDrag(int mouseX, int mouseY, int width, int height, int screenWidth, int screenHeight) {
        this.updateDrag(mouseX, mouseY, width, height, width, height, screenWidth, screenHeight);
    }
    
}
