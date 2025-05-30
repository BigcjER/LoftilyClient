package loftily.gui.components;

import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiTextField;

public class CustomTextField extends GuiTextField {
    private final FontRenderer font;
    
    public CustomTextField(int componentId, net.minecraft.client.gui.FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
        font = FontManager.NotoSans.of(16);
    }
    
    @Override
    public void drawTextBox() {
        if (!this.getVisible()) return;
        
        if (this.getEnableBackgroundDrawing()) {
            RenderUtils.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 2,
                    Colors.OnBackGround.color.brighter());
        }
        
        int color = this.isEnabled ? this.enabledColor : this.disabledColor;
        int cursorPos = this.getCursorPosition() - this.lineScrollOffset;
        int selectionEnd = this.getSelectionEnd() - this.lineScrollOffset;
        
        String visibleText = font.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth(), false);
        boolean cursorVisible = this.isFocused && this.cursorCounter / 6 % 2 == 0 && cursorPos >= 0 && cursorPos <= visibleText.length();
        
        int paddingX = (this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition) + 1;
        int paddingY = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
        
        selectionEnd = Math.min(selectionEnd, visibleText.length());
        
        String beforeCursor = visibleText.substring(0, Math.max(0, Math.min(cursorPos, visibleText.length())));
        font.drawStringWithShadow(beforeCursor, paddingX, paddingY, color);
        int cursorX = paddingX + font.getStringWidth(beforeCursor);
        
        if (cursorPos < visibleText.length()) {
            String afterCursor = visibleText.substring(cursorPos);
            font.drawStringWithShadow(afterCursor, cursorX + 5, paddingY, color);
        }
        
        if (cursorVisible) {
            if (this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength()) {
                FontManager.NotoSans.of(18).drawStringWithShadow("|", cursorX + 1.3F, paddingY - 1.5, -3092272);
            } else {
                font.drawStringWithShadow("_", cursorX, paddingY, color);
            }
        }
        
        if (selectionEnd != cursorPos) {
            int selectionX = paddingX + font.getStringWidth(visibleText.substring(0, selectionEnd));
            this.drawCursorVertical(cursorX, paddingY - 2, selectionX, paddingY + font.getFontHeight() - 2);
        }
    }
    
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean clickedInside = mouseX >= this.xPosition && mouseX < this.xPosition + this.width
                && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;
        
        if (this.canLoseFocus) {
            this.setFocused(clickedInside);
        }
        
        if (this.isFocused && clickedInside && mouseButton == 0) {
            int relativeX = mouseX - this.xPosition - (this.enableBackgroundDrawing ? 4 : 0);
            
            String visibleText = font.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth(), false);
            int charIndex = font.trimStringToWidth(visibleText, relativeX, true).length();
            
            this.setCursorPosition(this.lineScrollOffset + charIndex);
            return true;
        }
        
        return false;
    }
    
    
}
