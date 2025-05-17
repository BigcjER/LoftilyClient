package loftily.gui.menu.mainmenu;

import loftily.gui.animation.Ripple;
import loftily.gui.font.FontManager;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class MainMenuButton extends GuiButton {
    
    private final Ripple ripple = new Ripple();
    
    public MainMenuButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }
    
    public MainMenuButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }
    
    @Override
    public void drawScreen(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        
        Runnable backGroundRunnable = () -> RenderUtils.drawRoundedRect(xPosition, yPosition, width, height, 5, Colors.BackGround.color);
        backGroundRunnable.run();
        
        RenderUtils.startGlStencil(backGroundRunnable);
        ripple.draw();
        RenderUtils.stopGlStencil();
        
        FontManager.NotoSans.of(16).drawCenteredString(displayString, xPosition + width / 2F, yPosition + height / 3F, Colors.Text.color);
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY))
            ripple.add(mouseX, mouseY, 180, 500, 80, Colors.OnBackGround.color.brighter().brighter());
        return super.mousePressed(mc, mouseX, mouseY);
    }
}
