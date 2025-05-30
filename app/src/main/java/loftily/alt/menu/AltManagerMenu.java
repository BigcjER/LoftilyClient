package loftily.alt.menu;

import loftily.Client;
import loftily.alt.Alt;
import loftily.gui.animation.Ripple;
import loftily.gui.components.CustomButton;
import loftily.gui.font.FontManager;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.utils.timer.DelayTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.RandomUtils;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class AltManagerMenu extends GuiScreen {
    public static final Color buttonColor = Colors.OnBackGround.color.brighter();
    private GuiButton removeButton, loginButton, randomAltButton;
    private AltButton focusedButton;
    private final DelayTimer timer = new DelayTimer();
    private final List<Alt> alts = Client.INSTANCE.getAltManager().getAlts();
    public static String currentText = "";
    
    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        
        int size = alts.size();
        
        int x = 130;
        int y = 10;
        
        for (int i = 0; i < size; i++) {
            Alt alt = alts.get(i);
            //loadTextureAsync(account.getUuid());
            
            this.buttonList.add(new AltButton(i, x, y, 180, 40, alt));
            
            if ((i + 1) % Math.max(1, (width - 140) / 190) == 0) {
                x = 130;
                y += 40 + 10;
            } else {
                x += 180 + 10;
            }
        }
        
        this.buttonList.add(new CustomButton(size, 10, 10, 80, 20, "Add", buttonColor));
        this.buttonList.add(this.loginButton = new CustomButton(size + 1, 10, 40, 80, 20, "Login", buttonColor));
        this.buttonList.add(this.removeButton = new CustomButton(size + 2, 10, 70, 80, 20, "Remove", buttonColor));
        this.buttonList.add(this.randomAltButton = new CustomButton(size + 3, 10, 100, 80, 20, "RandomAlt", buttonColor));
        this.buttonList.add(new CustomButton(size + 4, 10, 130, 80, 20, "RandomOffline", buttonColor));
        removeButton.enabled = false;
        loginButton.enabled = false;
        randomAltButton.enabled = !alts.isEmpty();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRectHW(0, 0, width, height, Colors.BackGround.color);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        randomAltButton.enabled = !alts.isEmpty();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button instanceof AltButton) {
            //设置所有button的focused为false
            buttonList.forEach(guiButton -> {
                if (guiButton instanceof AltButton) {
                    AltButton altButton = (AltButton) guiButton;
                    altButton.focused = false;
                }
            });
            
            AltButton clickedButton = (AltButton) button;
            clickedButton.focused = true;
            
            focusedButton = clickedButton;
            removeButton.enabled = true;
            loginButton.enabled = true;
            
            //双击登录
            if (clickedButton.doubleClick) {
                Alt account = alts.get(clickedButton.id);
                Client.INSTANCE.getAltManager().login(account);
                clickedButton.doubleClick = false;
                timer.reset();
            }
            return;
        }
        
        switch (button.id - alts.size()) {
            case 0:
                mc.displayGuiScreen(new AltSelectTypeMenu(this));
                break;
            
            case 1:
                if (focusedButton != null) {
                    Alt accountToRemove = alts.get(focusedButton.id);
                    alts.remove(accountToRemove);
                    Client.INSTANCE.getAltManager().remove(accountToRemove);
                    buttonList.clear();
                    initGui();
                    focusedButton = null;
                    removeButton.enabled = false;
                }
                break;
            
            case 2:
                if (!alts.isEmpty()) {
                    Alt account = alts.get(RandomUtils.nextInt(0, alts.size()));
                    Client.INSTANCE.getAltManager().login(account);
                    break;
                }
                
                currentText = TextFormatting.RED + "No accounts available.";
                timer.reset();
                break;
        }
    }
    
    static class AltButton extends GuiButton {
        public boolean focused, doubleClick;
        private final Alt alt;
        private final Ripple ripple;
        
        public AltButton(int buttonId, int x, int y, int widthIn, int heightIn, Alt alt) {
            super(buttonId, x, y, widthIn, heightIn, alt.getName());
            this.focused = false;
            this.doubleClick = false;
            this.alt = alt;
            this.ripple = new Ripple();
        }
        
        @Override
        public void drawScreen(Minecraft mc, int mouseX, int mouseY, float p_191745_4_) {
            if (!this.visible) return;
            this.hovered = RenderUtils.isHovering(mouseX, mouseY, xPosition, yPosition, width, height);
            Runnable backGroundRunnable = () -> RenderUtils.drawRoundedRect(
                    this.xPosition, this.yPosition, width, height, 2,
                    hovered ? ColorUtils.colorWithAlpha(Colors.Text.color, 40) : Colors.OnBackGround.color);
            
            backGroundRunnable.run();
            
            RenderUtils.startGlStencil(backGroundRunnable);
            ripple.draw();
            RenderUtils.stopGlStencil();
            
            
            if (focused) {
                RenderUtils.drawRoundedRectOutline(this.xPosition - 1, this.yPosition - 1, width + 2, height + 2,
                        2, 0.5f, new Color(255, 255, 255, 0),
                        Colors.Active.color);
            }
            
            
            Color color = Colors.Text.color;
            
            if (hovered)
                color = new Color(16777120);
            
            FontManager.NotoSans.of(16).drawCenteredString(this.displayString,
                    this.xPosition + (float) this.width / 2, this.yPosition + (float) (this.height - 8) / 2 - 7, color);
            FontManager.NotoSans.of(18).drawCenteredString(alt.getType().name(),
                    this.xPosition + (float) this.width / 2, this.yPosition + (float) (this.height - 8) / 2 + 6, Colors.Text.color.darker().darker());
            
        }
        
        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            ripple.add(mouseX, mouseY,
                    250, 500, 60,
                    Colors.OnBackGround.color.brighter().brighter().brighter());
            if (hovered && focused) {
                doubleClick = true;
            }
            return super.mousePressed(mc, mouseX, mouseY);
        }
    }
}
