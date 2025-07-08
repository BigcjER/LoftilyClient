package loftily.gui.interaction.draggable;

import loftily.Client;
import loftily.config.impl.json.DragsJsonConfig;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.utils.client.ClientUtils;
import loftily.utils.render.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Draggable implements ClientUtils {
    private final int margin;
    private final Runnable afterDrag;
    private final Animation dragEffectAnimation = new Animation(Easing.EaseOutExpo, 250);
    @Getter
    private boolean isDragging = false;
    private int prevX = 0, prevY = 0;
    private int width, height;
    /**
     * Between 0 - 1
     */
    @Getter
    @Setter
    private float relativeX, relativeY;
    
    public Draggable(int startX, int startY, int margin, int screenWidth, int screenHeight, Runnable afterDrag) {
        this.relativeX = (float) startX / screenWidth;
        this.relativeY = (float) startY / screenHeight;
        this.margin = margin;
        this.afterDrag = afterDrag;
    }
    
    public Draggable(int startX, int startY, int margin, ScaledResolution sr, Runnable afterDrag) {
        this(startX, startY, margin, sr.getScaledWidth(), sr.getScaledHeight(), afterDrag);
    }
    
    public Draggable(int startX, int startY, ScaledResolution sr, int margin) {
        this(startX, startY, margin, sr, () -> Client.INSTANCE.getFileManager().get(DragsJsonConfig.class).write());
    }
    
    public int getPosX(int screenWidth) {
        return (int) (relativeX * screenWidth);
    }
    
    public int getPosY(int screenHeight) {
        return (int) (relativeY * screenHeight);
    }
    
    public void updateDrag(int mouseX, int mouseY,
                           int dragWidth, int dragHeight,
                           int width, int height,
                           int screenWidth, int screenHeight,
                           int clampStartX, int clampStartY) {
        this.width = width;
        this.height = height;
        boolean prevDragging = this.isDragging;
        
        int posX = getPosX(screenWidth);
        int posY = getPosY(screenHeight);
        
        if (Mouse.isButtonDown(0)) {
            if (!isDragging && RenderUtils.isHovering(mouseX, mouseY, posX, posY, dragWidth, dragHeight)) {
                isDragging = true;
                prevX = mouseX - posX;
                prevY = mouseY - posY;
            }
            
            if (isDragging) {
                posX = mouseX - prevX;
                posY = mouseY - prevY;
            }
        } else {
            isDragging = false;
        }
        
        if (prevDragging && !isDragging && afterDrag != null) {
            afterDrag.run();
        }
        
        this.relativeX = (float) Math.max(Math.max(margin, clampStartX), Math.min(posX, screenWidth - width - margin)) / screenWidth;
        this.relativeY = (float) Math.max(Math.max(margin, clampStartY), Math.min(posY, screenHeight - height - margin)) / screenHeight;
    }
    
    public void updateDrag(int mouseX, int mouseY, int dragWidth, int dragHeight, int width, int height, int screenWidth, int screenHeight) {
        updateDrag(mouseX, mouseY, dragWidth, dragHeight, width, height, screenWidth, screenHeight, 0, 0);
    }
    
    public void updateDrag(int mouseX, int mouseY, int width, int height, int screenWidth, int screenHeight) {
        this.updateDrag(mouseX, mouseY, width, height, width, height, screenWidth, screenHeight);
    }
    
    public void applyDragEffect(Runnable runnable, int offset, int screenWidth, int screenHeight) {
        dragEffectAnimation.run(isDragging ? 1 : 0);
        
        final int posX = getPosX(screenWidth);
        final int posY = getPosY(screenHeight);
        
        float scale = 1.0f - 0.08f * dragEffectAnimation.getValuef();
        float centerX = posX + width / 2.0f;
        float centerY = posY + height / 2.0f;
        
        if (!dragEffectAnimation.isFinished() || isDragging) {
            RenderUtils.drawRoundedRectOutline(
                    posX + offset,
                    posY,
                    width - offset * 2,
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
    
    public void applyDragEffect(Runnable runnable, ScaledResolution sr) {
        this.applyDragEffect(runnable, 0, sr.getScaledWidth(), sr.getScaledHeight());
    }
}