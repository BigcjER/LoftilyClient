package loftily.alt.menu;

import loftily.alt.AltType;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.animation.Ripple;
import loftily.gui.font.FontManager;
import loftily.utils.client.ClientUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class AltSelectTypeMenu extends GuiScreen {
    private final GuiScreen prevScreen;
    private final Animation slideInAnimation;
    
    public AltSelectTypeMenu(GuiScreen prevScreen) {
        this.prevScreen = prevScreen;
        this.slideInAnimation = new Animation(Easing.EaseOutExpo, 400);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        buttonList.clear();
        
        buttonList.add(new AccountTypeButton(0, width / 2 - 100, 0, 100, AltType.Microsoft, Colors.OnBackGround.color));
        buttonList.add(new AccountTypeButton(1, width / 2 + 10, 0, 100, AltType.Offline, Colors.OnBackGround.color));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRectHW(0, 0, width, height, Colors.BackGround.color);
        
        slideInAnimation.run(1);
        FontManager.NotoSans.of(20).drawString("Choose a type", width / 2F - 30, height / 5F * slideInAnimation.getValuef(), Colors.Text.color);
        buttonList.forEach(guiButton -> guiButton.yPosition = height - (int) ((height / 2F + 20) * slideInAnimation.getValue()));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prevScreen);
            return;
        }
        
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public void onGuiClosed() {
        slideInAnimation.run(0);
        slideInAnimation.setValue(0);
        super.onGuiClosed();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new AltAddMicrosoftMenu(this));
                break;
            case 1:
                mc.displayGuiScreen(new AltAddOfflineMenu(this, fontRendererObj));
                break;
        }
    }
    
    private static class AccountTypeButton extends GuiButton {
        
        private static final ResourceLocation MicrosoftImage = new ResourceLocation("loftily/microsoft.png");
        private static final ResourceLocation OfflineImage = new ResourceLocation("loftily/minecraft.png");
        
        final ResourceLocation imageResourceLocation;
        final Color backGroundColor;
        final Ripple ripple;
        final int ImageSize = 60;
        
        public AccountTypeButton(int buttonId, int x, int y, int size, AltType altType, Color backGroundColor) {
            super(buttonId, x, y, size, size, altType.name());
            switch (altType) {
                case Offline:
                    imageResourceLocation = OfflineImage;
                    break;
                case Microsoft:
                    imageResourceLocation = MicrosoftImage;
                    break;
                default:
                    imageResourceLocation = null;
                    break;
            }
            this.backGroundColor = backGroundColor;
            this.ripple = new Ripple();
            
            //避免动画卡顿
            ClientUtils.mc.getTextureManager().bindTexture(MicrosoftImage);
            ClientUtils.mc.getTextureManager().bindTexture(OfflineImage);
        }
        
        @Override
        public void drawScreen(Minecraft mc, int p_191745_2_, int p_191745_3_, float p_191745_4_) {
            if (!this.visible) return;
            
            Runnable backGroundRunnable = () -> RenderUtils.drawRoundedRect(xPosition, yPosition, width, height, 2,
                    enabled ? backGroundColor : backGroundColor.darker());
            backGroundRunnable.run();
            RenderUtils.drawImage(imageResourceLocation, xPosition + width / 2F - 30, yPosition + height / 10F, ImageSize, ImageSize);
            
            FontManager.NotoSans.of(18).drawCenteredString(displayString, xPosition + width / 2F, yPosition + height - height / 5F, Colors.Text.color);
            
            RenderUtils.startGlStencil(backGroundRunnable);
            ripple.draw();
            RenderUtils.stopGlStencil();
        }
        
        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY))
                ripple.add(mouseX, mouseY, width * 2, 500, 80, backGroundColor.brighter().brighter().brighter().brighter());
            return super.mousePressed(mc, mouseX, mouseY);
        }
    }
}
