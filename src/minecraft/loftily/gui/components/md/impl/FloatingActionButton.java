package loftily.gui.components.md.impl;

import loftily.gui.components.md.MD3Component;
import loftily.gui.components.md.MaterialIcons;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.utils.render.RenderUtils;

/**
 * <a href="https://m3.material.io/components/floating-action-button/overview">...</a>
 */
public class FloatingActionButton extends MD3Component {
    private final Runnable onClick;
    private final String icon;
    
    /**
     * @param icon Material Icon的名字（必须存在于 MaterialIcons.IconMap）
     */
    public FloatingActionButton(float scaleFactor, String icon, Runnable onClick) {
        super(56, 56, scaleFactor);
        this.icon = icon;
        this.onClick = onClick;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRoundedRect(x, y, width, height, 16 * scaleFactor, getTheme().getPrimaryContainer());
        
        if (!MaterialIcons.IconMap.containsKey(icon))
            throw new IllegalArgumentException(String.format("Icon '%s' isn't a valid icon", icon));
        
        FontRenderer fontRenderer = FontManager.MaterialSymbolsSharp.of(30);
        fontRenderer.drawCenteredString(MaterialIcons.get(icon), x + width / 2, y + height / 2 - fontRenderer.getFontHeight() / 4F - 0.5F, getTheme().getOnPrimaryContainer());
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (mouseButton == 0 && RenderUtils.isHovering(mouseX, mouseY, x, y, width, height))
            onClick.run();
    }
}
