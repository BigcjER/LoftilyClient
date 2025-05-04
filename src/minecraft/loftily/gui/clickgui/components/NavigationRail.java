package loftily.gui.clickgui.components;

import loftily.gui.components.md.MD3Component;
import loftily.gui.components.md.impl.FloatingActionButton;
import loftily.module.ModuleCategory;
import loftily.utils.render.RenderUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class NavigationRail extends MD3Component {
    private final FloatingActionButton searchFAB;
    
    private final List<Badge> badges;//Categories
    @Getter
    @Setter
    private Badge currentBadge;
    
    public NavigationRail(float Height, float scaleFactor) {
        super(80, Height / scaleFactor, scaleFactor);
        searchFAB = new FloatingActionButton(scaleFactor, "search", () -> System.out.print(1));
        badges = new ArrayList<>();
        
        for (ModuleCategory moduleCategory : ModuleCategory.values()) {
            badges.add(new Badge(moduleCategory, Width, scaleFactor));
        }
        
        currentBadge = badges.get(0);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        searchFAB.setPosition(x + width / 2 - searchFAB.width / 2, y + height / 16);
        searchFAB.drawScreen(mouseX, mouseY, partialTicks);
        
        float badgeYOffset = 0;
        for (Badge badge : badges) {
            badge.setPosition(x + width / 2 - badge.width / 2, y + height / 5 + badgeYOffset);
            badge.drawScreen(mouseX, mouseY, partialTicks);
            badgeYOffset += badge.height + (12 * badge.scaleFactor);
        }
        
        //分割线
        RenderUtils.drawRoundedRect(x + width, y - 1, 0.4F, height + 2, 0, getTheme().getOutline());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchFAB.mouseClicked(mouseX, mouseY, mouseButton);
        badges.forEach(badge -> badge.mouseClicked(mouseX, mouseY, mouseButton));
    }
}
