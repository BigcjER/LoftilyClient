package loftily.alt.menu;

import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class AltMicrosoftLoginMenu extends GuiScreen {
    private final GuiScreen prevScreen;
    
    public AltMicrosoftLoginMenu(GuiScreen prevScreen) {
        this.prevScreen = prevScreen;
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prevScreen);
            return;
        }
        
        super.keyTyped(typedChar, keyCode);
    }
    
}
