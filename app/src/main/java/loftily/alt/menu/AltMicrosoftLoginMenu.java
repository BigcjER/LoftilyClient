package loftily.alt.menu;

import loftily.alt.microsoft.MicrosoftLoginThread;
import loftily.gui.components.CustomButton;
import loftily.gui.font.FontManager;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class AltMicrosoftLoginMenu extends GuiScreen {
    
    private final GuiScreen prevScreen;
    private MicrosoftLoginThread loginThread;
    
    public AltMicrosoftLoginMenu(GuiScreen prevScreen) {
        this.prevScreen = prevScreen;
        this.loginThread = new MicrosoftLoginThread();
    }
    
    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new CustomButton(0, width / 2 - 100, height / 2 + 20, 200, 25, "Back", Colors.OnBackGround.color.brighter()));
        if (loginThread == null || !loginThread.isAlive()) {
            loginThread = new MicrosoftLoginThread();
            loginThread.setDaemon(true);
            loginThread.start();
        }
        super.initGui();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRectHW(0, 0, width, height, Colors.BackGround.color);
        
        
        String currentText = loginThread.getCurrentText();
        
        FontManager.NotoSans.of(16).drawCenteredString(currentText, width / 2F, height / 2f - 30, Colors.Text.color);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            mc.displayGuiScreen(prevScreen);
        }
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
        super.onGuiClosed();
        
        if (loginThread != null && loginThread.isAlive()) {
            loginThread.interrupt();
        }
    }
}
