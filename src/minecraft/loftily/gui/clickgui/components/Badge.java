package loftily.gui.clickgui.components;

import loftily.Client;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.components.md.MD3Component;
import loftily.gui.components.md.MaterialIcons;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.module.ModuleCategory;
import loftily.utils.render.RenderUtils;

import java.awt.*;

/**
 * <a href="https://m3.material.io/components/badges/overview">...</a>
 */
public class Badge extends MD3Component {
    private final ModuleCategory category;
    private final Animation activeIndicatorAnimation = new Animation(Easing.EaseOutExpo, 500);
    
    public Badge(ModuleCategory category, float width, float scaleFactor) {
        super(width, 56, scaleFactor);
        this.category = category;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        boolean active = Client.INSTANCE.getClickGui().getNavigationRail().getCurrentBadge() == this;
        Color color = active ? getTheme().getOnSurface() : getTheme().getOnSurfaceVariant();
        FontRenderer icon = FontManager.MaterialSymbolsSharp.of(48 * scaleFactor);
        
        float activeIndicatorWidth = 56 * scaleFactor;
        activeIndicatorAnimation.run(active ? activeIndicatorWidth : 0);
        RenderUtils.drawRoundedRect(
                x + width / 2 - activeIndicatorAnimation.getValuef() / 2 - 1F,
                y + 0.9F,
                activeIndicatorAnimation.getValuef(),
                32 * scaleFactor,
                32 * scaleFactor / 2F,
                getTheme().getSecondaryContainer());
        
        icon.drawCenteredString(MaterialIcons.get(category.icon), x + width / 2.05F, y + height / 6, color);
        
        FontManager.NotoSans.of(14)
                .drawCenteredString(category.name(), x + width / 2.05F, y + height - height / 3 + 4 * scaleFactor, color);
        
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && RenderUtils.isHovering(mouseX, mouseY, x, y, width, height)) {
            Client.INSTANCE.getClickGui().getNavigationRail().setCurrentBadge(this);
        }
    }
}
