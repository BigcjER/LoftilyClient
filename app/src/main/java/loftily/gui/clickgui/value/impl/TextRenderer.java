package loftily.gui.clickgui.value.impl;

import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.value.ValueRenderer;
import loftily.gui.components.CustomTextField;
import loftily.utils.render.Colors;
import loftily.value.impl.TextValue;
import net.minecraft.client.Minecraft;

public class TextRenderer extends ValueRenderer<TextValue> {
    
    private final CustomTextField customTextField;
    
    public TextRenderer(TextValue value) {
        super(value, 15);
        this.customTextField = new CustomTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, 80, 13);
        this.customTextField.setMaxStringLength(128);
        this.customTextField.setFocused(false);
        this.customTextField.setTextXOffset(-2);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        font.drawString(value.getName(), x + 6, y + height / 2 - font.getHeight() / 3F, Colors.Text.color);
        
        customTextField.xPosition = (int) (x + width - customTextField.getWidth() - ClickGui.PADDING * 4);
        customTextField.yPosition = (int) y;
        customTextField.drawTextBox();
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        customTextField.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        customTextField.textboxKeyTyped(typedChar, keyCode);
        value.setValue(customTextField.getText());
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        customTextField.updateCursorCounter();
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.customTextField.setFocused(false);
    }
}
