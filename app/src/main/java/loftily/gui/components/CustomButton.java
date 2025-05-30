package loftily.gui.components;

import loftily.gui.animation.Ripple;
import loftily.gui.font.FontManager;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class CustomButton extends GuiButton {
    
    private final Ripple ripple = new Ripple();
    private final Color backGroundColor;
    
    public CustomButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Color backGroundColor) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.backGroundColor = backGroundColor;
    }
    
    @Override
    public void drawScreen(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        
        Runnable backGroundRunnable = () -> RenderUtils.drawRoundedRect(xPosition, yPosition, width, height, 2,
                enabled ? backGroundColor : backGroundColor.darker());
        backGroundRunnable.run();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderUtils.startGlStencil(backGroundRunnable);
        ripple.draw();
        RenderUtils.stopGlStencil();
        
        FontManager.NotoSans.of(16).drawCenteredString(displayString, xPosition + width / 2F, yPosition + height / 3F, Colors.Text.color);
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY))
            ripple.add(mouseX, mouseY, 180, 500, 40, backGroundColor.brighter().brighter());
        return super.mousePressed(mc, mouseX, mouseY);
    }
}
