package loftily.gui.clickgui;

import loftily.Client;
import loftily.gui.clickgui.components.NavigationRail;
import loftily.gui.components.interaction.Draggable;
import loftily.utils.client.ClientUtils;
import loftily.utils.render.RenderUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class ClickGui extends GuiScreen implements ClientUtils {
    
    private final float Width = 905, Height = 680, ScaleFactor = 0.5F;
    private final float width = Width * ScaleFactor, height = Height * ScaleFactor;
    private final Draggable draggable;
    @Getter
    private final NavigationRail navigationRail;
    private float x, y;
    
    public ClickGui() {
        draggable = new Draggable(100, 100, 7);
        
        navigationRail = new NavigationRail(height, ScaleFactor);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //Drag
        draggable.updateDrag(mouseX, mouseY, (int) width, 20, (int) width, (int) height, super.width, super.height);
        x = draggable.getPosX();
        y = draggable.getPosY();
        
        RenderUtils.drawRoundedRectBoarded(x, y, width, height, Client.INSTANCE.getTheme(), () -> {
            navigationRail.setPosition(x, y);
            navigationRail.drawScreen(mouseX, mouseY, partialTicks);
        });
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        navigationRail.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
