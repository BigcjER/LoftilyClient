package loftily.gui.clickgui.value;

import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.value.Value;

public abstract class ValueRenderer<V extends Value<?, ?>> {
    protected float width, height;
    protected float x, y;
    protected V value;
    
    protected FontRenderer font = FontManager.NotoSans.of(16);
    
    public ValueRenderer(V value, float height) {
        this.width = 0;
        this.height = height;
        this.value = value;
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
    
    public void setCenteredPosition(float x, float y, float containerWidth, float containerHeight) {
        this.x = x + containerWidth / 2 - width / 2;
        this.y = y + containerHeight / 2 - height / 2;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
