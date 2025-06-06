package loftily.gui.clickgui.value.impl;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.value.ValueRenderer;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.mode.Mode;
import loftily.value.impl.mode.ModeValue;

public class ModeRenderer extends ValueRenderer<ModeValue> {
    private final float ModeBoxHeight = 11;
    private final Animation expandAnimation;
    private boolean expanded;
    
    private int longestModeNameWidth;
    
    public ModeRenderer(ModeValue value) {
        super(value, 15);
        this.expandAnimation = new Animation(Easing.EaseOutQuart, 300);
        this.expanded = false;
        
        for (Mode mode : value.getModes()) {
            if (font.getStringWidth(mode.getName()) > longestModeNameWidth) {
                longestModeNameWidth = (int) (font.getStringWidth(mode.getName()) + ClickGui.PADDING);
            }
        }
    }
    
    @Override
    public void initGui() {
        super.initGui();
        expandAnimation.setValue(expanded ? (value.getModes().size()) * ModeBoxHeight : ModeBoxHeight);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        //RenderUtils.drawRoundedRect(x,y,width,height,0,new Color(255,0,0,80));
        font.drawString(value.getName(), x + 6, y + 3.5F, Colors.Text.color);
        
        expandAnimation.run(expanded ? (value.getModes().size()) * ModeBoxHeight : ModeBoxHeight);
        height = expandAnimation.getValuef() + 4F;
        
        float modeBoxWidth = Math.max(63, longestModeNameWidth);
        float modeBoxHeight = expandAnimation.getValuef();
        float modeBoxX = x + width - modeBoxWidth - ClickGui.PADDING;
        float modeBoxY = y + 2;
        
        RenderUtils.drawRoundedRect(modeBoxX, modeBoxY, modeBoxWidth, modeBoxHeight, ClickGui.CORNER_RADIUS - 1, Colors.BackGround.color);
        
        FontRenderer valueFont = FontManager.NotoSans.of(14);
        
        valueFont.drawCenteredString(
                value.getValueByName(),
                modeBoxX + modeBoxWidth / 2,
                modeBoxY + 2.3F,
                Colors.Text.color);
        
        //展开后的文字部分
        RenderUtils.startGlScissor((int) modeBoxX, (int) modeBoxY, (int) modeBoxWidth, (int) modeBoxHeight);
        float modesYOffset = 0;
        for (Mode mode : value.getModes()) {
            if (mode == value.getValue()) continue;
            String modeName = mode.getName();
            valueFont.drawString(
                    modeName,
                    modeBoxX + modeBoxWidth / 2 - (float) valueFont.getStringWidth(modeName) / 2,
                    modeBoxY + 2F + ModeBoxHeight + modesYOffset, Colors.Text.color);
            modesYOffset += ModeBoxHeight;
        }
        RenderUtils.stopGlScissor();
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if ((mouseButton != 0 && mouseButton != 1)) return;
        
        float modeBoxWidth = Math.max(63, longestModeNameWidth);
        float modeBoxX = x + width - modeBoxWidth - ClickGui.PADDING;
        float modeBoxY = y + 2;
        
        
        if (expanded) {
            float modesYOffset = 0;
            for (Mode mode : value.getModes()) {
                if (mode == value.getValue()) continue;
                if (RenderUtils.isHovering(mouseX, mouseY, modeBoxX, modeBoxY + modesYOffset + ModeBoxHeight + 0.5F, modeBoxWidth, ModeBoxHeight - 1.5F)) {
                    value.update(mode);
                }
                modesYOffset += ModeBoxHeight;
            }
        }
        
        if (RenderUtils.isHovering(mouseX, mouseY, x, y, width, height))
            expanded = !expanded;
    }
}
