package loftily.gui.clickgui.value;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.value.impl.*;
import loftily.gui.font.FontManager;
import loftily.gui.interaction.Scrollable;
import loftily.module.Module;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.Value;
import loftily.value.impl.*;
import loftily.value.impl.mode.ModeValue;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ValuePanel {
    public final Animation animation;
    private final List<ValueRenderer<?>> valueRenderers;
    private final Module module;
    private final Scrollable scrollable;
    public boolean out;
    @Setter
    private float x, y, width, height;
    
    public ValuePanel(Module module) {
        this.module = module;
        this.valueRenderers = new ArrayList<>();
        this.animation = new Animation(Easing.EaseOutQuart, 300);
        this.scrollable = new Scrollable(8);
        
        for (Value<?, ?> value : module.getValues()) {
            if (value instanceof BooleanValue) valueRenderers.add(new BooleanRenderer((BooleanValue) value));
            if (value instanceof ModeValue) valueRenderers.add(new ModeRenderer((ModeValue) value));
            if (value instanceof NumberValue) valueRenderers.add(new NumberRenderer((NumberValue) value));
            if (value instanceof RangeSelectionNumberValue)
                valueRenderers.add(new RangeSelectionNumberRenderer((RangeSelectionNumberValue) value));
            if (value instanceof MultiBooleanValue)
                valueRenderers.add(new MultiBooleanRenderer((MultiBooleanValue) value));
            if (value instanceof TextValue)
                valueRenderers.add(new TextRenderer((TextValue) value));
        }
    }
    
    public void initGui() {
        valueRenderers.forEach(ValueRenderer::initGui);
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        animation.run(out ? 1 : 0);
        
        RenderUtils.drawRoundedRect(x, y, width, height, ClickGui.CORNER_RADIUS, Colors.OnBackGround.color);
        
        float maxHeight = 0;
        for (ValueRenderer<?> valueRenderer : valueRenderers) {
            if (shouldSkipRenderer(valueRenderer)) continue;
            maxHeight += valueRenderer.height + 2;
        }
        
        scrollable.setMax(Math.max(0, maxHeight - 20));
        scrollable.updateScroll();
        
        float yOffset = scrollable.getValuef();
        for (ValueRenderer<?> valueRenderer : valueRenderers) {
            if (shouldSkipRenderer(valueRenderer)) continue;
            
            valueRenderer.width = width;
            valueRenderer.setPosition(x, y + 30 + yOffset);
            valueRenderer.drawScreen(mouseX, mouseY, partialTicks);
            yOffset += valueRenderer.height + 2;
        }
        
        
        float HeaderHeight = 26;
        RenderUtils.drawRoundedRect(x, y - 1, width, HeaderHeight, ClickGui.CORNER_RADIUS, Colors.OnBackGround.color);
        FontManager.NotoSans.of(16).drawString("· " + module.getName(),
                x + 5,
                y + (HeaderHeight / 2F) - FontManager.NotoSans.of(16).getHeight() / 3F,
                Colors.Text.color);
    }
    
    private boolean shouldSkipRenderer(ValueRenderer<?> valueRenderer) {
        return valueRenderer.value.getVisible() != null && !valueRenderer.value.getVisible().get();
    }
    
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        valueRenderers.forEach(valueRenderer -> valueRenderer.mouseClicked(mouseX, mouseY, mouseButton));
    }
    
    public void mouseReleased(int mouseX, int mouseY, int state) {
        valueRenderers.forEach(valueRenderer -> valueRenderer.mouseReleased(mouseX, mouseY, state));
    }
    
    public void onGuiClosed() {
        valueRenderers.forEach(ValueRenderer::onGuiClosed);
    }
    
    public void keyTyped(char typedChar, int keyCode) {
        valueRenderers.forEach(valueRenderer -> valueRenderer.keyTyped(typedChar, keyCode));
    }
    
    public void updateScreen() {
        valueRenderers.forEach(ValueRenderer::updateScreen);
    }
}
