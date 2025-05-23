package loftily.gui.clickgui;

import loftily.Client;
import loftily.config.impl.ModuleConfig;
import loftily.gui.clickgui.components.impl.CategoryButton;
import loftily.gui.clickgui.components.impl.ModuleButton;
import loftily.gui.clickgui.value.ValuePanel;
import loftily.gui.interaction.Draggable;
import loftily.gui.interaction.Scrollable;
import loftily.module.ModuleCategory;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClickGui extends GuiScreen {
    //Positions
    public static final float CornerRadius = 3, Padding = 5;
    private final int width, height;
    private final Scrollable scrollableModuleButtons;
    private final float valuePanelWidth = 160;
    private final Draggable draggable;
    private int x, y;
    
    //CategoryButtons,ModuleButtons
    public final List<CategoryButton> categoryButtons;
    public List<ModuleButton> currentModuleButtons;
    public CategoryButton currentCategoryButton;
    @Getter
    private ValuePanel currentValuePanel;
    
    public ClickGui() {
        this.width = 430;
        this.height = 270;
        this.draggable = new Draggable(100, 100, 1);
        
        this.categoryButtons = new ArrayList<>();
        
        Arrays.stream(ModuleCategory.values()).forEach(category ->
                categoryButtons.add(new CategoryButton(90, 20, 98, 70, category, this)));
        this.currentCategoryButton = categoryButtons.get(0);
        
        this.currentModuleButtons = currentCategoryButton.moduleButtons;
        
        this.scrollableModuleButtons = new Scrollable(8);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        categoryButtons.forEach(categoryButton -> categoryButton
                .moduleButtons.forEach(moduleButton -> moduleButton.valuePanel.initGui()));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        draggable.updateDrag(mouseX, mouseY, width, height / 10, width, height, super.width, super.height);
        x = draggable.getPosX();
        y = draggable.getPosY();
        
        /* Background */
        RenderUtils.drawRoundedRect(x, y, width, height, CornerRadius, Colors.BackGround.color);
        
        /* CategoryButtons */
        drawCategoryButtons(mouseX, mouseY, partialTicks);
        
        
        /* ModuleButtons */
        updateModuleButtonsScorlls();
        renderModuleButtons(mouseX, mouseY, partialTicks);
        
        //Render currentValuePanel
        RenderUtils.startGlStencil(() -> RenderUtils.drawRoundedRect(x, y, width, height, CornerRadius, Colors.BackGround.color));
        if (currentValuePanel != null) {
            currentValuePanel.setX(x + width - (valuePanelWidth * currentValuePanel.animation.getValuef()));
            currentValuePanel.setY(y);
            currentValuePanel.setWidth(valuePanelWidth);
            currentValuePanel.setHeight(height);
            
            RenderUtils.drawRoundedRect(x, y, width, height, CornerRadius, ColorUtils.colorWithAlpha(Colors.BackGround.color, (int) (80 * currentValuePanel.animation.getValuef())));
            
            currentValuePanel.drawScreen(mouseX, mouseY, partialTicks);
            
            if (!currentValuePanel.out && currentValuePanel.animation.isFinished()) currentValuePanel = null;
        }
        RenderUtils.stopGlStencil();
    }
    
    private void renderModuleButtons(int mouseX, int mouseY, float partialTicks) {
        float baseXOffset = 10.5F;
        float panelStartX = x + CategoryButton.width + baseXOffset + Padding * 3;
        float panelStartY = y + Padding;
        
        float moduleButtonXOffset = 0.0F;
        float moduleButtonYOffset = scrollableModuleButtons.getValuef();
        int buttonCounts = 0;
        
        RenderUtils.startGlStencil(() -> RenderUtils.drawRoundedRect(
                panelStartX + Padding,
                panelStartY,
                width - (CategoryButton.width + baseXOffset) - Padding * 4.5F,
                height - Padding * 2,
                CornerRadius,
                new Color(255, 255, 255)
        ));
        
        for (ModuleButton moduleButton : currentModuleButtons) {
            moduleButton.setPosition(panelStartX + Padding + moduleButtonXOffset, panelStartY + moduleButtonYOffset);
            moduleButton.drawScreen(mouseX, mouseY, partialTicks);
            
            moduleButtonXOffset += moduleButton.width + Padding + 0.5F;
            buttonCounts++;
            
            if (buttonCounts % 3 == 0) {
                buttonCounts = 0;
                moduleButtonXOffset = 0.0F;
                moduleButtonYOffset += moduleButton.height + Padding;
            }
        }
        
        RenderUtils.stopGlStencil();
    }
    
    private void updateModuleButtonsScorlls() {
        //计算总高度，然后更新滚轮
        float buttonsHeight = 0.0F;
        int buttonsInRow = 0;
        float rowHeight = 0.0F;
        for (ModuleButton moduleButton : currentModuleButtons) {
            rowHeight = Math.max(rowHeight, moduleButton.height);
            if (++buttonsInRow == 3) {
                buttonsHeight += rowHeight + Padding;
                buttonsInRow = 0;
                rowHeight = 0;
            }
        }
        if (buttonsInRow > 0) buttonsHeight += rowHeight;
        buttonsHeight = Math.max(0, buttonsHeight - Padding - 140);
        
        if (currentValuePanel == null) {//确保当前没有ValuePanel避免冲突
            scrollableModuleButtons.setMax(buttonsHeight);
            scrollableModuleButtons.updateScroll();
        }
    }
    
    private void drawCategoryButtons(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRoundedRect(
                x + Padding,
                y + Padding,
                Padding + CategoryButton.width + Padding + 10,
                height - Padding * 2,
                CornerRadius - 1,
                Colors.OnBackGround.color);//backgound
        
        float categoryButtonsYOffset = 0;
        int categoryButtonX = x + 14;
        int categoryButtonY = y + 45;
        for (CategoryButton categoryButton : categoryButtons) {
            categoryButton.setPosition(categoryButtonX, categoryButtonY + categoryButtonsYOffset);
            categoryButton.drawScreen(mouseX, mouseY, partialTicks);
            categoryButtonsYOffset += categoryButton.getHeight() + 8;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        Client.INSTANCE.getConfigManager().get(ModuleConfig.class).write();
        
        if (currentValuePanel != null) {
            currentValuePanel.mouseClicked(mouseX, mouseY, mouseButton);
            if (!RenderUtils.isHovering(mouseX, mouseY, x + width - valuePanelWidth, y, valuePanelWidth, height) /* 鼠标ValuePanel上 */ &&
                    !RenderUtils.isHovering(mouseX, mouseY, x, y, width, height / 10F)/* 鼠标不在拖动位置上 */ &&
                    RenderUtils.isHovering(mouseX, mouseY, x, y, width, height)/* 鼠标在ClickGui上*/)
                currentValuePanel.out = false;
            return;
        }
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
        categoryButtons.forEach(categoryButton -> categoryButton.mouseClicked(mouseX, mouseY, mouseButton));
        currentModuleButtons.forEach(moduleButton -> moduleButton.mouseClicked(mouseX, mouseY, mouseButton));
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (currentValuePanel != null) currentValuePanel.mouseReleased(mouseX, mouseY, state);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        currentModuleButtons.forEach(moduleButton -> moduleButton.keyTyped(typedChar, keyCode));
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    public void setValuePanel(ValuePanel currentValuePanel) {
        if (currentValuePanel == null) {
            this.currentValuePanel.out = false;
            return;
        }
        this.currentValuePanel = currentValuePanel;
        this.currentValuePanel.out = true;
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (currentValuePanel != null) currentValuePanel.onGuiClosed();
    }
}
