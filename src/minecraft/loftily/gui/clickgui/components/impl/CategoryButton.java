package loftily.gui.clickgui.components.impl;

import loftily.Client;
import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.animation.Ripple;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.Colors;
import loftily.gui.clickgui.components.Component;
import loftily.gui.components.MaterialIcons;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.module.ModuleCategory;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.RenderUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryButton extends Component {
    public static float width, height;
    public final List<ModuleButton> moduleButtons;
    private final ModuleCategory moduleCategory;
    
    private final Animation textAnimation;
    private final Ripple clickRippleAnimation;
    
    private boolean hovering;
    
    public CategoryButton(float width, float height, float moduleButtonWidth, float moduleButtonHeight, ModuleCategory category) {
        super(width, height);
        CategoryButton.width = width;
        CategoryButton.height = height;
        
        this.moduleButtons = new ArrayList<>();
        this.moduleCategory = category;
        
        Client.INSTANCE.getModuleManager().get(category).forEach(module -> moduleButtons.add(new ModuleButton(moduleButtonWidth, moduleButtonHeight, module)));
        
        this.textAnimation = new Animation(Easing.EaseOutQuint, 500);
        this.clickRippleAnimation = new Ripple();
    }
    
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        hovering = RenderUtils.isHovering(mouseX, mouseY, x, y, width, height) && Client.INSTANCE.getClickGui().currentValuePanel == null;
        boolean active = Client.INSTANCE.getClickGui().currentCategoryButton == this;
        
        /* bottomest ripple animation */
        RenderUtils.startGlStencil(() -> RenderUtils.drawRoundedRect(x, y, width, height, ClickGui.CornerRadius, new Color(0, 0, 0)));
        clickRippleAnimation.draw();
        RenderUtils.stopGlStencil();
        
        /* background */
        if (active)
            RenderUtils.drawRoundedRect(x, y, width, height, ClickGui.CornerRadius, ColorUtils.colorWithAlpha(Colors.Active.color, 60));
        
        /* icon,text */
        textAnimation.run(hovering || active ? 7 : 0);
        FontRenderer iconFont = FontManager.MaterialSymbolsSharp.of(24);
        String icon = MaterialIcons.get(moduleCategory.icon);
        
        iconFont.drawCenteredString(icon, x + 10F + (textAnimation.getValuef() * 1.05F), y + height / 3 - 1, Colors.Text.color);
        FontManager.NotoSans.of(16).drawStringWithShadow(moduleCategory.name(),
                x + 7 + iconFont.getStringWidth(icon) + textAnimation.getValue(),
                y + height / 3, Colors.Text.color.getRGB());
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && hovering) {
            Client.INSTANCE.getClickGui().currentCategoryButton = this;
            Client.INSTANCE.getClickGui().currentModuleButtons = moduleButtons;
            clickRippleAnimation.add(mouseX, mouseY, width + 28, 600, 80);
        }
    }
}
