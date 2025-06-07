package loftily.gui.components;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.animation.Ripple;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import lombok.Setter;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

@Setter
public class CustomTextField extends GuiTextField {
    private final FontRenderer font;
    private Color backGroundColor;
    private boolean drawRipple;
    private final Ripple ripple;
    private final Animation cursorAnimation;
    private final Animation cursorXAnimation;
    private int prevXPosition;
    public CustomTextField(int componentId, net.minecraft.client.gui.FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
        this.font = FontManager.NotoSans.of(18);
        this.backGroundColor = Colors.OnBackGround.color.brighter();
        this.drawRipple = false;
        this.ripple = new Ripple();
        this.cursorAnimation = new Animation(Easing.EaseOutCirc, 250);
        this.cursorXAnimation = new Animation(Easing.EaseOutCirc, 200);
        this.cursorXAnimation.setValue(x);
    }
    
    @Override
    public void drawTextBox() {
        if (!this.getVisible()) return;
        
        if (this.getEnableBackgroundDrawing()) {
            Runnable runnable = () -> RenderUtils.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 2, backGroundColor);
            runnable.run();
            if (drawRipple) {
                RenderUtils.startGlStencil(runnable);
                ripple.draw();
                RenderUtils.stopGlStencil();
            }
        }
        
        int color = this.isEnabled ? this.enabledColor : this.disabledColor;
        int cursorPos = this.getCursorPosition() - this.lineScrollOffset;
        int selectionEnd = this.getSelectionEnd() - this.lineScrollOffset;
        
        String visibleText = font.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth(), false);
        boolean cursorVisible = this.isFocused && this.cursorCounter / 7 % 3 == 0 && cursorPos >= 0 && cursorPos <= visibleText.length();
        
        int paddingX = (this.enableBackgroundDrawing ? this.xPosition + 6 : this.xPosition + 3);
        int paddingY = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
        
        selectionEnd = Math.min(selectionEnd, visibleText.length());
        
        String beforeCursor = visibleText.substring(0, Math.max(0, Math.min(cursorPos, visibleText.length())));
        font.drawString(beforeCursor, paddingX, paddingY, color);
        int cursorX = paddingX + font.getStringWidth(beforeCursor);
        
        if (cursorPos < visibleText.length()) {
            String afterCursor = visibleText.substring(cursorPos);
            font.drawString(afterCursor, cursorX, paddingY, color);
        }
        
        cursorXAnimation.run(cursorX);
        cursorAnimation.run(cursorVisible ? 1 : 0);
        if (Math.abs(xPosition - prevXPosition) > 100) {
            cursorXAnimation.run(xPosition);
            cursorXAnimation.setValue(xPosition);
        }
        
        int alpha = MathHelper.clamp((int) (254 * cursorAnimation.getValuef()), 1, 255);
        if (this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength()) {
            font.drawString("|", cursorXAnimation.getValuef() - 1F, paddingY - 0.5F,
                    ColorUtils.colorWithAlpha(new Color(-3092272), alpha));
        } else {
            font.drawString("_", cursorXAnimation.getValuef(), paddingY,
                    ColorUtils.colorWithAlpha(new Color(color), alpha).getRGB());
        }
        
        if (selectionEnd != cursorPos) {
            int selectionX = paddingX + font.getStringWidth(visibleText.substring(0, selectionEnd));
            this.drawCursorVertical(cursorX, paddingY - 2, selectionX, paddingY + font.getFontHeight() - 2);
        }
        
        prevXPosition = xPosition;
    }
    
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean clickedInside = mouseX >= this.xPosition && mouseX < this.xPosition + this.width
                && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;
        
        if (clickedInside && drawRipple)
            ripple.add(mouseX, mouseY, width + 80, 600, 80, backGroundColor.brighter().brighter());
        
        if (this.canLoseFocus) {
            this.setFocused(clickedInside);
        }
        
        if (this.isFocused && clickedInside && mouseButton == 0) {
            int relativeX = mouseX - this.xPosition - (this.enableBackgroundDrawing ? 6 : 3);
            
            String visibleText = font.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth(), false);
            int charIndex = font.trimStringToWidth(visibleText, relativeX, true).length();
            
            this.setCursorPosition(this.lineScrollOffset + charIndex);
            return true;
        }
        
        return false;
    }
    
    
    public int getHeight() {
        return height;
    }
    
    @Override
    public int getWidth() {
        if (this.getEnableBackgroundDrawing()) {
            return this.width - 16;
        } else {
            return this.width - 8;
        }
    }
}
