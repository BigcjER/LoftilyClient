package loftily.gui.menu.mainmenu;

import loftily.Client;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.font.FontManager;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.utils.render.Shader;
import loftily.utils.render.ShaderUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class MainMenu extends GuiScreen {
    private final Animation fadeInAnim = new Animation(Easing.Linear, 1000);
    private final int backgroundWidth = 150, backgroundHeight = 165;
    private final long startMillisTime = System.currentTimeMillis();
    private boolean ran = false;
    
    @Override
    public void initGui() {
        int startX = width / 2 - backgroundWidth / 2;
        int startY = 30 + height / 2 - backgroundHeight / 2;
        
        final int offset = 30;
        buttonList.clear();
        buttonList.add(new MainMenuButton(0, startX + 10, startY + 10, backgroundWidth - 20, 25, "Singleplayer"));
        buttonList.add(new MainMenuButton(1, startX + 10, startY + 10 + offset, backgroundWidth - 20, 25, "Multiplayer"));
        buttonList.add(new MainMenuButton(2, startX + 10, startY + 10 + offset * 2, backgroundWidth - 20, 25, "AltManager"));
        buttonList.add(new MainMenuButton(3, startX + 10, startY + 10 + offset * 3, backgroundWidth - 20, 25, "Settings"));
        buttonList.add(new MainMenuButton(4, startX + 10, startY + 10 + offset * 4, backgroundWidth - 20, 25, "Exit"));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        fadeInOut(() -> {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            ShaderUtils program = new ShaderUtils(Shader.BackGround);
            program.start();
            program.setUniformf("iMouse", mouseX, mouseY, 0, 0);
            program.setUniformf("iTime", (System.currentTimeMillis() - startMillisTime) / 1000F);
            program.setUniformf("iResolution", mc.displayWidth, mc.displayHeight);
            ShaderUtils.drawQuad();
            program.stop();
            
            int startX = width / 2 - backgroundWidth / 2;
            int startY = 30 + height / 2 - backgroundHeight / 2;
            
            RenderUtils.drawRoundedRect(startX - 2, startY - 2, backgroundWidth + 4, backgroundHeight + 4, 5, Colors.BackGround.color);
            RenderUtils.drawRoundedRect(startX, startY, backgroundWidth, backgroundHeight, 3, Colors.OnBackGround.color);
            
            FontManager.NotoSans.of(30).drawCenteredStringWithShadow(Client.Name, width / 2F, 60, Colors.Text.color.getRGB());
            
            super.drawScreen(mouseX, mouseY, partialTicks);
        });
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new GuiWorldSelection(this));
                break;
            case 1:
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case 2:
                break; //TODO AltManager
            case 3:
                mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 4:
                mc.shutdown();
        }
    }
    
    
    private void fadeInOut(Runnable runnable) {
        if (!ran) {
            if (!fadeInAnim.isFinished()) {
                fadeInAnim.run(254);
                runnable.run();
                int alpha = 254 - fadeInAnim.getValuei();
                RenderUtils.drawRect(0, 0, width, height, alpha << 24);
                return;
            }
            
            ran = true;
        }
        
        runnable.run();
    }
    
    
}
