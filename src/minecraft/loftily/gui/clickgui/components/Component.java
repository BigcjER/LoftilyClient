package loftily.gui.clickgui.components;

import loftily.utils.client.ClientUtils;
import lombok.Getter;

@Getter
public abstract class Component implements ClientUtils {
    public final float width, height;
    protected float x, y;
    
    public Component(float width, float height) {
        this.width = width;
        this.height = height;
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }
    
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }
    
    public void keyTyped(char typedChar, int keyCode) {
    }
    
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }
    
    public void onGuiClosed() {
    }
    
    public void initGui() {
    }
    
    public void updateScreen() {
    }
    
    public void setCenteredPosition(float x, float y,float containerWidth,float containerHeight) {
        this.x = x + containerWidth / 2 - width / 2;
        this.y = y + containerHeight / 2 - height / 2;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}