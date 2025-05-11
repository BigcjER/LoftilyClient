package loftily.gui.clickgui.value.impl;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.Colors;
import loftily.gui.clickgui.value.ValueRenderer;
import loftily.gui.components.MaterialIcons;
import loftily.gui.font.FontManager;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.BooleanValue;

public class BooleanRenderer extends ValueRenderer<BooleanValue> {
    private final Animation animation;
    
    public BooleanRenderer(BooleanValue value) {
        super(value, 15);
        this.animation = new Animation(Easing.EaseOutQuart, 250);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        animation.setValue(value.getValue() ? 255 : 1);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        //RenderUtils.drawRoundedRect(x,y,width,height,0,new Color(255,0,0,80));
        font.drawString(value.getName(), x + 6, y + height / 2 - font.getHeight() / 3F, Colors.Text.color);
        
        animation.run(value.getValue() ? 255 : 1);
        
        float widthHeight = 8;
        float indicatorX = x + width - 13.2F;
        float indicatorY = y + height / 2 - widthHeight / 2;
        RenderUtils.drawRoundedRect(
                indicatorX,
                indicatorY,
                widthHeight,
                widthHeight,
                ClickGui.CornerRadius - 0.8F,
                ColorUtils.colorWithAlpha(Colors.Active.color, animation.getValuei()));
        
        FontManager.MaterialSymbolsSharp.of(15).drawString(
                MaterialIcons.get("check"),
                indicatorX,
                indicatorY + 2F,
                ColorUtils.colorWithAlpha(Colors.BackGround.color, animation.getValuei()));
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && RenderUtils.isHovering(mouseX, mouseY, x, y, width, height))
            value.setValue(!value.getValue());
    }
}
