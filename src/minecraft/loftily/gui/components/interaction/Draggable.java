package loftily.gui.components.interaction;

import loftily.utils.client.ClientUtils;
import loftily.utils.render.RenderUtils;
import lombok.Getter;
import org.lwjgl.input.Mouse;

public class Draggable implements ClientUtils {
    private final int margin;
    private final Runnable afterDrag;
    private boolean isDragging = false;
    private int prevX = 0, prevY = 0;
    @Getter
    private int posX, posY;
    
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
    
    public void updateDrag(int mouseX, int mouseY, int width, int height, int screenWidth, int screenHeight) {
        this.updateDrag(mouseX, mouseY, width, height, width, height, screenWidth, screenHeight);
    }
    
}
